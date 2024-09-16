package com.pedidos.android.persistence.viewmodel

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.util.Log
import com.pedidos.android.persistence.api.CoolboxApi
import com.pedidos.android.persistence.db.entity.ProductComplementaryEntity
import com.pedidos.android.persistence.ui.BasicApp
import com.pedidos.android.persistence.utils.ApiWrapper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GarantiesProductViewModel (application: Application, private var repository: CoolboxApi) : ViewModel() {

    lateinit var changeStatusExt : ((list : List<ProductComplementaryEntity>) -> Unit)
    lateinit var changeStatusDamage : ((list : List<ProductComplementaryEntity>) -> Unit)
    var complementDataExt = MutableLiveData<List<ProductComplementaryEntity>>()
    var complementDataDamage = MutableLiveData<List<ProductComplementaryEntity>>()
    var listDataExt : List<ProductComplementaryEntity> = mutableListOf()
    var listDataDamage : List<ProductComplementaryEntity> = mutableListOf()
    var dataExtSelected : ProductComplementaryEntity ?= null
    var dataDamageSelected : ProductComplementaryEntity ?= null
    var showProgress = MutableLiveData<Boolean>()
    var message = MutableLiveData<String>()
    var emptyComplementDate = MutableLiveData<String>()


    fun loadComplement(prodCode: String, prodPrice: String) {
        repository.getProductsGarantie(prodCode,prodPrice).enqueue(object :
            Callback<ApiWrapper<List<ProductComplementaryEntity>>> {
            override fun onFailure(call: Call<ApiWrapper<List<ProductComplementaryEntity>>>, t: Throwable) {
                Log.e(SaleViewModel.TAG, "fail request at ${t.message}")
                showProgress.postValue(false)
                message.postValue(t.message)
            }

            override fun onResponse(call: Call<ApiWrapper<List<ProductComplementaryEntity>>>, response: Response<ApiWrapper<List<ProductComplementaryEntity>>>) {
                if (response.isSuccessful) {
                    if(!response.body()!!.result){
                        emptyComplementDate.postValue(response.body()!!.message)
                    } else {
                        listDataExt = response.body()!!.data!!.filter { it.type == 1 }
                        complementDataExt.postValue(listDataExt)
                        listDataDamage = response.body()!!.data!!.filter { it.type == 2 }
                        complementDataDamage.postValue(listDataDamage)
                    }
                } else {
                    Log.e(SaleViewModel.TAG, "result false ${response.body()!!.message}")
                    showProgress.postValue(false)
                    message.postValue(response.body()!!.message)
                }
            }
        })
    }

    fun changeCheckToProductDamage( position : Int) {
        var flag = false
        listDataDamage.forEachIndexed { index, productComplementaryEntity ->
            if(productComplementaryEntity.checkStatus == 1 && index == position) {
                flag = true
            }
            productComplementaryEntity.checkStatus = 0
        }
        if(!flag){
            listDataDamage[position].checkStatus = 1
            dataDamageSelected = listDataDamage[position]
        } else {
            dataDamageSelected = null
        }
        changeStatusDamage.invoke(listDataDamage)
    }
    fun changeCheckToProductExt(position : Int) {
        var flag = false
        listDataExt.forEachIndexed { index, productComplementaryEntity ->
            if(productComplementaryEntity.checkStatus == 1 && index == position) {
                flag = true
            }
            productComplementaryEntity.checkStatus = 0
        }
        if(!flag) {
            listDataExt[position].checkStatus = 1
            dataExtSelected = listDataExt[position]
        } else {
            dataExtSelected = null
        }
        changeStatusExt.invoke(listDataExt)
    }

    companion object {
        val TAG = SaleViewModel::class.java.simpleName!!

        class Factory(private var application: Application, urlBase: String) : ViewModelProvider.NewInstanceFactory() {
            private var repository = (application as BasicApp).getApiRepository(urlBase)

            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return GarantiesProductViewModel(application, repository) as T
            }
        }
    }
}