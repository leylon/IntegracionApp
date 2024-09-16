package com.pedidos.android.persistence.viewmodel

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.util.Base64
import com.pedidos.android.persistence.api.CoolboxApi
import com.pedidos.android.persistence.db.entity.CancelSaleEntity
import com.pedidos.android.persistence.db.entity.CancelSaleResponseEntity
import com.pedidos.android.persistence.ui.BasicApp
import com.pedidos.android.persistence.utils.ApiWrapper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CancelViewModel(private val repository: CoolboxApi) : ViewModel() {
    companion object {
        val TAG = CancelViewModel::class.java.simpleName!!

        class Factory(application: Application, urlBase: String) : ViewModelProvider.NewInstanceFactory() {
            private var repository = (application as BasicApp).getApiRepository(urlBase)

            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return CancelViewModel(repository) as T
            }
        }
    }

    var showProgress = MutableLiveData<Boolean>()
    var errorMessages = MutableLiveData<String>()
    var cancelResult = MutableLiveData<CancelSaleResponseEntity>()


    fun anularPedido(data: CancelSaleEntity) {
        showProgress.postValue(true)
        repository.cancelSale(data).enqueue(object : Callback<ApiWrapper<CancelSaleResponseEntity>> {
            override fun onFailure(call: Call<ApiWrapper<CancelSaleResponseEntity>>, t: Throwable) {
                errorMessages.postValue(t.message)
                showProgress.postValue(false)
            }

            override fun onResponse(call: Call<ApiWrapper<CancelSaleResponseEntity>>, response: Response<ApiWrapper<CancelSaleResponseEntity>>) {
                val responseResult = response.body()!!.data

                if (response.isSuccessful && response.body()!!.result) {
                    val receipt = Base64.decode(responseResult!!.documentToPrint, Base64.DEFAULT)
                    responseResult.documentToPrint = String(receipt)

                    cancelResult.postValue(responseResult)
                } else {
                    errorMessages.postValue(response.body()!!.message)
                }
                showProgress.postValue(false)
            }

        })
    }
}