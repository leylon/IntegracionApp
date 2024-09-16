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
import android.widget.*
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.model.guide.DocumentTransportGuideResponse


class TransportPopUpFragment : DialogFragment, OnClickListenerCustom<DocumentTransportGuideResponse>  {

    var etwSearchDialog: EditText? = null
    var rvwDataSearch: RecyclerView? = null
    var transportGuideAdapter: TransportGuideAdapter? = null
    var listTransport: List<DocumentTransportGuideResponse>? = ArrayList()
    constructor()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.search_custom_dialog, container)
        etwSearchDialog = view.findViewById(R.id.etwSearchDialog)
        rvwDataSearch = view.findViewById(R.id.rvwDataSearch)
        listTransport = arguments?.get("DataList") as List<DocumentTransportGuideResponse>
        val linearLayoutManager = LinearLayoutManager(context)
        rvwDataSearch?.layoutManager = linearLayoutManager
        transportGuideAdapter = TransportGuideAdapter()
        transportGuideAdapter?.setDataStorage(listTransport)
        rvwDataSearch?.adapter = transportGuideAdapter
        transportGuideAdapter?.setOnItemClickListener(this@TransportPopUpFragment)

        etwSearchDialog?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val datafilte: MutableList<DocumentTransportGuideResponse> = ArrayList()
                for (storage in listTransport!!){
                    if (storage.codigo?.uppercase()!!.contains(s?.toString()!!.trim().uppercase()) || storage.descripcion?.uppercase()!!.contains(s?.toString()!!.trim().uppercase())){
                        datafilte.add(storage)
                    }
                }
                val linearLayoutManager = LinearLayoutManager(context)
                rvwDataSearch?.layoutManager = linearLayoutManager
                transportGuideAdapter = TransportGuideAdapter()
                transportGuideAdapter?.setDataStorage(datafilte)
                rvwDataSearch?.adapter = transportGuideAdapter
                transportGuideAdapter?.setOnItemClickListener(this@TransportPopUpFragment)
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        return view
    }

    override fun onClickDataListener(objectData: DocumentTransportGuideResponse) {
        val activity = activity as newDialoglistenerTransportGuide

        if (objectData.codigo != "NO"){
            activity.closeDialogTransportGuide(objectData)
            this.dismiss()
        }

    }
    interface newDialoglistenerTransportGuide {
        fun closeDialogTransportGuide(data : DocumentTransportGuideResponse)
    }
}
