package com.pedidos.android.persistence.viewmodel

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.util.Base64
import android.util.Log
import com.pedidos.android.persistence.api.CoolboxApi
import com.pedidos.android.persistence.db.entity.ReceiptEntity
import com.pedidos.android.persistence.db.entity.SaleEntity
import com.pedidos.android.persistence.model.ReceiptRequest
import com.pedidos.android.persistence.model.SelectedCreditCard
import com.pedidos.android.persistence.model.SelectedOtherPayment
import com.pedidos.android.persistence.ui.BasicApp
import com.pedidos.android.persistence.utils.ApiWrapper
import com.pedidos.android.persistence.utils.Defaults
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class EndingViewModel(private val repository: CoolboxApi) : ViewModel() {
    companion object {
        val TAG = EndingViewModel::class.java.simpleName!!

        class Factory(application: Application, urlBase: String) : ViewModelProvider.NewInstanceFactory() {
            private var repository = (application as BasicApp).getApiRepository(urlBase)

            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return EndingViewModel(repository) as T
            }
        }
    }

    var saleLiveData = MutableLiveData<SaleEntity>()
    var showProgress = MutableLiveData<Boolean>()
    var cardsAccepted = MutableLiveData<ArrayList<SelectedCreditCard>>()
    var otherPayments = MutableLiveData<ArrayList<SelectedOtherPayment>>()
    var receiptLiveData = MutableLiveData<ReceiptEntity>()
    var receiptQrLiveData = MutableLiveData<ReceiptEntity>()
    var errorMessages = MutableLiveData<String>()

    init {
        val newSale = SaleEntity().apply {
            clienteCodigo = Defaults.Cliente.documentNumber
            clienteNombres = Defaults.Cliente.fullName
        }

        saleLiveData.postValue(newSale)
        getCardsAccepted()
        getOtherPayments()
    }


    private fun getCardsAccepted() {
       showProgress.postValue(true)
        repository.cardsAvailable().enqueue(object : Callback<ApiWrapper<ArrayList<SelectedCreditCard>>>{
            override fun onResponse(
                call: Call<ApiWrapper<ArrayList<SelectedCreditCard>>>,
                response: Response<ApiWrapper<ArrayList<SelectedCreditCard>>>
            ) {
                if(response.isSuccessful && response.body()!!.result) {
                    cardsAccepted.value = response.body()?.data ?: arrayListOf()
                }
                showProgress.postValue(false)
            }

            override fun onFailure(
                call: Call<ApiWrapper<ArrayList<SelectedCreditCard>>>,
                t: Throwable
            ) {
                Log.e(TAG, t.message)
                showProgress.postValue(false)
            }

        })
    }

    private fun getOtherPayments() {
        showProgress.postValue(true)
        repository.otherPatments().enqueue(object : Callback<ApiWrapper<ArrayList<SelectedOtherPayment>>>{
            override fun onResponse(
                call: Call<ApiWrapper<ArrayList<SelectedOtherPayment>>>,
                response: Response<ApiWrapper<ArrayList<SelectedOtherPayment>>>
            ) {
                if(response.isSuccessful && response.body()!!.result) {
                    otherPayments.value = response.body()?.data ?: arrayListOf()
                }
                showProgress.postValue(false)
            }

            override fun onFailure(
                call: Call<ApiWrapper<ArrayList<SelectedOtherPayment>>>,
                t: Throwable
            ) {
                Log.e(TAG, t.message)
                showProgress.postValue(false)
            }

        })
    }

    fun getSaleReceipt(numeroDocumento: String) {
        showProgress.postValue(true)
        repository.getReceipt(ReceiptRequest(numeroDocumento)).enqueue(object : Callback<ApiWrapper<ReceiptEntity>> {
            override fun onFailure(call: Call<ApiWrapper<ReceiptEntity>>, t: Throwable) {
                Log.e(TAG, t.message)
            }

            override fun onResponse(call: Call<ApiWrapper<ReceiptEntity>>, response: Response<ApiWrapper<ReceiptEntity>>) {
                if (response.isSuccessful && response.body()!!.result) {
                    val receiptEntity = response.body()!!.data
                    if (receiptEntity != null) {
                        val data = Base64.decode(receiptEntity.documentoPrint, Base64.DEFAULT)
                        val dataQr = receiptEntity.qrbase64
                        receiptEntity.documentoPrint = String(data)
                        receiptEntity.qrbase64 = dataQr
                        receiptLiveData.postValue(receiptEntity)
                    } else {
                        errorMessages.postValue(response.body()!!.message)
                    }
                } else {
                    errorMessages.postValue("Error al obtener el documento")
                }

                showProgress.postValue(false)
            }

        })
    }

    fun getSaleReceiptPDF(numeroDocumento: String) {
        showProgress.postValue(true)
        repository.getReceiptPDF(ReceiptRequest(numeroDocumento)).enqueue(object : Callback<ApiWrapper<ReceiptEntity>> {
            override fun onFailure(call: Call<ApiWrapper<ReceiptEntity>>, t: Throwable) {
                Log.e(TAG, t.message)
            }

            override fun onResponse(call: Call<ApiWrapper<ReceiptEntity>>, response: Response<ApiWrapper<ReceiptEntity>>) {
                if (response.isSuccessful && response.body()!!.result) {
                    val receiptEntity = response.body()!!.data
                    if (receiptEntity != null) {
                        val data = Base64.decode(receiptEntity.documentoPrint, Base64.DEFAULT)

                        receiptEntity.pdfBytes = data//getPDFBytes(data)
                        receiptLiveData.postValue(receiptEntity)
                    } else {
                        errorMessages.postValue(response.body()!!.message)
                    }
                } else {
                    errorMessages.postValue("Error al obtener el documento")
                }

                showProgress.postValue(false)
            }

        })
    }

    //review
    private fun getPDFBytes(byteArray: ByteArray): ByteArray {
        val input = byteArray.inputStream()
        val bos = ByteArrayOutputStream()
        val b = ByteArray(1024)
        while ((input.read(b)) != -1) {
            bos.write(b, 0, input.read(b))
        }

        return bos.toByteArray()
    }

    fun eliminarPedido() {

    }

    fun cobrarPedido() {

    }
}