package com.pedidos.android.persistence.viewmodel

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.util.Base64
import android.util.Log
import com.pedidos.android.persistence.api.CoolboxApi
import com.pedidos.android.persistence.db.entity.CashBalanceEntity
import com.pedidos.android.persistence.db.entity.CashBalanceResponseEntity
import com.pedidos.android.persistence.ui.BasicApp
import com.pedidos.android.persistence.utils.ApiWrapper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CashBalanceViewModel(private val repository: CoolboxApi) : ViewModel() {
    companion object {
        val TAG = CashBalanceViewModel::class.java.simpleName!!

        class Factory(application: Application, urlBase: String) : ViewModelProvider.NewInstanceFactory() {
            private var repository = (application as BasicApp).getApiRepository(urlBase)

            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return CashBalanceViewModel(repository) as T
            }
        }
    }

    var showProgress = MutableLiveData<Boolean>()
    var errorMessages = MutableLiveData<String>()
    var cashBalanceResult = MutableLiveData<CashBalanceResponseEntity>()

    fun generateReport(userName: String, date: String) {
        showProgress.postValue(true)
        repository.cashBalance(CashBalanceEntity().apply {
            username = userName
            this.date = date
        }).enqueue(object : Callback<ApiWrapper<CashBalanceResponseEntity>> {
            override fun onFailure(call: Call<ApiWrapper<CashBalanceResponseEntity>>, t: Throwable) {
                Log.e(TAG, t.message)
                showProgress.postValue(false)
            }

            override fun onResponse(call: Call<ApiWrapper<CashBalanceResponseEntity>>, response: Response<ApiWrapper<CashBalanceResponseEntity>>) {
                if (response.isSuccessful && response.body()!!.result) {
                    val report = response.body()!!.data
                    if (report != null) {
                        val data = Base64.decode(report.result, Base64.DEFAULT)
                        report.result = String(data)
                        cashBalanceResult.postValue(report)
                    }
                    else {
                        errorMessages.postValue(response.body()!!.message)
                    }
                } else {
                    errorMessages.postValue("Error al obtener el documento")
                }

                showProgress.postValue(false)
            }
        })
    }
}