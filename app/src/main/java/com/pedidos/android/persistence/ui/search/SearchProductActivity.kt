package com.pedidos.android.persistence.ui.search

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.db.entity.ProductEntity
import com.pedidos.android.persistence.ui.menu.MenuActivity
import com.pedidos.android.persistence.viewmodel.SearchProductViewModel
import com.google.zxing.integration.android.IntentIntegrator
import com.pedidos.android.persistence.utils.hideSoftInput
import kotlinx.android.synthetic.main.search_activity.*
import kotlinx.android.synthetic.main.search_imei_dialog.view.*


class SearchProductActivity : MenuActivity() {

    private var dialog: AlertDialog? = null
    private var view: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentViewWithMenu(R.layout.search_activity)
        setSupportActionBarMenu(toolbar)

        rvwSearch.layoutManager = LinearLayoutManager(this)
        rvwSearch.adapter = SearchProductAdapter(mutableListOf()) { insertData(it) }

        ivbSearch.setOnClickListener {
            this.hideSoftInput()
            tvwSearchDescription.setText("")
            searchProduct() }
        btnScan.setOnClickListener {
            openBarCodeReader() }
        ivbSearchDescription.setOnClickListener{
            this.hideSoftInput()
            tvwSearch.setText("")
            searchProductDescription()
        }
        val factory = SearchProductViewModel.Companion.Factory(application, getSettings().urlbase)

        val searchViewModel = ViewModelProviders.of(this, factory)[SearchProductViewModel::class.java]
        searchViewModel.searchResults.observe(this, Observer { showResults(it) })
        searchViewModel.searchDescriptionResults.observe(this, Observer { showResultDescription(it) })
        searchViewModel.imeiHelperResults.observe(this, Observer { showResultsForImei(it) })
        searchViewModel.errorResults.observe(this, Observer { showError(it) })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null) {
            val result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data)
            if (result != null) {
                when (requestCode) {
                    198 -> {
                        if (result.contents == null) {
                            AlertDialog.Builder(this)
                                    .setTitle(R.string.app_name)
                                    .setMessage("Lectura cancelada")
                                    .setPositiveButton(R.string.aceptar) { d, _ -> d.dismiss() }
                                    .setCancelable(false)
                                    .create().show()
                        } else {
                            tvwSearch.setText(result.contents)
                        }
                    }
                    199 -> {
                        if (result.contents == null) {
                            AlertDialog.Builder(this)
                                    .setTitle(R.string.app_name)
                                    .setMessage("Lectura cancelada")
                                    .setPositiveButton(R.string.aceptar) { d, _ -> d.dismiss() }
                                    .setCancelable(false)
                                    .create().show()
                        } else {
                            view?.edtImei?.setText(result.contents)
                        }
                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun insertData(productEntity: ProductEntity) {
        if (productEntity.stimei) {
            view = LayoutInflater.from(this).inflate(R.layout.search_imei_dialog, lltRoot, false)
            dialog = AlertDialog.Builder(this)
                    .setView(view)
                    .setCancelable(false)
                    .setTitle(R.string.propt_imei_title)
                    .show()

            view?.btnScan?.setOnClickListener {
                val integrator = IntentIntegrator(this)
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                integrator.setPrompt("Escanear producto")
                integrator.setOrientationLocked(false)
                integrator.setBeepEnabled(true)
                integrator.setBarcodeImageEnabled(true)
                integrator.setRequestCode(199)
                integrator.initiateScan()
            }
            view?.tvwAccept?.setOnClickListener {
                fltLoading.visibility = View.VISIBLE

                productEntity.imei = view?.edtImei?.text.toString()
                val factory = SearchProductViewModel.Companion.Factory(application, getSettings().urlbase)
                val searchViewModel = ViewModelProviders.of(this, factory)[SearchProductViewModel::class.java]
                searchViewModel.checkAutomaticallyForManualSearch(productEntity)
                dialog?.dismiss()
            }
        } else {
            val intent = Intent()
            intent.putExtra(PRODUCT_KEY, productEntity)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun showError(error: String?) {
        fltLoading.visibility = View.GONE
        AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(error)
                .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
                .show()
    }


    private fun searchProductDescription() {
        fltLoading.visibility = View.VISIBLE
        val searchViewModel = ViewModelProviders.of(this)[SearchProductViewModel::class.java]
        searchViewModel.searchProductDescription(tvwSearchDescription.text.toString())
    }

    private fun searchProduct() {
        fltLoading.visibility = View.VISIBLE
        val searchViewModel = ViewModelProviders.of(this)[SearchProductViewModel::class.java]
        searchViewModel.searchProduct(tvwSearch.text.toString())
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isEmpty()) return
        when (requestCode) {
            CAMERA_REQUEST_ID -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openBarCodeReader()
                } else {
                    showMessageForCamPermission(true)
                }
            }
        }
    }

    private fun openBarCodeReader() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                showMessageForCamPermission()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_ID)
            }
        } else {
            val integrator = IntentIntegrator(this)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            integrator.setPrompt("Escanear producto")
            integrator.setOrientationLocked(false)
            integrator.setBeepEnabled(true)
            integrator.setRequestCode(198)
            integrator.setBarcodeImageEnabled(true)
            integrator.initiateScan()
        }
    }

    private fun showMessageForCamPermission(messageType: Boolean = false) {
        AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(if (messageType) R.string.cam_permission_request_message_dont_show else R.string.cam_permission_request_message)
                .setPositiveButton(R.string.ok) { _, _ ->
                    if (!messageType)
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_ID)
                }.show()
    }

    private fun showResultDescription(it : List<ProductEntity>?) {
        fltLoading.visibility = View.GONE
        if(it == null) {
            showNoResults()
        } else {
            (rvwSearch.adapter as SearchProductAdapter).updateList(it)
        }
    }


    private fun showResults(it: ProductEntity?) {
        fltLoading.visibility = View.GONE
        if (it == null)
            showNoResults()
        else {
            (rvwSearch.adapter as SearchProductAdapter).updateList(listOf(it))
        }
    }

    private fun showResultsForImei(prod: ProductEntity?) {
        if (prod?.stimei2 == true) {
            fltLoading.visibility = View.GONE
            view = LayoutInflater.from(this).inflate(R.layout.search_imei_dialog, lltRoot, false)
            dialog = AlertDialog.Builder(this)
                    .setView(view)
                    .setCancelable(false)
                    .setTitle(R.string.propt_imei_title2)
                    .show()

            view?.btnScan?.setOnClickListener {
                val integrator = IntentIntegrator(this)
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                integrator.setPrompt("Escanear Imei 2")
                integrator.setOrientationLocked(false)
                integrator.setBeepEnabled(true)
                integrator.setBarcodeImageEnabled(true)
                integrator.setRequestCode(199)
                integrator.initiateScan()
            }
            view?.tvwAccept?.setOnClickListener {
                fltLoading.visibility = View.VISIBLE

                prod.imei2 = view?.edtImei?.text.toString()
                val intent = Intent()
                intent.putExtra(PRODUCT_KEY, prod)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        } else {
            fltLoading.visibility = View.GONE
            val intent = Intent()
            intent.putExtra(PRODUCT_KEY, prod)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun showNoResults() {

    }

    companion object {
        const val CAMERA_REQUEST_ID = 155
        const val PRODUCT_KEY = "product_key"
        const val PRODUCT_KEY_EXT = "product_key_ext"
        const val PRODUCT_KEY_DAMAGE = "product_key_damage"
        const val SALE_KEY = "product_key"
        const val SIZE_PRODUCTS = "size_product_key"
    }
}
