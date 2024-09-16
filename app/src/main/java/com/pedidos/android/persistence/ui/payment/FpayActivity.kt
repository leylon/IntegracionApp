package com.pedidos.android.persistence.ui.payment

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.db.entity.PaymentEntity
import com.pedidos.android.persistence.ui.menu.MenuActivity
import com.pedidos.android.persistence.viewmodel.FpayViewModel
import kotlinx.android.synthetic.main.activity_fpay.*
import kotlinx.android.synthetic.main.activity_fpay.fltLoading
import kotlinx.android.synthetic.main.payment_activity.*
import android.view.animation.LinearInterpolator

import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.pedidos.android.persistence.db.entity.PaymentIntentionResponseEntity
import com.pedidos.android.persistence.db.entity.PaymentIntentionsEntity
import com.pedidos.android.persistence.ui.sale.SaleActivity
import com.pedidos.android.persistence.utils.setQrCode
import kotlinx.android.synthetic.main.activity_fpay.btnCheckPayment
import kotlinx.android.synthetic.main.activity_fpay.btnSendPaymentInt
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.payment_success_dialog.view.*
import kotlin.math.roundToInt


class FpayActivity : MenuActivity() {
    companion object {
        val TAG = PaymentEntity::class.java.simpleName!!
        const val ENTITY_EXTRA = "com.example.android.persistence.ui.payment.entity"
    }
    private var handler = Handler()
    private var runnable : Runnable ?= null
    private var timeAdd = 0
    private lateinit var paymentEntity : PaymentIntentionsEntity
    private var round = false
    private var inCallPayment = false
    private lateinit var viewModel : FpayViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fpay)
        initSession()
        paymentEntity = intent.getSerializableExtra(PagoLinkActivity.ENTITY_EXTRA) as PaymentIntentionsEntity

        val factory = FpayViewModel.Companion.Factory(application, getSettings().urlbase)
        viewModel = ViewModelProviders.of(this, factory)[FpayViewModel::class.java]
        viewModel.showProgress.observe(this,{ showLoading(it!!) })
        viewModel.fpayLiveData.observe(this,{performAtQrPaymentFpay(it)})
        viewModel.paymentSuccessData.observe(this,{paymentSuccess(it)})
        viewModel.paymentCancel.observe(this,{cancelPaymentTimeOut(it)})
        viewModel.reverseSuccess.observe(this,{reverseSuccess(it)})
        viewModel.orderIdFpay.observe(this,{savePagoId(it?:"")})
        textFpayImport.setText(paymentEntity.amount.toString())
        textTotalFpay.text = paymentEntity.amount.toString()
        btnClose.setOnClickListener { exitPayment() }
        btnSendPaymentInt.setOnClickListener { viewModel.getPaymentData(
            PaymentIntentionsEntity(
                email = paymentEntity.email,
                market = paymentEntity.market,
                amount = textTotalFpay.text.toString().toFloat(),
                pedido = paymentEntity.pedido
            ),::onError) }
        btnToRoundFpay.setOnClickListener {
            if(round) {textTotalFpay.text = paymentEntity.amount.toString()
            round = false}
            else {
                textTotalFpay.text = paymentEntity.amount.roundToInt().toString()
                round = true
            }
        }
    }

    private fun exitPayment() {
        if(inCallPayment) {
            AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
                .setTitle(R.string.app_name)
                .setMessage("Desea REVERSAR Intención de Pago?")
                .setPositiveButton(R.string.yes) { d, _ -> viewModel.reversePayment(::onError)}
                .setNegativeButton(R.string.no) {d, _ -> d.dismiss()}
                .setCancelable(false)
                .create().show()
        } else {
            finish()
        }
    }

    fun reverseSuccess(msg: String?) {
        if( runnable != null)
            handler.removeCallbacks(runnable!!)
        finishAtPaymentFpay()
        val view = LayoutInflater.from(this).inflate(R.layout.payment_success_dialog, payment_activity_root, false)
        view.textMsg.text = msg
        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .show()
        view.btnAccept.setOnClickListener {
            dialog.dismiss()
            finish() }
    }

    fun paymentSuccess(msg : String?) {
        if( runnable != null)
        handler.removeCallbacks(runnable!!)
        loadPaymentSuccess(msg)
    }

    private fun initSession() {
        containerQrFpay.visibility = View.INVISIBLE
    }

    private fun showLoading(show: Boolean) {
        fltLoading.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun performAtQrPaymentFpay(paymentIntentionResponseEntity: PaymentIntentionResponseEntity?) {
        containerQrFpay.visibility = View.VISIBLE
        inCallPayment = true
        textOrderId.text = paymentIntentionResponseEntity?.orderId ?: ""
        textPaymentId.text = paymentIntentionResponseEntity?.pagoId ?: ""
        initAnimationQr(paymentIntentionResponseEntity)
        checkPaymentSuccess()
    }

    private fun checkPaymentSuccess() {
        handler.postDelayed(Runnable {
            handler.postDelayed(runnable!!, 4000)
            timeAdd += 1
            if(timeAdd >= 20) {
                cancelPaymentTimeOut("Se ha cancelado el proceso de pago, intentelo nuevamete.")
            }
            viewModel.checkPaymentSuccess(PaymentIntentionResponseEntity(pagoId = textPaymentId.text.toString(), orderId = textOrderId.text.toString()),::onError)

        }.also { runnable = it }, 4000)
    }

    private fun finishAtPaymentFpay() {
        containerQrFpay.visibility = View.INVISIBLE
        finishAnimationPayment()
    }

    private fun finishAnimationPayment() {
        btnSendPaymentInt.backgroundTintList = ContextCompat.getColorStateList(this,R.color.grayButton)
        btnCheckPayment.backgroundTintList = ContextCompat.getColorStateList(this, R.color.grayButton)
    }

    private fun cancelPaymentTimeOut(msg : String?) {
        finishAtPaymentFpay()
        onError(msg ?: "")
        handler.removeCallbacks(runnable!!)
    }

    private fun initAnimationQr(paymentIntentionResponseEntity: PaymentIntentionResponseEntity?) {
        imgQrFpay.setQrCode(paymentIntentionResponseEntity?.pagoId ?: "",100,100)
        btnSendPaymentInt.isClickable = false
        btnSendPaymentInt.isEnabled = false
        btnSendPaymentInt.backgroundTintList = ContextCompat.getColorStateList(this,R.color.grayButton)
        btnCheckPayment.backgroundTintList = ContextCompat.getColorStateList(this, R.color.link)
        val animation: Animation = AlphaAnimation(1f, 0f)
        animation.duration = 1000
        animation.interpolator = LinearInterpolator()
        animation.repeatCount = Animation.INFINITE
        animation.repeatMode = Animation.REVERSE
        imgQrFpay.startAnimation(animation)
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
        }
    }

    private fun savePagoId(id : String) {
        inCallPayment = false
        var sharedPreferences = getDefaultSharedPreferences(this)
        val editor = sharedPreferences.edit()
        editor.putString("id_fpay",id)
        editor.apply()
    }
    // Agregado CPV
    private fun onError(message: String) {
        Log.e(SaleActivity.TAG, message)

        AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
            .setTitle(R.string.app_name)
            .setMessage(message)
            .setPositiveButton(R.string.aceptar) { d, _ ->
                d.dismiss()
                finish()
            }
            .setCancelable(false)
            .create().show()
    }

    override fun onBackPressed() {
        if(inCallPayment) {
            AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
                .setTitle(R.string.app_name)
                .setMessage("Desea REVERSAR Intención de Pago?")
                .setPositiveButton(R.string.yes) { d, _ -> viewModel.reversePayment(::onError)}
                .setNegativeButton(R.string.no) {d, _ -> d.dismiss()}
                .setCancelable(false)
                .create().show()
        }else{
            finish()
        }
    }
}