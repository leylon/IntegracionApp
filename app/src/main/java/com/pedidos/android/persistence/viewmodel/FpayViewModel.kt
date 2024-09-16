package com.pedidos.android.persistence.viewmodel

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.util.Log
import com.pedidos.android.persistence.api.CoolboxApi
import com.pedidos.android.persistence.db.entity.PaymentIntentionResponseEntity
import com.pedidos.android.persistence.db.entity.PaymentIntentionsEntity
import com.pedidos.android.persistence.ui.BasicApp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FpayViewModel (private val repository: CoolboxApi) : ViewModel() {
    companion object {
        val TAG = FpayViewModel::class.java.simpleName!!
        class Factory(application: Application, urlBase: String) : ViewModelProvider.NewInstanceFactory() {
            private var repository = (application as BasicApp).getApiRepository(urlBase)

            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return FpayViewModel(repository) as T
            }
        }
    }

    var fpayLiveData = MutableLiveData<PaymentIntentionResponseEntity>()
    var paymentSuccessData = MutableLiveData<String>()
    var showProgress = MutableLiveData<Boolean>()
    var resultMessages = MutableLiveData<String>()
    var paymentCancel = MutableLiveData<String>()
    var reverseSuccess = MutableLiveData<String>()
    var orderIdFpay = MutableLiveData<String>()
    var paymentSuccessInfo : PaymentIntentionResponseEntity ?= null
    fun getPaymentData(data : PaymentIntentionsEntity, onError: (message: String) -> Unit) {
        showProgress.postValue(true)

        repository.getFpay(data).enqueue(object : Callback<PaymentIntentionResponseEntity> {
            override fun onResponse(
                call: Call<PaymentIntentionResponseEntity>,
                response: Response<PaymentIntentionResponseEntity>
            ) {
                val responseResult = response.body()!!
                paymentSuccessInfo = responseResult
                if( response.isSuccessful && responseResult!!.success) {
                    fpayLiveData.postValue(responseResult!!)
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

    fun checkPaymentSuccess(data : PaymentIntentionResponseEntity, onError: (message: String) -> Unit) {

        repository.checkPaymentFpay(data).enqueue(object :
            Callback<PaymentIntentionResponseEntity> {
            override fun onResponse(
                call: Call<PaymentIntentionResponseEntity>,
                response: Response<PaymentIntentionResponseEntity>
            ) {
                val responseResult = response.body()!!

                if( response.isSuccessful && responseResult.success) {
                    when(responseResult!!.status!!) {
                        "PAGADO" -> {
                            orderIdFpay.postValue(data.pagoId)
                            paymentSuccessData.postValue(responseResult.msg)
                        }
                        "" -> {
                        }
                        else -> {
                            paymentCancel.postValue(responseResult.msg)
                            resultMessages.postValue(responseResult.msg)
                            onError(responseResult.msg)
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

    fun reversePayment(onError: (message: String) -> Unit) {
        showProgress.postValue(true)

        repository.reverseFpayPayment(paymentSuccessInfo!!).enqueue(object : Callback<PaymentIntentionResponseEntity> {
            override fun onResponse(
                call: Call<PaymentIntentionResponseEntity>,
                response: Response<PaymentIntentionResponseEntity>
            ) {
                val responseResult = response.body()!!

                if( responseResult.status == "200") {
                    reverseSuccess.postValue(responseResult.msg)
                } else if ( responseResult.status == "400") {
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
}