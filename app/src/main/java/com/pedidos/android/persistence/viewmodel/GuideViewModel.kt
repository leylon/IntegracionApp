package com.pedidos.android.persistence.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.util.Log
import com.pedidos.android.persistence.api.CoolboxApi
import com.pedidos.android.persistence.db.entity.SaleEntity
import com.pedidos.android.persistence.model.SaleSubItem
import com.pedidos.android.persistence.model.guide.*
import com.pedidos.android.persistence.ui.BasicApp
import com.pedidos.android.persistence.utils.ApiWrapper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GuideViewModel(application: Application, private var repository: CoolboxApi) : AndroidViewModel(application) {

    var saleLiveData = MutableLiveData<SaleEntity>()
    //var guideLiveData = MutableLiveData<>
    var showProgress = MutableLiveData<Boolean>()
    var message = MutableLiveData<String>()
    fun saveSale(onSuccess: (entity: SaleEntity) -> Unit, onError: (message: String) -> Unit) {
        //validations
        repository.insertSale(saleLiveData.value!!)
                .enqueue(object : Callback<ApiWrapper<SaleEntity>> {
                    override fun onResponse(call: Call<ApiWrapper<SaleEntity>>, response: Response<ApiWrapper<SaleEntity>>) {
                        if (response.isSuccessful) {
                            if (response.body()!!.result) {

                                saleLiveData.postValue(response.body()!!.data!!)
                                onSuccess(response.body()!!.data!!)

                            } else {
                                //Log.e(TAG, "error at ${response.body()!!.message}")
                                showProgress.postValue(false)
                                // Mensaje en pequeño de pantalla
                                message.postValue(response.body()!!.message)
                                // Mensaje poppup
                                onError(response.body()!!.message)



                            }
                        }
                    }

                    override fun onFailure(call: Call<ApiWrapper<SaleEntity>>, t: Throwable) {
                        Log.e(TAG, "error at ${t.message}")
                        showProgress.postValue(false)
                        // Mensaje en pequeño de pantalla
                        message.postValue(t.message!!)
                        // Mensaje poppup
                        onError(t.message!!)


                    }

                })
    }

    fun saveDetail(saleSubItem: SaleSubItem) {
        val entityToSave = SaleEntity(saleLiveData.value!!)
        entityToSave.productos = mutableListOf(saleSubItem)
        showProgress.postValue(true)
        repository.insertSaleSubItem(entityToSave)
                .enqueue(object : Callback<ApiWrapper<SaleEntity>> {
                    override fun onResponse(call: Call<ApiWrapper<SaleEntity>>, response: Response<ApiWrapper<SaleEntity>>) {
                        if (response.isSuccessful) {
                            if (response.body()!!.result) {
                                val result = response.body()!!.data
                                val currentEntity = SaleEntity(saleLiveData.value!!)

                                //actualizamos documento, total, subtotal, etc
                                currentEntity.documento = result?.documento ?: ""
                                currentEntity.subTotal = result?.subTotal ?: 0.0
                                currentEntity.descuento = result?.descuento ?: 0.0
                                currentEntity.impuesto = result?.impuesto ?: 0.0
                                currentEntity.total = result?.total ?: 0.0
                                currentEntity.monedaSimbolo = result?.monedaSimbolo ?: ""
                                currentEntity.complementaryRowColor = result?.complementaryRowColor ?: ""
                                currentEntity.productoconcomplemento = result?.productoconcomplemento ?: 0
                                var datas = ArrayList<SaleSubItem>()
                                /*for (product in result?.productos!!){
                                    if (product.codigoProducto == saleSubItem.codigoProducto){
                                        product.stimei = saleSubItem.stimei
                                    }
                                    datas.add(product)
                                    println("product : ${Gson().toJson(datas)}")
                                }*/

                                // cuando se borra un elemento se debe verificar cuando se borra

                                if(saleSubItem.cantidad == 0) {
                                    /*if(saleSubItem.codgaraexte == "") {
                                        //delete all
                                        saleLiveData.value!!.productos.removeAll { true }

                                        //agregamos el producto al pedido para mostrarlo
                                        //currentEntity.productos.addAll(result?.productos ?: mutableListOf())
                                        var datas = ArrayList<SaleSubItem>()

                                        datas = entityToSave.productos as ArrayList<SaleSubItem>
                                        currentEntity.productos.addAll(datas)
                                    } else {
                                        val tempListProducts = saleLiveData.value!!.productos.filter { it.codigoProducto != saleSubItem.codigoProducto }
                                        saleLiveData.value!!.productos.removeAll{true}
                                        currentEntity.productos.addAll( tempListProducts)
                                    }*/
                                    val tempListProducts = saleLiveData.value!!.productos.filter { it.codigoProducto != saleSubItem.codigoProducto }
                                    saleLiveData.value!!.productos.removeAll{true}
                                    currentEntity.productos.addAll( tempListProducts)
                                } else {
                                    //delete all
                                    saleLiveData.value!!.productos.removeAll { true }

                                    //agregamos el producto al pedido para mostrarlo
                                    var datas = ArrayList<SaleSubItem>()

                                    datas = entityToSave.productos as ArrayList<SaleSubItem>
                                    currentEntity.productos.addAll(datas)
                                }

                                saleLiveData.postValue(currentEntity)
                                showProgress.postValue(false)
                            } else {
                                Log.e(TAG, "result false ${response.body()!!.message}")
                                showProgress.postValue(false)
                                message.postValue(response.body()!!.message)
                            }
                        }
                    }

                    override fun onFailure(call: Call<ApiWrapper<SaleEntity>>, t: Throwable) {
                        Log.e(TAG, "fail request at ${t.message}")
                        showProgress.postValue(false)
                        message.postValue(t.message)
                    }
                })

    }

    fun deleteItem(saleSubItem: SaleSubItem) {
        saleSubItem.cantidad = 0 //flag to delete

        saveDetail(saleSubItem)
    }
    fun getListStorage(
        storageRequest: StorageRequest,
        onSuccess: (storage: List<StorageResponse>) -> Unit,
        onError: (message: String) -> Unit
    ) {
        repository.getStorageGr(storageRequest)
            .enqueue(object : Callback<List<StorageResponse>> {
                override fun onResponse(
                    call: Call<List<StorageResponse>>,
                    response: Response<List<StorageResponse>>
                ) {
                    showProgress.postValue(false)
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("error: code= ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<StorageResponse>>, t: Throwable) {
                    Log.e(TAG, "error at ${t.message}")
                    showProgress.postValue(false)
                    // Mensaje en pequeño de pantalla
                    message.postValue(t.message!!)
                    // Mensaje poppup
                    onError(t.message!!)

                }
            })

    }

    fun getTypeDocumentGuide(
        typeDocumentRequest: TypeDocumentRequest,
        onSuccess: (storage: List<DataResponse>) -> Unit,
        onError: (message: String) -> Unit
    ) {
        repository.getTypeDocumentGuide(typeDocumentRequest)
            .enqueue(object : Callback<List<DataResponse>> {
                override fun onResponse(
                    call: Call<List<DataResponse>>,
                    response: Response<List<DataResponse>>
                ) {
                    showProgress.postValue(false)
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("error: code= ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<DataResponse>>, t: Throwable) {
                    Log.e(TAG, "error at ${t.message}")
                    showProgress.postValue(false)
                    // Mensaje en pequeño de pantalla
                    message.postValue(t.message!!)
                    // Mensaje poppup
                    onError(t.message!!)
                }
            })
    }

    fun getOperationGuide(
        request: OperationGuideRequest,
        onSuccess: (storage: List<OperationGuideResponse>) -> Unit,
        onError: (message: String) -> Unit
    ) {
        repository.getOperationGuide(request)
            .enqueue(object : Callback<List<OperationGuideResponse>> {
                override fun onResponse(
                    call: Call<List<OperationGuideResponse>>,
                    response: Response<List<OperationGuideResponse>>
                ) {
                    showProgress.postValue(false)
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("error: code= ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<OperationGuideResponse>>, t: Throwable) {
                    Log.e(TAG, "error at ${t.message}")
                    showProgress.postValue(false)
                    // Mensaje en pequeño de pantalla
                    message.postValue(t.message!!)
                    // Mensaje poppup
                    onError(t.message!!)
                }
            })
    }

    fun getTypeAuxGuide(
        request: TypeAuxGuideRequest,
        onSuccess: (storage: List<DataResponse>) -> Unit,
        onError: (message: String) -> Unit
    ) {
        repository.getTypeAuxGuide(request)
            .enqueue(object : Callback<List<DataResponse>> {
                override fun onResponse(
                    call: Call<List<DataResponse>>,
                    response: Response<List<DataResponse>>
                ) {
                    showProgress.postValue(false)
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("error: code= ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<DataResponse>>, t: Throwable) {
                    Log.e(TAG, "error at ${t.message}")
                    showProgress.postValue(false)
                    // Mensaje en pequeño de pantalla
                    message.postValue(t.message!!)
                    // Mensaje poppup
                    onError(t.message!!)
                }
            })
    }

    fun getAuxGuide(
        request: AuxGuideRequest,
        onSuccess: (storage: List<DataResponse>) -> Unit,
        onError: (message: String) -> Unit
    ) {
        repository.getAuxGuide(request)
            .enqueue(object : Callback<List<DataResponse>> {
                override fun onResponse(
                    call: Call<List<DataResponse>>,
                    response: Response<List<DataResponse>>
                ) {
                    showProgress.postValue(false)
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("error: code= ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<DataResponse>>, t: Throwable) {
                    Log.e(TAG, "error at ${t.message}")
                    showProgress.postValue(false)
                    // Mensaje en pequeño de pantalla
                    message.postValue(t.message!!)
                    // Mensaje poppup
                    onError(t.message!!)
                }
            })
    }
    fun getUbigeo(
        request: UbigeoRequest,
        onSuccess: (storage: List<DataResponse>) -> Unit,
        onError: (message: String) -> Unit
    ) {
        repository.getUbigeo(request)
            .enqueue(object : Callback<List<DataResponse>> {
                override fun onResponse(
                    call: Call<List<DataResponse>>,
                    response: Response<List<DataResponse>>
                ) {
                    showProgress.postValue(false)
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("error: code= ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<DataResponse>>, t: Throwable) {
                    Log.e(TAG, "error at ${t.message}")
                    showProgress.postValue(false)
                    // Mensaje en pequeño de pantalla
                    message.postValue(t.message!!)
                    // Mensaje poppup
                    onError(t.message!!)
                }
            })
    }
    fun getTypeDocumentId(
        request: TypeDocumentRequest,
        onSuccess: (storage: List<DataResponse>) -> Unit,
        onError: (message: String) -> Unit
    ) {
        repository.getTypeDocumentId(request)
            .enqueue(object : Callback<List<DataResponse>> {
                override fun onResponse(
                    call: Call<List<DataResponse>>,
                    response: Response<List<DataResponse>>
                ) {
                    showProgress.postValue(false)
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("error: code= ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<DataResponse>>, t: Throwable) {
                    Log.e(TAG, "error at ${t.message}")
                    showProgress.postValue(false)
                    // Mensaje en pequeño de pantalla
                    message.postValue(t.message!!)
                    // Mensaje poppup
                    onError(t.message!!)
                }
            })
    }
    fun getDocumentIdGuide(
        request: DocumentIdRequest,
        onSuccess: (storage: List<DocumentIdResponse>) -> Unit,
        onError: (message: String) -> Unit
    ) {
        repository.getDocumentIdGuide(request)
            .enqueue(object : Callback<List<DocumentIdResponse>> {
                override fun onResponse(
                    call: Call<List<DocumentIdResponse>>,
                    response: Response<List<DocumentIdResponse>>
                ) {
                    showProgress.postValue(false)
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("error: code= ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<DocumentIdResponse>>, t: Throwable) {
                    Log.e(TAG, "error at ${t.message}")
                    showProgress.postValue(false)
                    // Mensaje en pequeño de pantalla
                    message.postValue(t.message!!)
                    // Mensaje poppup
                    onError(t.message!!)
                }
            })
    }
    fun getDocumentIdAllGuide(
        request: DocumentIdAllRequest,
        onSuccess: (storage: List<DocumentIdResponse>) -> Unit,
        onError: (message: String) -> Unit
    ) {
        repository.getDocumentIdAllGuide(request)
            .enqueue(object : Callback<List<DocumentIdResponse>> {
                override fun onResponse(
                    call: Call<List<DocumentIdResponse>>,
                    response: Response<List<DocumentIdResponse>>
                ) {
                    showProgress.postValue(false)
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("error: code= ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<DocumentIdResponse>>, t: Throwable) {
                    Log.e(TAG, "error at ${t.message}")
                    showProgress.postValue(false)
                    // Mensaje en pequeño de pantalla
                    message.postValue(t.message!!)
                    // Mensaje poppup
                    onError(t.message!!)
                }
            })
    }
    fun saveGuide(
        request: GuideRequest,
        onSuccess: (storage: GuideResponse) -> Unit,
        onError: (message: String) -> Unit
    ) {
        repository.saveGuide(request)
            .enqueue(object : Callback<GuideResponse> {
                override fun onResponse(
                    call: Call<GuideResponse>,
                    response: Response<GuideResponse>
                ) {
                    showProgress.postValue(false)
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("error: code= ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<GuideResponse>, t: Throwable) {
                    Log.e(TAG, "error at ${t.message}")
                    showProgress.postValue(false)
                    // Mensaje en pequeño de pantalla
                    message.postValue(t.message!!)
                    // Mensaje poppup
                    onError(t.message!!)
                }
            })
    }

    fun getDataTransport(
        request: DocumentTransportGuideRequest,
        onSuccess: (storage: List<DocumentTransportGuideResponse>) -> Unit,
        onError: (message: String) -> Unit
    ) {
        repository.getDataTranport(request)
            .enqueue(object : Callback<List<DocumentTransportGuideResponse>> {
                override fun onResponse(
                    call: Call<List<DocumentTransportGuideResponse>>,
                    response: Response<List<DocumentTransportGuideResponse>>
                ) {
                    showProgress.postValue(false)
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("error: code= ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<DocumentTransportGuideResponse>>, t: Throwable) {
                    Log.e(TAG, "error at ${t.message}")
                    showProgress.postValue(false)
                    // Mensaje en pequeño de pantalla
                    message.postValue(t.message!!)
                    // Mensaje poppup
                    onError(t.message!!)
                }
            })
    }

    fun getPlacaCarGuide(
        request: PlacaCarGuideRequest,
        onSuccess: (storage: List<DataResponse>) -> Unit,
        onError: (message: String) -> Unit
    ) {
        repository.getPlacaCarGuide(request)
            .enqueue(object : Callback<List<DataResponse>> {
                override fun onResponse(
                    call: Call<List<DataResponse>>,
                    response: Response<List<DataResponse>>
                ) {
                    showProgress.postValue(false)
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("error: code= ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<DataResponse>>, t: Throwable) {
                    Log.e(TAG, "error at ${t.message}")
                    showProgress.postValue(false)
                    // Mensaje en pequeño de pantalla
                    message.postValue(t.message!!)
                    // Mensaje poppup
                    onError(t.message!!)
                }
            })
    }
    companion object {
        val TAG = GuideViewModel::class.java.simpleName!!

        class Factory(private var application: Application, urlBase: String) : ViewModelProvider.NewInstanceFactory() {
            private var repository = (application as BasicApp).getApiRepository(urlBase)

            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return GuideViewModel(application, repository) as T
            }
        }
    }
}