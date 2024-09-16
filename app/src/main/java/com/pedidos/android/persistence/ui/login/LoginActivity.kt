package com.pedidos.android.persistence.ui.login

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.LoaderManager.LoaderCallbacks
import android.content.Context
import android.content.CursorLoader
import android.content.Intent
import android.content.Loader
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import com.google.gson.Gson
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.api.LoginApiTask
import com.pedidos.android.persistence.model.LoginResponse
import com.pedidos.android.persistence.ui.BasicApp
import com.pedidos.android.persistence.ui.menu.MenuActivity
import com.pedidos.android.persistence.ui.sale.SaleActivity
import com.pedidos.android.persistence.utils.hideSoftInput
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*
import kotlin.collections.ArrayList


class LoginActivity : MenuActivity(), LoaderCallbacks<Cursor> {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private var mAuthApiTask: LoginApiTask? = null
    var imei: String? = null

    val PHONESTATS = 0x1
//    val tel = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setSupportActionBar(toolbar)
        textVersion.text = "Version : " + BasicApp.APP_VERSION
        supportActionBar!!.setDisplayShowTitleEnabled(false)
       // IMEI()
        //permissionPhone()
        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        email_sign_in_button.setOnClickListener {
            this.hideSoftInput()
            attemptLogin() }


        //get session
        val sessionActive = getSession()
        //no quitar validacion de null
        if (sessionActive.usuario == null || sessionActive.usuario == "") {
            return
        }

        openSales()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun permissionPhone(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //IMEI()
            consultarPermiso(Manifest.permission.READ_PHONE_STATE, PHONESTATS);
        }else {
            Toast.makeText(
                this,
                "${Build.VERSION.SDK_INT}",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun attemptLogin() {
        if (mAuthApiTask != null) {
            return
        }

        // Reset errors.
        email.error = null
        password.error = null
       // val tel = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        // Store values at the time of the login attempt.
        val emailStr = email.text.toString()
        val passwordStr = password.text.toString()
        var imeiPhone = "error" //tel.imei.toString()
        val androidID: String =
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        imeiPhone = androidID
        val cancel = false
        val focusView: View? = null

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            mAuthApiTask = LoginApiTask(emailStr, passwordStr, imeiPhone,::onSuccess, ::onError, ::showProgress, getRepository())
            mAuthApiTask!!.execute(null as Void?, null as Void?)
            mAuthApiTask = null
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @SuppressLint("ObsoleteSdkInt")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            login_form.visibility = if (show) View.GONE else View.VISIBLE
            login_form.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_form.visibility = if (show) View.GONE else View.VISIBLE
                        }
                    })

            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        } else {
            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_form.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<Cursor> {
        return CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE + " = ?", arrayOf(ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE),

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC")
    }

    override fun onLoadFinished(cursorLoader: Loader<Cursor>, cursor: Cursor) {
        val emails = ArrayList<String>()
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS))
            cursor.moveToNext()
        }

        addEmailsToAutoComplete(emails)
    }

    override fun onLoaderReset(cursorLoader: Loader<Cursor>) {

    }

    private fun addEmailsToAutoComplete(emailAddressCollection: List<String>) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        val adapter = ArrayAdapter(this@LoginActivity,
                android.R.layout.simple_dropdown_item_1line, emailAddressCollection)

        email.setAdapter(adapter)
    }

    object ProfileQuery {
        val PROJECTION = arrayOf(
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY)
        val ADDRESS = 0
        val IS_PRIMARY = 1
    }

    private fun onSuccess(loginResponse: LoginResponse) {
        val androidID: String =
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        loginResponse.imei = androidID
        saveSession(loginResponse)
        openSales()
        finish()
    }

    private fun onError(err: String) {
        Log.e(TAG, err)
        password.error = err //getString(R.string.error_incorrect_password)
        password.requestFocus()
    }

    companion object {
        const val SESSION_USER_NAME = "session_user_name"
        const val NAMESPACE = "com.example.android.persistence"
        val TAG = LoginActivity::class.java.simpleName!!
    }

    private fun openSales() {
        val intent = Intent(this, SaleActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun saveSession(login: LoginResponse) {
        val preferences = getSharedPreferences(NAMESPACE, MODE_PRIVATE)

        preferences.edit().putString(
                SESSION_USER_NAME,
                Gson().toJson(login)
        ).apply()
    }

    // Con este método mostramos en un Toast con un mensaje que el usuario ha concedido los permisos a la aplicación
    private fun consultarPermiso(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(
                this@LoginActivity,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@LoginActivity,
                    permission
                )
            ) {
                ActivityCompat.requestPermissions(
                    this@LoginActivity,
                    arrayOf(permission),
                    requestCode
                )
            } else {
                ActivityCompat.requestPermissions(
                    this@LoginActivity,
                    arrayOf(permission),
                    requestCode
                )
            }
        } else {
            //imei = obtenerIMEI()
            println("imei: "+imei)
            Toast.makeText(
                this,
                "$permission El permiso a la aplicación esta concedido.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    // Con este método consultamos al usuario si nos puede dar acceso a leer los datos internos del móvil
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {


                // Validamos si el usuario acepta el permiso para que la aplicación acceda a los datos internos del equipo, si no denegamos el acceso
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //imei = obtenerIMEI()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Has negado el permiso a la aplicación",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }


    private fun obtenerIMEI(): String {
        val telephonyManager = this@LoginActivity.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Hacemos la validación de métodos, ya que el método getDeviceId() ya no se admite para android Oreo en adelante, debemos usar el método getImei()
           telephonyManager.getImei(PHONESTATS)
        } else {
            telephonyManager.deviceId
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun IMEI()
    {
        var myIMEI: String? = null
        try
        {
            val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val IMEI = tm.getMeid()
            if (IMEI != null)
            {
                val androidID: String =
                    Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                val serialID = Build.SERIAL
                val uuID: String = UUID.randomUUID().toString()

                imei = androidID

            }else {
                println("no se encontro IMEI")
            }
        }
        catch (ex:Exception)
        {
            Toast.makeText(this, ex.toString(),Toast.LENGTH_SHORT ).show()

        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission. READ_PHONE_STATE) !=
            PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat. shouldShowRequestPermissionRationale(this, android.Manifest.permission. READ_PHONE_STATE))
            {

            }
            else
            {
                ActivityCompat. requestPermissions(this, arrayOf(android.Manifest. permission.READ_PHONE_STATE), 2)

            }
        }
    }
}
