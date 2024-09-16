package com.pedidos.android.persistence.ui.reports

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.db.entity.OperationReportEntity
import com.pedidos.android.persistence.db.entity.OperationReportResponseEntity
import com.pedidos.android.persistence.ui.BasicApp
import com.pedidos.android.persistence.ui.menu.MenuActivity
import com.pedidos.android.persistence.utils.Defaults
import com.pedidos.android.persistence.viewmodel.OperationsReportViewModel
import kotlinx.android.synthetic.main.operations_report_activity.*
import pe.beyond.visanet.manager.MPOSError
import pe.beyond.visanet.manager.MPOSManagerSession
import pe.beyond.visanet.manager.MPOSResponseBean
import pe.beyond.visanet.manager.listener.MPOSHistoryListener

class OperationsReportActivity : MenuActivity() {

    companion object {
        val TAG = OperationsReportActivity::class.java.simpleName!!
    }

    private lateinit var manager: MPOSManagerSession
    private lateinit var viewModel: OperationsReportViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentViewWithMenu(R.layout.operations_report_activity)
        setSupportActionBarMenu(toolbar)
        checkSession()

        val factory = OperationsReportViewModel.Companion.Factory(application, getSettings().urlbase)
        viewModel = ViewModelProviders.of(this, factory)[OperationsReportViewModel::class.java]

        viewModel.operationReportResult.observe(this, Observer { onGenerateReportFinished(it!!) })
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

        btnGenerarReporte.setOnClickListener { generarReporteDeOperaciones() }
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

    private fun generarReporteDeOperaciones() {
        viewModel.showProgress.postValue(true)
        val data = generateOperationsReportEntity()

        manager.history(this, object : MPOSHistoryListener {
            override fun mPOSGetVoucher(result: MPOSResponseBean?) {
                data.mposBean = result!!.toString()
                viewModel.generateOperationsReport(data)
            }

            override fun mPOSHistoryError(result: MPOSError?) {
                viewModel.errorMessages.postValue(result?.message)
            }

            override fun mPOSCancellationComplete(result: MPOSResponseBean?) {
                data.mposBean = result!!.toString()
                viewModel.generateOperationsReport(data)
            }
        })
    }

    private fun onGenerateReportFinished(data: OperationReportResponseEntity) {
        this.performPrinting(data.documentToPrint)
    }

    private fun generateOperationsReportEntity(): OperationReportEntity {
        return OperationReportEntity()
                .apply {
                    numeroDocumento = edwNumeroDocumento.text.toString()
                    tipoDocumento = if (rdwFactura.isChecked) Defaults.FACTURA else Defaults.BOLETA
                }
    }

    private fun showProgress(show: Boolean) {
        fltLoading.visibility = if (show) View.VISIBLE else View.GONE
    }
}
