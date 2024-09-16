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
import com.pedidos.android.persistence.model.picking.*
import com.pedidos.android.persistence.model.transfer.*
import com.pedidos.android.persistence.ui.BasicApp
import com.pedidos.android.persistence.utils.ApiWrapper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PackingViewModel(application: Application, private var repository: CoolboxApi) : AndroidViewModel(application){
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

    fun listPedidos(transferRequest: PrincipalPedidoRequest, onSuccess: (entity: PrincipalPedidosPicking) -> Unit, onError: (message: String) -> Unit){
        repository.obtenerPedidoPacking(transferRequest).enqueue(object : Callback<PrincipalPedidosPicking> {
            override fun onResponse(
                call: Call<PrincipalPedidosPicking>,
                response: Response<PrincipalPedidosPicking>
            ) {
                showProgress.postValue(false)
                if (response.isSuccessful) {
                    onSuccess(response.body()!!)
                } else {
                    onError("error: code= ${response.code()}")
                }
            }

            override fun onFailure(call: Call<PrincipalPedidosPicking>, t: Throwable) {
                Log.e(TransferViewModel.TAG, "error at ${t.message}")
                showProgress.postValue(false)
                // Mensaje en pequeño de pantalla
                message.postValue(t.message!!)
                // Mensaje poppup
                onError(t.message!!)
            }
        })
    }
    fun showPedidos(transferRequest: PedidoRequest, onSuccess: (entity: PedidoDetail) -> Unit, onError: (message: String) -> Unit){
        repository.obtenerPedido(transferRequest).enqueue(object : Callback<PedidoDetail> {
            override fun onResponse(
                call: Call<PedidoDetail>,
                response: Response<PedidoDetail>
            ) {
                showProgress.postValue(false)
                if (response.isSuccessful) {
                    if (response.body()?.result == true){
                        onSuccess(response.body()!!)
                    }else {
                        onError("${response.body()?.message}")
                    }

                } else {
                    onError("error: code= ${response.code()}")
                }
            }

            override fun onFailure(call: Call<PedidoDetail>, t: Throwable) {
                Log.e(TransferViewModel.TAG, "error at ${t.message}")
                showProgress.postValue(false)
                // Mensaje en pequeño de pantalla
                message.postValue(t.message!!)
                // Mensaje poppup
                onError(t.message!!)
            }
        })
    }
    fun extornarPacking(transferRequest: PickingExtornoRequest, onSuccess: (entity: PickingExtornoTerminarResponse) -> Unit, onError: (message: String) -> Unit){
        repository.pedidoExtorno(transferRequest).enqueue(object : Callback<PickingExtornoTerminarResponse> {
            override fun onResponse(
                call: Call<PickingExtornoTerminarResponse>,
                response: Response<PickingExtornoTerminarResponse>
            ) {
                showProgress.postValue(false)
                if (response.isSuccessful) {
                    onSuccess(response.body()!!)
                } else {
                    onError("error: code= ${response.code()}")
                }
            }

            override fun onFailure(call: Call<PickingExtornoTerminarResponse>, t: Throwable) {
                Log.e(TransferViewModel.TAG, "error at ${t.message}")
                showProgress.postValue(false)
                // Mensaje en pequeño de pantalla
                message.postValue(t.message!!)
                // Mensaje poppup
                onError(t.message!!)
            }
        })
    }


    fun pickingTerminar(transferRequest: PickingTerminarRequest, onSuccess: (entity: PickingTerminarResponse) -> Unit, onError: (message: String) -> Unit){
        repository.pickingTerminar(transferRequest).enqueue(object : Callback<PickingTerminarResponse> {
            override fun onResponse(
                call: Call<PickingTerminarResponse>,
                response: Response<PickingTerminarResponse>
            ) {
                showProgress.postValue(false)
                if (response.isSuccessful) {
                    onSuccess(response.body()!!)
                } else {
                    onError("error: code= ${response.code()}")
                }
            }

            override fun onFailure(call: Call<PickingTerminarResponse>, t: Throwable) {
                Log.e(TransferViewModel.TAG, "error at ${t.message}")
                showProgress.postValue(false)
                // Mensaje en pequeño de pantalla
                message.postValue(t.message!!)
                // Mensaje poppup
                onError(t.message!!)
            }
        })
    }

    fun picking(transferRequest: PickingRequest, onSuccess: (entity: PedidoDetail) -> Unit, onError: (message: String) -> Unit){
        repository.pickado(transferRequest).enqueue(object : Callback<PedidoDetail> {
            override fun onResponse(
                call: Call<PedidoDetail>,
                response: Response<PedidoDetail>
            ) {
                showProgress.postValue(false)
                if (response.isSuccessful) {
                    onSuccess(response.body()!!)
                } else {
                    onError("error: code= ${response.code()}")
                }
            }

            override fun onFailure(call: Call<PedidoDetail>, t: Throwable) {
                Log.e(TransferViewModel.TAG, "error at ${t.message}")
                showProgress.postValue(false)
                // Mensaje en pequeño de pantalla
                message.postValue(t.message!!)
                // Mensaje poppup
                onError(t.message!!)
            }
        })
    }
    fun ReportPicking(transferRequest: PedidoRequest, onSuccess: (entity: ReportPickingResponse) -> Unit, onError: (message: String) -> Unit){
        repository.reportPickado(transferRequest).enqueue(object : Callback<ReportPickingResponse> {
            override fun onResponse(
                call: Call<ReportPickingResponse>,
                response: Response<ReportPickingResponse>
            ) {
                showProgress.postValue(false)
                if (response.isSuccessful) {
                    onSuccess(response.body()!!)
                } else {
                    onError("error: code= ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ReportPickingResponse>, t: Throwable) {
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
        val TAG = PackingViewModel::class.java.simpleName!!

        class Factory(private var application: Application, urlBase: String) : ViewModelProvider.NewInstanceFactory() {
            private var repository = (application as BasicApp).getApiRepository(urlBase)

            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return PackingViewModel(application, repository) as T
            }
        }
    }
}