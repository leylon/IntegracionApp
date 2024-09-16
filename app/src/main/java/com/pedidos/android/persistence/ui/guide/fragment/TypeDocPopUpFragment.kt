package com.pedidos.android.persistence.ui.guide.fragment

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import com.google.zxing.integration.android.IntentIntegrator
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.databinding.ClientPopupFragmentBinding
import com.pedidos.android.persistence.db.entity.ClientEntity
import com.pedidos.android.persistence.model.Client
import com.pedidos.android.persistence.model.LoginResponse
import com.pedidos.android.persistence.model.TipoDocumento
import com.pedidos.android.persistence.model.guide.DataResponse


class TypeDocPopUpFragment : DialogFragment, OnClickListenerCustom<DataResponse>  {

    var etwSearchDialog: EditText? = null
    var rvwDataSearch: RecyclerView? = null
    var storageAdapter: TypeDocumentGuideAdapter? = null
    var listStorage: List<DataResponse>? = ArrayList()
    constructor()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.search_custom_dialog, container)
        etwSearchDialog = view.findViewById(R.id.etwSearchDialog)
        rvwDataSearch = view.findViewById(R.id.rvwDataSearch)
        listStorage = arguments?.get("DataList") as List<DataResponse>
        val linearLayoutManager = LinearLayoutManager(context)
        rvwDataSearch?.layoutManager = linearLayoutManager
        storageAdapter = TypeDocumentGuideAdapter()
        storageAdapter?.setDataStorage(listStorage)
        rvwDataSearch?.adapter = storageAdapter
        storageAdapter?.setOnItemClickListener(this@TypeDocPopUpFragment)

        etwSearchDialog?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val datafilte: MutableList<DataResponse> = ArrayList()
                for (storage in listStorage!!){
                    if (storage.codigo?.uppercase()!!.contains(s?.toString()!!.trim().uppercase()) || storage.descripcion?.uppercase()!!.contains(s?.toString()!!.trim().uppercase())){
                        datafilte.add(storage)
                    }
                }
                val linearLayoutManager = LinearLayoutManager(context)
                rvwDataSearch?.layoutManager = linearLayoutManager
                storageAdapter = TypeDocumentGuideAdapter()
                storageAdapter?.setDataStorage(datafilte)
                rvwDataSearch?.adapter = storageAdapter
                storageAdapter?.setOnItemClickListener(this@TypeDocPopUpFragment)
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        return view
    }

    override fun onClickDataListener(objectData: DataResponse) {
        val activity = activity as newDialoglistenerTypeDocGuide

        if (objectData.codigo != "NO"){
            activity.closeDialogTypeDocGuide(objectData)
            this.dismiss()
        }

    }
    interface newDialoglistenerTypeDocGuide {
        fun closeDialogTypeDocGuide(data : DataResponse)
    }
}
