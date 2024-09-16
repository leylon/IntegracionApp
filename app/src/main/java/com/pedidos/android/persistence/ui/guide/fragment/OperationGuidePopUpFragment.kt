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
import com.pedidos.android.persistence.model.guide.OperationGuideResponse


class OperationGuidePopUpFragment : DialogFragment, OnClickListenerCustom<OperationGuideResponse>  {

    var etwSearchDialog: EditText? = null
    var rvwDataSearch: RecyclerView? = null
    var dataAdapter: OperationGuideAdapter? = null
    var listStorage: List<OperationGuideResponse>? = ArrayList()
    constructor()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.search_custom_dialog, container)
        etwSearchDialog = view.findViewById(R.id.etwSearchDialog)
        rvwDataSearch = view.findViewById(R.id.rvwDataSearch)
        listStorage = arguments?.get("DataList") as List<OperationGuideResponse>
        val linearLayoutManager = LinearLayoutManager(context)
        rvwDataSearch?.layoutManager = linearLayoutManager
        dataAdapter = OperationGuideAdapter()
        dataAdapter?.setDataStorage(listStorage)
        rvwDataSearch?.adapter = dataAdapter
        dataAdapter?.setOnItemClickListener(this@OperationGuidePopUpFragment)

        etwSearchDialog?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val datafilte: MutableList<OperationGuideResponse> = ArrayList()
                for (data in listStorage!!){
                    if (data.codigo?.uppercase()!!.contains(s?.toString()!!.trim().uppercase()) || data.descripcion?.uppercase()!!.contains(s?.toString()!!.trim().uppercase())){
                        datafilte.add(data)
                    }
                }
                val linearLayoutManager = LinearLayoutManager(context)
                rvwDataSearch?.layoutManager = linearLayoutManager
                dataAdapter = OperationGuideAdapter()
                dataAdapter?.setDataStorage(datafilte)
                rvwDataSearch?.adapter = dataAdapter
                dataAdapter?.setOnItemClickListener(this@OperationGuidePopUpFragment)
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        return view
    }

    override fun onClickDataListener(objectData: OperationGuideResponse) {
        val activity = activity as newDialoglistenerTypeDocGuide

        if (objectData.codigo != "NO"){
            activity.closeDialogOperationGuide(objectData)
            this.dismiss()
        }

    }
    interface newDialoglistenerTypeDocGuide {
        fun closeDialogOperationGuide(data : OperationGuideResponse)
    }
}
