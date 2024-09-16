package com.pedidos.android.persistence.ui.cancel

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.db.entity.CancelSaleEntity
import com.pedidos.android.persistence.db.entity.CancelSaleResponseEntity
import com.pedidos.android.persistence.ui.BasicApp
import com.pedidos.android.persistence.ui.menu.MenuActivity
import com.pedidos.android.persistence.utils.Defaults
import com.pedidos.android.persistence.viewmodel.CancelViewModel
import kotlinx.android.synthetic.main.cancel_activity.*
import pe.beyond.visanet.manager.MPOSError
import pe.beyond.visanet.manager.MPOSManagerSession
import pe.beyond.visanet.manager.MPOSResponseBean
import pe.beyond.visanet.manager.listener.MPOSCancellationListener

class CancelActivity : MenuActivity() {

    companion object {
        val TAG = CancelActivity::class.java.simpleName!!
    }

    private lateinit var manager: MPOSManagerSession
    private lateinit var viewModel: CancelViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentViewWithMenu(R.layout.cancel_activity)
        setSupportActionBarMenu(toolbar)
        checkSession()

        val factory = CancelViewModel.Companion.Factory(application, getSettings().urlbase)
        viewModel = ViewModelProviders.of(this, factory)[CancelViewModel::class.java]

        viewModel.cancelResult.observe(this, Observer { onCancelTransactioFinished(it!!) })
        viewModel.showProgress.observe(this, Observer { showProgress(it!!) })
        viewModel.errorMessages.observe(this, Observer {
            if (it != null) {
                printOnSnackBar(it)
            }
        })

        try {
            manager = MPOSManagerSession(this, BasicApp.URL_VISA, BasicApp.KEY_VISA)
            manager.setIsVoucherRequired(true)
        } catch (ex: Exception) {
            Log.d("VISANET-APP", ex.message, ex)
        }


        btnAnular.setOnClickListener { anularTransaccion() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 100) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish()
            }
        }

        if (::manager.isInitialized)
            manager.parseResult(requestCode, resultCode, data)
    }

    override fun onBackPressed(){}

    private fun anularTransaccion() {
        val data = generateCancelSaleEntity()
        manager.cancellation(this, data.numeroTransaccion, object : MPOSCancellationListener {
            override fun mPOSCancellation(result: MPOSResponseBean?) {
                data.mposBean = result!!.toString()
                viewModel.anularPedido(data)
            }

            override fun mPOSCancellationError(error: MPOSError?) {
                viewModel.errorMessages.postValue("codigo: ${error?.errorCode} mensaje: ${error?.message}")
            }
        })
    }

    private fun onCancelTransactioFinished(data: CancelSaleResponseEntity) {
        this.performPrinting(data.documentToPrint)
    }

    private fun generateCancelSaleEntity(): CancelSaleEntity {
        return CancelSaleEntity()
                .apply {
                    numeroDocumento = edwNumeroDocumento.text.toString()
                    numeroTransaccion = edwNumeroTransaccion.text.toString()
                    tipoDocumento = if (rdwFactura.isChecked) Defaults.FACTURA else Defaults.BOLETA

                }
    }

    private fun showProgress(show: Boolean) {
        fltLoading.visibility = if (show) View.VISIBLE else View.GONE
    }
}
