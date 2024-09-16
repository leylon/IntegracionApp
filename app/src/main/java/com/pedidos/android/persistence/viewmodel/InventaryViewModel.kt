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
import com.pedidos.android.persistence.model.inventary.InventaryGenerateRequest
import com.pedidos.android.persistence.model.inventary.InventaryRequest
import com.pedidos.android.persistence.model.inventary.InventaryResponse
import com.pedidos.android.persistence.model.inventary.InventaryResponseStatus
import com.pedidos.android.persistence.ui.BasicApp
import com.pedidos.android.persistence.utils.ApiWrapper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InventaryViewModel(application: Application, private var repository: CoolboxApi) : AndroidViewModel(application){
    var invetaryLiveData = MutableLiveData<SaleEntity>()
    //var guideLiveData = MutableLiveData<>
    val searchResults = MutableLiveData<ProductEntity>()
    var showProgress = MutableLiveData<Boolean>()
    var message = MutableLiveData<String>()
    val errorResults = MutableLiveData<String>()

    fun listInventary(inventaryRequest: InventaryRequest, onSuccess: (entity: InventaryResponse) -> Unit, onError: (message: String) -> Unit){
        repository.listaInventary(inventaryRequest).enqueue(object : Callback<InventaryResponse> {
            override fun onResponse(
                call: Call<InventaryResponse>,
                response: Response<InventaryResponse>
            ) {
                showProgress.postValue(false)
                if (response.isSuccessful) {
                    onSuccess(response.body()!!)
                } else {
                    onError("error: code= ${response.code()}")
                }
            }

            override fun onFailure(call: Call<InventaryResponse>, t: Throwable) {
                Log.e(InventaryViewModel.TAG, "error at ${t.message}")
                showProgress.postValue(false)
                // Mensaje en pequeño de pantalla
                message.postValue(t.message!!)
                // Mensaje poppup
                onError(t.message!!)
            }
        })
    }

    fun generateInventary(inventaryRequest: InventaryGenerateRequest, onSuccess: (entity: InventaryResponseStatus) -> Unit, onError: (message: String) -> Unit){
        repository.generateInventary(inventaryRequest).enqueue(object : Callback<InventaryResponseStatus> {
            override fun onResponse(
                call: Call<InventaryResponseStatus>,
                response: Response<InventaryResponseStatus>
            ) {
                showProgress.postValue(false)
                if (response.isSuccessful) {
                    onSuccess(response.body()!!)
                } else {
                    onError("error: code= ${response.code()}")
                }
            }

            override fun onFailure(call: Call<InventaryResponseStatus>, t: Throwable) {
                Log.e(InventaryViewModel.TAG, "error at ${t.message}")
                showProgress.postValue(false)
                // Mensaje en pequeño de pantalla
                message.postValue(t.message!!)
                // Mensaje poppup
                onError(t.message!!)
            }
        })
    }



    fun searchProductDirectly(productID: String, onSuccess: (entity: ProductEntity) -> Unit, onError: (message: String) -> Unit) {
        repository.searchProduct(productID).enqueue(object : Callback<ApiWrapper<ProductEntity>> {
            override fun onFailure(call: Call<ApiWrapper<ProductEntity>>, t: Throwable) {
                errorResults.postValue(t.message)
                Log.e(InventaryViewModel.TAG, "error at ${t.message}")
                showProgress.postValue(false)
                // Mensaje en pequeño de pantalla
                message.postValue(t.message!!)
                // Mensaje poppup
                onError(t.message!!)
            }

            override fun onResponse(call: Call<ApiWrapper<ProductEntity>>, response: Response<ApiWrapper<ProductEntity>>) {
                if (response.isSuccessful)
                    if (response.body()?.result == true && response.body()?.data != null) {
                        val productoEntity = response.body()?.data
                        productoEntity!!.codigoVenta = productID
                        searchResults.postValue(productoEntity)
                        onSuccess(productoEntity)
                    } else
                        errorResults.postValue(response.body()?.message)
                else
                    errorResults.postValue(response.body()!!.message)
            }
        })
    }
    companion object {
        val TAG = InventaryViewModel::class.java.simpleName!!

        class Factory(private var application: Application, urlBase: String) : ViewModelProvider.NewInstanceFactory() {
            private var repository = (application as BasicApp).getApiRepository(urlBase)

            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return InventaryViewModel(application, repository) as T
            }
        }
    }
}