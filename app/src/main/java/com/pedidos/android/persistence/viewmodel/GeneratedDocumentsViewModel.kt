package com.pedidos.android.persistence.viewmodel

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.util.Base64
import com.pedidos.android.persistence.api.CoolboxApi
import com.pedidos.android.persistence.db.entity.CashBalanceEntity
import com.pedidos.android.persistence.db.entity.GeneratedDocumentEntity
import com.pedidos.android.persistence.db.entity.ReceiptEntity
import com.pedidos.android.persistence.model.ReceiptRequest
import com.pedidos.android.persistence.ui.BasicApp
import com.pedidos.android.persistence.utils.ApiWrapper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GeneratedDocumentsViewModel(private val repository: CoolboxApi) : ViewModel() {
    companion object {
        val TAG = GeneratedDocumentsViewModel::class.java.simpleName!!

        class Factory(application: Application, urlBase: String) : ViewModelProvider.NewInstanceFactory() {
            private var repository = (application as BasicApp).getApiRepository(urlBase)

            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return GeneratedDocumentsViewModel(repository) as T
            }
        }
    }

    var showProgress = MutableLiveData<Boolean>()
    var errorMessages = MutableLiveData<String>()

    var operationReportResult = MutableLiveData<List<GeneratedDocumentEntity>>()
    var receiptResult = MutableLiveData<ReceiptEntity>()

    fun getGeneratedDocuments(data: CashBalanceEntity) {
        repository.generatedDocuments(data).enqueue(object : Callback<ApiWrapper<List<GeneratedDocumentEntity>>> {
            override fun onFailure(call: Call<ApiWrapper<List<GeneratedDocumentEntity>>>, t: Throwable) {
                errorMessages.postValue(t.message)
                showProgress.postValue(false)
            }

            override fun onResponse(call: Call<ApiWrapper<List<GeneratedDocumentEntity>>>, response: Response<ApiWrapper<List<GeneratedDocumentEntity>>>) {
                val responseResult = response.body()!!

                if (response.isSuccessful && responseResult.result) {
                    operationReportResult.postValue(responseResult.data)
                } else {
                    if (!responseResult.message.isNullOrEmpty()) //todo: apply to every Null assertion
                        errorMessages.postValue(responseResult.message)
                    operationReportResult.postValue(listOf())
                }

                showProgress.postValue(false)
            }
        })
    }

    fun getDocumentToPrint(documentType: String, documentNumber: String) {
        repository.getReceiptForReprint(ReceiptRequest(documentNumber, documentType))
                .enqueue(object : Callback<ApiWrapper<ReceiptEntity>> {
                    override fun onFailure(call: Call<ApiWrapper<ReceiptEntity>>, t: Throwable) {
                        errorMessages.postValue(t.message)
                        showProgress.postValue(false)
                    }

                    override fun onResponse(call: Call<ApiWrapper<ReceiptEntity>>, response: Response<ApiWrapper<ReceiptEntity>>) {
                        val receiptEntity = response.body()!!.data
                        if (receiptEntity != null) {
                            val data = Base64.decode(receiptEntity.documentoPrint, Base64.DEFAULT)
                            receiptEntity.documentoPrint = String(data)
                            receiptResult.postValue(receiptEntity)
                        } else {
                            errorMessages.postValue(response.body()!!.message)
                        }

                        showProgress.postValue(false)
                    }
                })
    }
}