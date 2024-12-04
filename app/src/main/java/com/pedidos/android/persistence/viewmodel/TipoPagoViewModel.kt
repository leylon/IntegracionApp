package com.pedidos.android.persistence.viewmodel

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.pedidos.android.persistence.api.ApiExternas
import com.pedidos.android.persistence.api.CoolboxApi
import com.pedidos.android.persistence.model.pagos.PaymentNcrRequest
import com.pedidos.android.persistence.model.pagos.PaymentNcrResponse
import com.pedidos.android.persistence.model.pagos.PaymentValeRequest
import com.pedidos.android.persistence.model.pagos.PaymentValeResponse
import com.pedidos.android.persistence.ui.BasicApp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TipoPagoViewModel(private val repository: ApiExternas) : ViewModel() {
    companion object {
        val TAG = CashBalanceViewModel::class.java.simpleName!!

        class Factory(application: Application, urlBase: String) :
            ViewModelProvider.NewInstanceFactory() {
            private var repository = (application as BasicApp).getApiRepositoryExterno(urlBase)

            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return TipoPagoViewModel(repository) as T
            }
        }
    }

    var showProgress = MutableLiveData<Boolean>()
    var errorMessages = MutableLiveData<String>()
    var valeResult = MutableLiveData<PaymentValeResponse>()
    var ncrResult = MutableLiveData<PaymentNcrResponse>()

    fun getValesCard(url: String, paymentValeRequest: PaymentValeRequest) {
        showProgress.postValue(true)
        repository.pagovale(url, paymentValeRequest)
            .enqueue(object : Callback<PaymentValeResponse> {
                override fun onResponse(
                    call: Call<PaymentValeResponse>, response: Response<PaymentValeResponse>
                ) {
                    if (response.isSuccessful && response.body()!!.result) {
                        valeResult.postValue(response.body())
                    } else {
                        errorMessages.postValue(response.body()!!.message)
                    }
                }

                override fun onFailure(call: Call<PaymentValeResponse>, t: Throwable) {
                    errorMessages.postValue(t.message)
                }
            })
    }

    fun getNcrCard(url: String, paymentValeRequest: PaymentNcrRequest) {
        showProgress.postValue(true)
        repository.pagoNcr(url, paymentValeRequest)
            .enqueue(object : Callback<PaymentNcrResponse> {
                override fun onResponse(
                    call: Call<PaymentNcrResponse>, response: Response<PaymentNcrResponse>
                ) {
                    if (response.isSuccessful ) {
                        ncrResult.postValue(response.body())
                    } else {
                        errorMessages.postValue(response.message())
                    }
                }

                override fun onFailure(call: Call<PaymentNcrResponse>, t: Throwable) {
                    errorMessages.postValue(t.message)
                }
            })
    }

}