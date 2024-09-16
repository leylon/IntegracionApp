package com.pedidos.android.persistence.ui.reports

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import android.view.View
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.db.entity.CashBalanceResponseEntity
import com.pedidos.android.persistence.ui.menu.MenuActivity
import com.pedidos.android.persistence.viewmodel.CashBalanceViewModel
import kotlinx.android.synthetic.main.cash_balance_activity.*

class CashBalanceActivity : MenuActivity() {

    companion object {
        val TAG = CashBalanceActivity::class.java.simpleName!!
    }

    private lateinit var viewModel: CashBalanceViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentViewWithMenu(R.layout.cash_balance_activity)
        setSupportActionBarMenu(toolbar)
        checkSession()

        val factory = CashBalanceViewModel.Companion.Factory(application, getSettings().urlbase)
        viewModel = ViewModelProviders.of(this, factory)[CashBalanceViewModel::class.java]
        viewModel.cashBalanceResult.observe(this, Observer { printReport(it!!) })

        btnPrint.setOnClickListener { generateReport() }
    }

    override fun onBackPressed(){}

    private fun generateReport() {
        val date = "${datePickerCashBalance.year}${"%02d".format(datePickerCashBalance.month + 1)}${"%02d".format(datePickerCashBalance.dayOfMonth)}"
        viewModel.generateReport(getSession().usuario, date)
    }

    private fun printReport(cashBalance: CashBalanceResponseEntity) {
        Log.i(TAG, "The cash balanace is: " + cashBalance.result)
        fltLoading.visibility = View.VISIBLE
        if (performPrinting(cashBalance.result)) {
            fltLoading.visibility = View.GONE
        }
    }
}
