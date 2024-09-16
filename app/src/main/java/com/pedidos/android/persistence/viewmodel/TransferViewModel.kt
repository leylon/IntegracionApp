package com.pedidos.android.persistence.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.util.Log
import com.pedidos.android.persistence.api.CoolboxApi
import com.pedidos.android.persistence.db.entity.ProductEntity
import com.pedidos.android.persistence.db.entity.SaleEntity
import com.pedidos.android.persistence.model.guide.GuideResponse
import com.pedidos.android.persistence.model.inventary.InventaryGenerateRequest
import com.pedidos.android.persistence.model.inventary.InventaryRequest
import com.pedidos.android.persistence.model.inventary.InventaryResponse
import com.pedidos.android.persistence.model.inventary.InventaryResponseStatus
import com.pedidos.android.persistence.model.transfer.*
import com.pedidos.android.persistence.ui.BasicApp
import com.pedidos.android.persistence.utils.ApiWrapper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TransferViewModel(application: Application, private var repository: CoolboxApi) : AndroidViewModel(application){
    var invetaryLiveData = MutableLiveData<SaleEntity>()
    //var guideLiveData = MutableLiveData<>
    val searchResults = MutableLiveData<ProductEntity>()
    var showProgress = MutableLiveData<Boolean>()
    var message = MutableLiveData<String>()
    val errorResults = MutableLiveData<String>()

    fun listProduct(transferRequest: TransferRequest, onSuccess: (entity: TransferResponse) -> Unit, onError: (message: String) -> Unit){
        repository.listaGuideTransfer(transferRequest).enqueue(object : Callback<TransferResponse> {
            override fun onResponse(
                call: Call<TransferResponse>,
                response: Response<TransferResponse>
            ) {
                showProgress.postValue(false)
                if (response.isSuccessful) {
                    onSuccess(response.body()!!)
                } else {
                    onError("error: code= ${response.code()}")
                }
            }

            override fun onFailure(call: Call<TransferResponse>, t: Throwable) {
                Log.e(TransferViewModel.TAG, "error at ${t.message}")
                showProgress.postValue(false)
                // Mensaje en pequeño de pantalla
                message.postValue(t.message!!)
                // Mensaje poppup
                onError(t.message!!)
            }
        })
    }

    fun showProductTransfer(transferRequest: TransferShowRequest, onSuccess: (entity: TransferShowResponse) -> Unit, onError: (message: String) -> Unit){
        repository.showGuideTranfer(transferRequest).enqueue(object : Callback<TransferShowResponse> {
            override fun onResponse(
                call: Call<TransferShowResponse>,
                response: Response<TransferShowResponse>
            ) {
                showProgress.postValue(false)
                if (response.isSuccessful) {
                    onSuccess(response.body()!!)
                } else {
                    onError("error: code= ${response.code()}")
                }
            }

            override fun onFailure(call: Call<TransferShowResponse>, t: Throwable) {
                Log.e(TransferViewModel.TAG, "error at ${t.message}")
                showProgress.postValue(false)
                // Mensaje en pequeño de pantalla
                message.postValue(t.message!!)
                // Mensaje poppup
                onError(t.message!!)
            }
        })
    }
    fun finishTransfer(transferRequest: TransferFinishRequest, onSuccess: (entity: GuideResponse) -> Unit, onError: (message: String) -> Unit){
        repository.confirmarGuideTranfer(transferRequest).enqueue(object : Callback<GuideResponse> {
            override fun onResponse(
                call: Call<GuideResponse>,
                response: Response<GuideResponse>
            ) {
                showProgress.postValue(false)
                if (response.isSuccessful) {
                    onSuccess(response.body()!!)
                } else {
                    onError("error: code= ${response.code()}")
                }
            }

            override fun onFailure(call: Call<GuideResponse>, t: Throwable) {
                Log.e(TransferViewModel.TAG, "error at ${t.message}")
                showProgress.postValue(false)
                // Mensaje en pequeño de pantalla
                message.postValue(t.message!!)
                // Mensaje poppup
                onError(t.message!!)
            }
        })
    }

    companion object {
        val TAG = TransferViewModel::class.java.simpleName!!

        class Factory(private var application: Application, urlBase: String) : ViewModelProvider.NewInstanceFactory() {
            private var repository = (application as BasicApp).getApiRepository(urlBase)

            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return TransferViewModel(application, repository) as T
            }
        }
    }
}