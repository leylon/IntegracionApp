package com.pedidos.android.persistence.viewmodel

import android.app.Application
import android.arch.lifecycle.*
import android.databinding.*
import android.util.Log
import com.pedidos.android.persistence.api.CoolboxApi
import com.pedidos.android.persistence.db.entity.ClientEntity
import com.pedidos.android.persistence.db.entity.ClientResponseEntity
import com.pedidos.android.persistence.model.SelectedTipoDocumento
import com.pedidos.android.persistence.model.TipoDocumento
import com.pedidos.android.persistence.ui.BasicApp
import com.pedidos.android.persistence.utils.ApiWrapper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.LinkedHashMap

class ClientViewModel private constructor(application: Application)
    : AndroidViewModel(application) {
    private var _liveData = MutableLiveData<ClientEntity>()
    private lateinit var _repository: CoolboxApi

    fun getObservableClient(): LiveData<ClientEntity> = _liveData

    //will be setted when observable change
    var client = ObservableField<ClientEntity>()
    var saved = MutableLiveData<Boolean>()
    var findResultSuccess = MutableLiveData<Boolean>()
    var typeInputText = MutableLiveData<String>()

    var showProgress = ObservableBoolean(false)
    var documentosIdentidad = ObservableArrayList<String>()
    var message = MutableLiveData<String>()

    var listTipoDocumento = MutableLiveData<ArrayList<SelectedTipoDocumento>>()
    val mapTipoDocumento = LinkedHashMap<Int, String>()
    constructor(application: Application, repository: CoolboxApi) : this(application) {
        _repository = repository
        getTipoDocumentoIdentidad()
        //documentosIdentidad.addAll(TipoDocumento.getAll().values.toList())
        documentosIdentidad.addAll(mapTipoDocumento.values.toList())
    }

    fun registerClient(entity: ClientEntity) {
        showProgress.set(true)
        _repository.insertClient(entity)
                .enqueue(object : Callback<ApiWrapper<ClientResponseEntity>> {

                    override fun onFailure(call: Call<ApiWrapper<ClientResponseEntity>>, t: Throwable) {
                        showProgress.set(false)
                        Log.e(TAG, t.message)
                    }

                    override fun onResponse(call: Call<ApiWrapper<ClientResponseEntity>>, response: Response<ApiWrapper<ClientResponseEntity>>) {
                        if (response.isSuccessful && response.body()!!.result) {
                            //preserve tipo documento
                            val previousTipoDocumento = _liveData.value?.identityDocumentType
                                    ?: TipoDocumento.DNI

                            val resultData = response.body()!!.data.apply {
                                this!!.identityDocument = previousTipoDocumento
                            }
                            _liveData.postValue(ClientEntity(resultData!!))
                            saved.postValue(true)
                        } else {
                            message.postValue(response.body()!!.message)

                        }
                        showProgress.set(false)
                    }

                })
    }

    fun findClient(codigo: String, documentType: Int) {
        showProgress.set(true)
        val call = _repository.getClients(codigo, documentType)
        call.enqueue(object : Callback<ApiWrapper<ClientResponseEntity>> {
            override fun onFailure(call: Call<ApiWrapper<ClientResponseEntity>>, t: Throwable) {
                Log.e(TAG, t.message)
                showProgress.set(false)
                findResultSuccess.postValue(false)
            }

            override fun onResponse(call: Call<ApiWrapper<ClientResponseEntity>>, response: Response<ApiWrapper<ClientResponseEntity>>) {

                if (response.isSuccessful && response.body()!!.result) {
                    val resultData = response.body()!!.data
                    _liveData.postValue(ClientEntity(resultData!!))
                    findResultSuccess.postValue(true)
                } else {
                    Log.e(TAG, "Cliente no existente")
                    message.postValue(response.body()!!.message)
                    val liveData = _liveData.value
                    if (liveData != null) {
                        liveData.fullName = ""
                        _liveData.postValue(liveData)
                    }
                    findResultSuccess.postValue(false)
                }
                showProgress.set(false)
            }
        })
    }

    fun findClientInRenienSunat(codigo: String, documentType: Int) {
        showProgress.set(true)
        val call = _repository.getClientsByReniecSunat(codigo, documentType)
        call.enqueue(object : Callback<ApiWrapper<ClientResponseEntity>> {
            override fun onFailure(call: Call<ApiWrapper<ClientResponseEntity>>, t: Throwable) {
                Log.e(TAG, t.message)
                showProgress.set(false)
                findResultSuccess.postValue(false)
            }

            override fun onResponse(call: Call<ApiWrapper<ClientResponseEntity>>, response: Response<ApiWrapper<ClientResponseEntity>>) {

                if (response.isSuccessful && response.body()!!.result) {
                    val resultData = response.body()!!.data
                    _liveData.postValue(ClientEntity(resultData!!))
                    findResultSuccess.postValue(true)
                } else {
                    Log.e(TAG, "Cliente no existente en reniec sunat ")
                    message.postValue(response.body()!!.message)
                    val liveData = _liveData.value
                    if (liveData != null) {
                        liveData.fullName = ""
                        _liveData.postValue(liveData)
                    }
                    findResultSuccess.postValue(false)
                }
                showProgress.set(false)
            }
        })
    }

    fun cleanAndSetTipoDocumento(tipoDocumento: Int) {
        if (_liveData.value?.identityDocumentType ?: TipoDocumento.DNI != tipoDocumento) {
            _liveData.postValue(ClientEntity().apply {
                identityDocumentType = tipoDocumento
            })
        }
    }

    private fun getTipoDocumentoIdentidad() {
        showProgress.set(true)
        _repository.tipoDocumentIdentidad().enqueue(object : Callback<ApiWrapper<ArrayList<SelectedTipoDocumento>>>{
            override fun onResponse(
                call: Call<ApiWrapper<ArrayList<SelectedTipoDocumento>>>,
                response: Response<ApiWrapper<ArrayList<SelectedTipoDocumento>>>
            ) {
                if(response.isSuccessful && response.body()!!.result) {
                    listTipoDocumento.value = response.body()?.data ?: arrayListOf()
                   // val mapTipoDocumento = LinkedHashMap<Int, String>()
                    for (list in listTipoDocumento.value!!){
                        mapTipoDocumento[list.codigo]= list.description
                    }
                    documentosIdentidad.addAll(mapTipoDocumento.values.toList())
                }
                showProgress.set(false)
            }

            override fun onFailure(
                call: Call<ApiWrapper<ArrayList<SelectedTipoDocumento>>>,
                t: Throwable
            ) {
                Log.e(EndingViewModel.TAG, t.message)
                showProgress.set(false)
            }

        })
    }

    companion object {
        val TAG = ClientViewModel::class.java.simpleName!!

        class Factory(private var application: Application, urlBase: String) : ViewModelProvider.NewInstanceFactory() {
            private var repository = (application as BasicApp).getApiRepository(urlBase)

            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return ClientViewModel(application, repository) as T
            }
        }
    }
}