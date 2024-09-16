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

class ComplementaryProductViewModel(application: Application, private var repository: CoolboxApi) : ViewModel() {

    var complementData = MutableLiveData<List<ProductComplementaryEntity>>()
    var showProgress = MutableLiveData<Boolean>()
    var message = MutableLiveData<String>()
    var emptyComplementDate = MutableLiveData<String>()
    var listDataComp: List<ProductComplementaryEntity> = mutableListOf()
    lateinit var changeStatusComp : ((list : List<ProductComplementaryEntity>) -> Unit)


    fun loadComplement(prodCode: String, prodPrice: String) {
        repository.getComplementaryProducts(prodCode).enqueue(object : Callback<ApiWrapper<List<ProductComplementaryEntity>>> {
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
                        listDataComp = response.body()!!.data!!
                        complementData.postValue(response.body()!!.data)
                    }
                } else {
                    Log.e(SaleViewModel.TAG, "result false ${response.body()!!.message}")
                    showProgress.postValue(false)
                    message.postValue(response.body()!!.message)
                }
            }
        })
    }

    fun getAllSelectedProducts() : List<ProductComplementaryEntity> {
        return listDataComp.filter { it.checkStatus == 1 }
    }

    fun changeCheckToProductComp(position : Int) {
        if (listDataComp[position].checkStatus == 1) {
            listDataComp[position].checkStatus = 0
        } else {
            listDataComp[position].checkStatus = 1
        }
        changeStatusComp.invoke(listDataComp)
    }


    companion object {
        val TAG = SaleViewModel::class.java.simpleName!!

        class Factory(private var application: Application, urlBase: String) : ViewModelProvider.NewInstanceFactory() {
            private var repository = (application as BasicApp).getApiRepository(urlBase)

            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return ComplementaryProductViewModel(application, repository) as T
            }
        }
    }
}