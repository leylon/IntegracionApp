package com.pedidos.android.persistence.viewmodel

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.pedidos.android.persistence.api.CoolboxApi
import com.pedidos.android.persistence.db.entity.PaymentIntentionResponseEntity
import com.pedidos.android.persistence.db.entity.PaymentIntentionsEntity
import com.pedidos.android.persistence.ui.BasicApp
import com.pedidos.android.persistence.utils.ApiWrapper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlinkViewModel (private val repository: CoolboxApi) : ViewModel() {
    companion object {
        val TAG = PlinkViewModel::class.java.simpleName!!
        class Factory(application: Application, urlBase: String) : ViewModelProvider.NewInstanceFactory() {
            private var repository = (application as BasicApp).getApiRepository(urlBase)

            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return PlinkViewModel(repository) as T
            }
        }
    }
    var orderIdPagoLink = MutableLiveData<String>()
    var plinkLiveData = MutableLiveData<PaymentIntentionResponseEntity>()
    var paymentSuccessData = MutableLiveData<String>()
    var showProgress = MutableLiveData<Boolean>()
    var resultMessages = MutableLiveData<String>()
    var paymentCancel = MutableLiveData<String>()
    fun getPaymentData(data : PaymentIntentionsEntity, onError: (message: String) -> Unit) {
        showProgress.postValue(true)

        repository.getPagoLink(data).enqueue(object : Callback< PaymentIntentionResponseEntity> {
            override fun onResponse(
                call: Call<PaymentIntentionResponseEntity>,
                response: Response<PaymentIntentionResponseEntity>
            ) {
                val responseResult = response.body()!!

                if( response.isSuccessful && responseResult!!.success) {
                    plinkLiveData.postValue(responseResult!!)
                } else {
                    resultMessages.postValue(responseResult.msg)
                    onError(responseResult.msg)
                }
                showProgress.postValue(false)

            }

            override fun onFailure(
                call: Call<PaymentIntentionResponseEntity>,
                t: Throwable
            ) {
                Log.e(PaymentViewModel.TAG, t.message)
                resultMessages.postValue(t.message)
                showProgress.postValue(false)
                // Agregado CPV
                onError(t.message!!)
            }
        })
    }

    fun sendPaymentData() {
        /*showProgress.postValue(true)

        Handler(Looper.getMainLooper()).postDelayed({
            plinkLiveData.postValue(true)
            showProgress.postValue(false)
        },1500)*/
    }

    fun checkPaymentSuccess(data : PaymentIntentionResponseEntity, onError: (message: String) -> Unit) {

        repository.checkPaymentPLink(data).enqueue(object : Callback<PaymentIntentionResponseEntity> {
            override fun onResponse(
                call: Call<PaymentIntentionResponseEntity>,
                response: Response<PaymentIntentionResponseEntity>
            ) {
                val responseResult = response.body()!!

                if( response.isSuccessful && responseResult.success) {
                    when(responseResult!!.status!!) {
                        "PAID" -> {
                            orderIdPagoLink.postValue(data.orderId)
                            paymentSuccessData.postValue(responseResult.msg)
                        }
                        "EXPIRED","CANCELED","REFUSED" -> {
                            paymentCancel.postValue(responseResult.msg)
                            resultMessages.postValue(responseResult.msg)
                            onError(responseResult.msg)
                        }
                        else -> {
                            /*paymentCancel.postValue(true)
                            resultMessages.postValue(responseResult.msg)
                            onError(responseResult.msg)*/
                        }
                    }
                }else {
                    resultMessages.postValue(responseResult.msg)
                    onError(responseResult.msg)
                }
            }

            override fun onFailure(
                call: Call<PaymentIntentionResponseEntity>,
                t: Throwable
            ) {
                Log.e(PaymentViewModel.TAG, t.message)
                resultMessages.postValue(t.message)
                // Agregado CPV
                onError(t.message!!)
            }
        })
    }
}