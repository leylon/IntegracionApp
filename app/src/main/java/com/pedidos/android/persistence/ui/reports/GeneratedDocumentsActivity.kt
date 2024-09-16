package com.pedidos.android.persistence.ui.reports

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.db.entity.CashBalanceEntity
import com.pedidos.android.persistence.db.entity.GeneratedDocumentEntity
import com.pedidos.android.persistence.db.entity.ReceiptEntity
import com.pedidos.android.persistence.ui.menu.MenuActivity
import com.pedidos.android.persistence.viewmodel.GeneratedDocumentsViewModel
import kotlinx.android.synthetic.main.generated_documents_activity.*
import kotlinx.android.synthetic.main.generated_documents_activity.toolbar

class GeneratedDocumentsActivity : MenuActivity() {

    private lateinit var viewModel: GeneratedDocumentsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentViewWithMenu(R.layout.generated_documents_activity)
        setSupportActionBarMenu(toolbar)
        checkSession()

        val factory = GeneratedDocumentsViewModel.Companion.Factory(application, getSettings().urlbase)

        viewModel = ViewModelProviders.of(this, factory)[GeneratedDocumentsViewModel::class.java]

        viewModel.operationReportResult.observe(this, Observer {
            if (it != null) {
                showResults(it)
            }
        })

        viewModel.errorMessages.observe(this, Observer { showError(it) })
        viewModel.showProgress.observe(this, Observer { showProgress(it!!) })
        viewModel.receiptResult.observe(this, Observer {
            if (it != null) {
                printDocument(it)
            }
        })

        btnSearch.setOnClickListener { search() }
        rvwProducts.layoutManager = LinearLayoutManager(this)
        rvwProducts.adapter = GeneratedDocumentsAdapter(mutableListOf()) { viewModel.getDocumentToPrint(it.documentTypeTemp, it.documentNumberTemp)}
    }

    override fun onBackPressed(){}

    private fun search() {
        viewModel.showProgress.postValue(true)
        val date = "${datePickerGeneratedDocument.year}${"%02d".format(datePickerGeneratedDocument.month + 1)}${"%02d".format(datePickerGeneratedDocument.dayOfMonth)}"
        viewModel.getGeneratedDocuments(CashBalanceEntity().apply {
            this.date = date
            this.username = getSession().usuario
        })
    }

    private fun showResults(sales: List<GeneratedDocumentEntity>) {
        (rvwProducts.adapter as GeneratedDocumentsAdapter).updateList(sales)
    }

    private fun printDocument(document: ReceiptEntity){
        performPrinting(document.documentoPrint)
    }

    private fun showError(error: String?) {
        fltLoading.visibility = View.GONE
        AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(error)
                .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
                .show()
    }

    private fun showProgress(show: Boolean) {
        fltLoading.visibility = if (show) View.VISIBLE else View.GONE
    }
}
