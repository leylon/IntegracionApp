package com.pedidos.android.persistence.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.util.Log
import com.pedidos.android.persistence.api.ApiCorreo
import com.pedidos.android.persistence.api.CoolboxApi
import com.pedidos.android.persistence.db.entity.SaleEntity
import com.pedidos.android.persistence.model.SaleSubItem
import com.pedidos.android.persistence.model.sale.*
import com.pedidos.android.persistence.ui.BasicApp
import com.pedidos.android.persistence.utils.ApiWrapper
import com.pedidos.android.persistence.utils.ServicioGenerador
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class SaleViewModel(application: Application, private var repository: CoolboxApi) : AndroidViewModel(application) {

    var saleLiveData = MutableLiveData<SaleEntity>()
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

                                // cuando se borra un elemento se debe verificar cuando se borra

                                if(saleSubItem.cantidad == 0) {
                                    if(saleSubItem.codgaraexte == "") {
                                        //delete all
                                        saleLiveData.value!!.productos.removeAll { true }

                                        //agregamos el producto al pedido para mostrarlo
                                        currentEntity.productos.addAll(result?.productos ?: mutableListOf())
                                    } else {
                                        val tempListProducts = saleLiveData.value!!.productos.filter { it.codigoProducto != saleSubItem.codigoProducto }
                                        saleLiveData.value!!.productos.removeAll{true}
                                        currentEntity.productos.addAll( tempListProducts)
                                    }
                                } else {
                                    //delete all
                                    saleLiveData.value!!.productos.removeAll { true }

                                    //agregamos el producto al pedido para mostrarlo
                                    currentEntity.productos.addAll(result?.productos ?: mutableListOf())
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
    fun ventaProducto(
        request: List<VentaProductoRequest>,
        onSuccess: (ventaProducto: VentaProductoResponse) -> Unit,
        onError: (message: String) -> Unit
    ) {
        repository.ventaProducto(request).enqueue(
            object : Callback<VentaProductoResponse>{
                override fun onResponse(
                    call: Call<VentaProductoResponse>,
                    response: Response<VentaProductoResponse>
                ) {
                    showProgress.postValue(false)
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("error: code= ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<VentaProductoResponse>, t: Throwable) {
                   Log.e(GuideViewModel.TAG, "error at ${t.message}")
                    showProgress.postValue(false)
                    // Mensaje en pequeño de pantalla
                    message.postValue(t.message!!)
                    // Mensaje poppup
                    onError(t.message!!)
                }
            }
        )

    }
    fun ventaProducto(
        request: VentaProductoRequest,
        onSuccess: (ventaProducto: VentaProductoResponse) -> Unit,
        onError: (message: String) -> Unit
    ) {
        repository.ventaProducto(request).enqueue(
            object : Callback<VentaProductoResponse>{
                override fun onResponse(
                    call: Call<VentaProductoResponse>,
                    response: Response<VentaProductoResponse>
                ) {
                    showProgress.postValue(false)
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("error: code= ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<VentaProductoResponse>, t: Throwable) {
                    Log.e(GuideViewModel.TAG, "error at ${t.message}")
                    showProgress.postValue(false)
                    // Mensaje en pequeño de pantalla
                    message.postValue(t.message!!)
                    // Mensaje poppup
                    onError(t.message!!)
                }
            }
        )

    }
    fun ventaProductoEnvioCorreo(
        url: String,
        request: EnvioCorreoRequest,
        onSuccess: (ventaProducto: EnvioCorreoResponse,flag: Boolean) -> Unit,
        onError: (message: String) -> Unit
    ){
        val procesosService = ServicioGenerador.createService(
            ApiCorreo::class.java,
           url+"/"
        )
        procesosService.ventaProductoEnvioCorreo(request).enqueue(
            object : Callback<EnvioCorreoResponse>{
                override fun onResponse(
                    call: Call<EnvioCorreoResponse>,
                    response: Response<EnvioCorreoResponse>
                ) {
                    showProgress.postValue(false)
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it,true) }
                    } else {
                        onError("error: code= ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<EnvioCorreoResponse>, t: Throwable) {
                    Log.e(SaleViewModel.TAG, "error at ${t.message}")
                    showProgress.postValue(false)
                    // Mensaje en pequeño de pantalla
                    message.postValue(t.message!!)
                    // Mensaje poppup
                    onError(t.message!!)
                }
            }
        )
    }
    fun envioCodigoRespuesta(
        url: String,
        request: EnvioCodigoRequest,
        onSuccess: (entity: EnvioCodigoResponse) -> Unit,
        onError: (message: String) -> Unit
    ){
        val procesosService = ServicioGenerador.createService(
            ApiCorreo::class.java,
            url+"/"
        )
       /*
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .build()
        val procesosService = retrofit.create(ApiCorreo::class.java)*/
        procesosService.envioCodigoRespuesta(request).enqueue(
            object : Callback<EnvioCodigoResponse>{
                override fun onResponse(
                    call: Call<EnvioCodigoResponse>,
                    response: Response<EnvioCodigoResponse>
                ) {
                    showProgress.postValue(false)
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("error: code= ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<EnvioCodigoResponse>, t: Throwable) {
                    Log.e(SaleViewModel.TAG, "error at ${t.message}")
                    showProgress.postValue(false)
                    // Mensaje en pequeño de pantalla
                    message.postValue(t.message!!)
                    // Mensaje poppup
                    onError(t.message!!)
                }
            }
        )
    }
    fun ventaProductoValidaCodigo(
        request: VentaProductoValidaCodigoRequest,
        onSuccess: (entity: VentaProductoValidaCodigoResponse) -> Unit,
        onError: (message: String) -> Unit
    ){

        repository.ventaProductoValidaCodigo(request).enqueue(
            object : Callback<VentaProductoValidaCodigoResponse>{
                override fun onResponse(
                    call: Call<VentaProductoValidaCodigoResponse>,
                    response: Response<VentaProductoValidaCodigoResponse>
                ) {
                    showProgress.postValue(false)
                    if (response.isSuccessful) {
                        response.body()?.let { onSuccess(it) }
                    } else {
                        onError("error: code= ${response.code()}")
                    }
                }

                override fun onFailure(
                    call: Call<VentaProductoValidaCodigoResponse>,
                    t: Throwable
                ) {
                    Log.e(SaleViewModel.TAG, "error at ${t.message}")
                    showProgress.postValue(false)
                    // Mensaje en pequeño de pantalla
                    message.postValue(t.message!!)
                    // Mensaje poppup
                    onError(t.message!!)
                }
            }
        )
    }
    fun deleteItem(saleSubItem: SaleSubItem) {
        saleSubItem.cantidad = 0 //flag to delete

        saveDetail(saleSubItem)
    }

    companion object {
        val TAG = SaleViewModel::class.java.simpleName!!

        class Factory(private var application: Application, urlBase: String) : ViewModelProvider.NewInstanceFactory() {
            private var repository = (application as BasicApp).getApiRepository(urlBase)

            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return SaleViewModel(application, repository) as T
            }
        }
    }
}