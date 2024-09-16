package com.pedidos.android.persistence.viewmodel

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.util.Log
import com.pedidos.android.persistence.api.CoolboxApi
import com.pedidos.android.persistence.db.entity.ProductEntity
import com.pedidos.android.persistence.model.CheckImeiResponse
import com.pedidos.android.persistence.ui.BasicApp
import com.pedidos.android.persistence.utils.ApiWrapper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SearchProductViewModel(private var repository: CoolboxApi) : ViewModel() {

    companion object {
        val TAG = SearchProductViewModel::class.java.simpleName!!
        class Factory(application: Application, urlBase: String) : ViewModelProvider.NewInstanceFactory() {
            private var repository = (application as BasicApp).getApiRepository(urlBase)

            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return SearchProductViewModel(repository) as T
            }
        }
    }

    val searchResults = MutableLiveData<ProductEntity>()
    val searchDescriptionResults = MutableLiveData<List<ProductEntity>>()
    val imeiHelperResults = MutableLiveData<ProductEntity>()
    val errorResults = MutableLiveData<String>()

    fun searchProduct(productID: String) {

        repository.searchProduct(productID).enqueue(object : Callback<ApiWrapper<ProductEntity>> {
            override fun onFailure(call: Call<ApiWrapper<ProductEntity>>, t: Throwable) {
                Log.e(TAG, t.message)
                errorResults.postValue(t.message)
            }

            override fun onResponse(call: Call<ApiWrapper<ProductEntity>>, response: Response<ApiWrapper<ProductEntity>>) {
                if (response.isSuccessful)
                    if (response.body()?.result == true && response.body()?.data != null) {
                        val productoEntity = response.body()?.data
                        productoEntity!!.codigoVenta = productID
                        searchResults.postValue(productoEntity)
                    } else
                        errorResults.postValue(response.body()?.message)
                else
                    errorResults.postValue(response.body()!!.message)
            }
        })
    }

    fun searchProductDescription(productID: String) {
        repository.searchProductDescription(productID).enqueue(object : Callback<ApiWrapper<List<ProductEntity>>> {


            override fun onFailure(call: Call<ApiWrapper<List<ProductEntity>>>, t: Throwable) {
                Log.e(TAG, t.message)
                errorResults.postValue(t.message)
            }

            override fun onResponse(
                call: Call<ApiWrapper<List<ProductEntity>>>,
                response: Response<ApiWrapper<List<ProductEntity>>>
            ) {
                if (response.isSuccessful)
                    if (response.body()?.result == true && response.body()?.data != null) {
                        val productosEntity = response.body()?.data
                        searchDescriptionResults.postValue(productosEntity)
                    } else
                        errorResults.postValue(response.body()?.message)
                else
                    errorResults.postValue(response.body()!!.message)
            }

        })
    }

    //check whether product requires IMEI2 or not
    fun checkAutomatically(it: ProductEntity) {
        repository.checkImei(it.codigoVenta, it.imei).enqueue(object : Callback<ApiWrapper<CheckImeiResponse>> {
            override fun onFailure(call: Call<ApiWrapper<CheckImeiResponse>>, t: Throwable) {
                errorResults.postValue(t.message)
            }

            override fun onResponse(call: Call<ApiWrapper<CheckImeiResponse>>, response: Response<ApiWrapper<CheckImeiResponse>>) {
                if (response.isSuccessful) {
                    if (response.body()?.data != null) {
                        it.stimei = false
                        searchResults.postValue(it)
                    } else
                        errorResults.postValue(response.body()?.message)
                } else
                    errorResults.postValue(response.body()!!.message)
            }
        })
    }
    fun checkAutomaticallyGuide(it: ProductEntity) {
        repository.checkImei(it.codigoVenta, it.imei).enqueue(object : Callback<ApiWrapper<CheckImeiResponse>> {
            override fun onFailure(call: Call<ApiWrapper<CheckImeiResponse>>, t: Throwable) {
                errorResults.postValue(t.message)
            }

            override fun onResponse(call: Call<ApiWrapper<CheckImeiResponse>>, response: Response<ApiWrapper<CheckImeiResponse>>) {
                if (response.isSuccessful) {
                    if (response.body()?.data != null) {
                        it.stimei = true
                        searchResults.postValue(it)
                    } else
                        errorResults.postValue(response.body()?.message)
                } else
                    errorResults.postValue(response.body()!!.message)
            }
        })
    }

    fun checkAutomaticallyForManualSearch(it: ProductEntity) {
        repository.checkImei(it.codigoVenta, it.imei).enqueue(object : Callback<ApiWrapper<CheckImeiResponse>> {
            override fun onFailure(call: Call<ApiWrapper<CheckImeiResponse>>, t: Throwable) {
                errorResults.postValue(t.message)
            }

            override fun onResponse(call: Call<ApiWrapper<CheckImeiResponse>>, response: Response<ApiWrapper<CheckImeiResponse>>) {
                if (response.isSuccessful) {
                    if (response.body()?.data != null) {
                        it.stimei = false
                        imeiHelperResults.postValue(it)
                    } else
                        errorResults.postValue(response.body()?.message)
                } else
                    errorResults.postValue(response.body()!!.message)
            }
        })
    }

    fun searchProductDirectly(productID: String) {
        repository.searchProduct(productID).enqueue(object : Callback<ApiWrapper<ProductEntity>> {
            override fun onFailure(call: Call<ApiWrapper<ProductEntity>>, t: Throwable) {
                errorResults.postValue(t.message)
            }

            override fun onResponse(call: Call<ApiWrapper<ProductEntity>>, response: Response<ApiWrapper<ProductEntity>>) {
                if (response.isSuccessful)
                    if (response.body()?.result == true && response.body()?.data != null) {
                        val productoEntity = response.body()?.data
                        productoEntity!!.codigoVenta = productID
                        searchResults.postValue(productoEntity)
                    } else
                        errorResults.postValue(response.body()?.message)
                else
                    errorResults.postValue(response.body()!!.message)
            }
        })
    }
}