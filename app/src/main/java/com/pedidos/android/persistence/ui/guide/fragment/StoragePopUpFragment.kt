package com.pedidos.android.persistence.ui.guide.fragment


import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.google.gson.Gson
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.model.guide.StorageRequest
import com.pedidos.android.persistence.model.guide.StorageResponse

class StoragePopUpFragment: DialogFragment, OnClickListenerCustom<StorageResponse> {

    var etwSearchDialog: EditText? = null
    var rvwDataSearch: RecyclerView? = null
    var storageAdapter: StorageAdapter? = null
    var listStorage: List<StorageResponse>? = ArrayList()
    constructor()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.search_custom_dialog, container)
        etwSearchDialog = view.findViewById(R.id.etwSearchDialog)
        rvwDataSearch = view.findViewById(R.id.rvwDataSearch)
        listStorage = arguments?.get("DataList") as List<StorageResponse>
        val linearLayoutManager = LinearLayoutManager(context)
        rvwDataSearch?.layoutManager = linearLayoutManager
        storageAdapter = StorageAdapter()
        storageAdapter?.setDataStorage(listStorage)
        rvwDataSearch?.adapter = storageAdapter
        storageAdapter?.setOnItemClickListener(this@StoragePopUpFragment)

        etwSearchDialog?.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val datafilter: MutableList<StorageResponse> = ArrayList()
                println("cantidad: ${listStorage?.size}")
                for (storage in listStorage!!){
                    if (storage.codigo ?.uppercase()!!.contains(s?.toString()!!.trim().uppercase()) ) {
                        println("data_bad: ${Gson().toJson(storage.descripcion.toString().length)}")
                        /*if (storage.codigo ?.uppercase()!!.contains(s?.toString()!!.trim().uppercase()) || storage.descripcion?.uppercase()!!.contains(s?.toString()!!.trim().uppercase())){
                            datafilte.add(storage)
                        }*/
                        datafilter.add(storage)
                    }
                }
                val linearLayoutManager = LinearLayoutManager(context)
                rvwDataSearch?.layoutManager = linearLayoutManager
                storageAdapter = StorageAdapter()
                storageAdapter?.setDataStorage(datafilter)
                rvwDataSearch?.adapter = storageAdapter
                storageAdapter?.setOnItemClickListener(this@StoragePopUpFragment)
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        return view
    }

    override fun onClickDataListener(objectData: StorageResponse) {
        val activity = activity as newDialoglistenerStorage

        if (objectData.codigo != "NO"){
            activity.closeDialogStorage(objectData)
            this.dismiss()
        }

    }
    interface newDialoglistenerStorage {
        fun closeDialogStorage(data : StorageResponse)
    }
}