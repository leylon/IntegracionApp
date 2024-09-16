package com.pedidos.android.persistence.ui.payment

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.db.entity.PaymentEntity
import com.pedidos.android.persistence.db.entity.PaymentIntentionResponseEntity
import com.pedidos.android.persistence.db.entity.PaymentIntentionsEntity
import com.pedidos.android.persistence.ui.menu.MenuActivity
import com.pedidos.android.persistence.ui.sale.SaleActivity
import com.pedidos.android.persistence.viewmodel.PlinkViewModel
import kotlinx.android.synthetic.main.activity_fpay.*
import kotlinx.android.synthetic.main.activity_fpay.btnSendPaymentInt
import kotlinx.android.synthetic.main.activity_fpay.fltLoading
import kotlinx.android.synthetic.main.activity_pago_link.*
import kotlinx.android.synthetic.main.activity_pago_link.btnCheckPayment
import kotlinx.android.synthetic.main.payment_activity.*
import kotlinx.android.synthetic.main.payment_success_dialog.view.*
import kotlin.math.roundToInt

class PagoLinkActivity : MenuActivity() {
    companion object {
        val TAG = PaymentEntity::class.java.simpleName!!
        const val ENTITY_EXTRA = "com.example.android.persistence.ui.payment.entity.pagolink"
    }

    private var handler = Handler()
    private var runnable : Runnable ?= null
    private var timeAdd = 0
    private lateinit var viewModel : PlinkViewModel
    private lateinit var paymentEntity : PaymentIntentionsEntity
    private var round = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pago_link)

        initSession()

        paymentEntity = intent.getSerializableExtra(ENTITY_EXTRA) as PaymentIntentionsEntity
        val factory = PlinkViewModel.Companion.Factory(application, getSettings().urlbase)
        viewModel = ViewModelProviders.of(this, factory)[PlinkViewModel::class.java]
        viewModel.showProgress.observe(this,{ showLoading(it!!) })
        viewModel.plinkLiveData.observe(this,{performAtPaymentPLink(it)})
        viewModel.paymentSuccessData.observe(this,{paymentSuccess(it)})
        viewModel.paymentCancel.observe(this,{cancelPaymentTimeOut(it)})
        viewModel.orderIdPagoLink.observe(this,{savePagoId(it?:"")})
        textPlinkImport.setText(paymentEntity.amount.toString())
        textTotalPlink.text = paymentEntity.amount.toString()
        btnSendPaymentInt.setOnClickListener {
            viewModel.getPaymentData(
                PaymentIntentionsEntity(
                market = paymentEntity.market,
                    amount = textTotalPlink.text.toString().toFloat(),
                    email = paymentEntity.email,
                    pedido = paymentEntity.pedido
            ),::onError) }
        btnToRound.setOnClickListener {
            if(round) {textTotalPlink.text = paymentEntity.amount.toString()
            round = false}
            else {
                textTotalPlink.text = paymentEntity.amount.roundToInt().toString()
                round = true
            }
        }

    }

    private fun paymentSuccess(msg: String?) {
        if( runnable != null)
        handler.removeCallbacks(runnable!!)
        loadPaymentSuccess(msg)
    }

    private fun cancelPaymentTimeOut(msg : String?) {
        finishAtPaymentPLink()
        onError(msg ?: "")
        handler.removeCallbacks(runnable!!)
        //finish()
    }
    private fun checkPaymentSuccess() {
        handler.postDelayed(Runnable {
            handler.postDelayed(runnable!!, 4000)
            timeAdd += 1
            if(timeAdd >= 20) {
                cancelPaymentTimeOut("Se ha cancelado el proceso de pago, intentelo nuevamente.")
            }
            viewModel.checkPaymentSuccess(PaymentIntentionResponseEntity(orderId = textOrdenId.text.toString()),::onError)
        }.also { runnable = it }, 4000)
    }

    private fun performAtPaymentPLink(paymentIntentionResponseEntity: PaymentIntentionResponseEntity?) {
        containerLoadingPayment.visibility = View.VISIBLE
        textOrdenId.text = paymentIntentionResponseEntity?.orderId
        initAnimationPayment()
        checkPaymentSuccess()
    }

    private fun finishAtPaymentPLink() {
        containerLoadingPayment.visibility = View.GONE
        finishAnimationPayment()
    }
    private fun finishAnimationPayment() {
        btnSendPaymentInt.backgroundTintList = ContextCompat.getColorStateList(this,R.color.grayButton)
        btnCheckPayment.backgroundTintList = ContextCompat.getColorStateList(this, R.color.grayButton)
    }
    private fun initAnimationPayment() {
        btnSendPaymentInt.isClickable = false
        btnSendPaymentInt.isEnabled = false
        btnSendPaymentInt.backgroundTintList = ContextCompat.getColorStateList(this,R.color.grayButton)
        btnCheckPayment.backgroundTintList = ContextCompat.getColorStateList(this, R.color.link)
    }

    private fun initSession() {
        containerLoadingPayment.visibility = View.INVISIBLE
    }

    private fun showLoading(show: Boolean) {
        fltLoading.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun loadPaymentSuccess(msg: String?) {
        val view = LayoutInflater.from(this).inflate(R.layout.payment_success_dialog, payment_activity_root, false)
        view.textMsg.text = msg
        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .show()
        view.btnAccept.setOnClickListener {
            dialog.dismiss()
            setResult(RESULT_OK)
            finish()
            finish()
        }
    }

    private fun savePagoId(id : String) {
        var sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPreferences.edit()
        editor.putString("id_plink",id)
        editor.apply()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if( runnable != null)
            handler.removeCallbacks(runnable!!)
    }
    // Agregado CPV
    private fun onError(message: String) {
        Log.e(SaleActivity.TAG, message)

        AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
            .setTitle(R.string.app_name)
            .setMessage(message)
            .setPositiveButton(R.string.aceptar) { d, _ -> d.dismiss() }
            .setCancelable(false)
            .create().show()
    }
}