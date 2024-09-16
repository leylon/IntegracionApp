package com.pedidos.android.persistence.api

import android.os.AsyncTask
import android.util.Log
import com.pedidos.android.persistence.model.Login
import com.pedidos.android.persistence.model.LoginResponse

class LoginApiTask internal constructor(private val mEmail: String,
                                        private val mPassword: String,
                                        private var mImei: String,
                                        private val onSuccess: (m: LoginResponse) -> Unit,
                                        private val onError: (m: String) -> Unit,
                                        private val onProgress: (m: Boolean) -> Unit,
                                        private val repository : CoolboxApi) : AsyncTask<Void, Void, Boolean>() {


    private lateinit var loginResponse: LoginResponse
    private lateinit var errorMessage: String

    companion object {
        val TAG = LoginApiTask::class.java.simpleName!!

    }

    override fun doInBackground(vararg params: Void): Boolean? {
        try {
            val response = repository.login(Login(mEmail, mPassword,mImei)).execute()
            if (response.isSuccessful) {
                if (response.body()!!.result) {
                    loginResponse = response.body()!!.data!!
                    loginResponse.usuario = mEmail
                    return true
                } else {
                    errorMessage = response.body()!!.message
                    return false
                }
            }

            return false

        } catch (e: InterruptedException) {
            Log.i(TAG, e.toString())
            return false
        }
    }

    override fun onPostExecute(success: Boolean?) {
        onProgress(false)

        if (success!!) {
            onSuccess(loginResponse)
        } else if (::errorMessage.isInitialized) {
            onError(errorMessage)
        }
    }

    override fun onCancelled() {
        onProgress(false)
    }
}