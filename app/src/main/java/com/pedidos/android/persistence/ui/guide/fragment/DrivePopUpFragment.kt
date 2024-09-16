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
import com.pedidos.android.persistence.model.guide.DocumentIdResponse


class DrivePopUpFragment : DialogFragment, OnClickListenerCustom<DocumentIdResponse>  {

    var etwSearchDialog: EditText? = null
    var rvwDataSearch: RecyclerView? = null
    var auxGuideAdapter: DriveDocGuideAdapter? = null
    var listAuxGuide: List<DocumentIdResponse>? = ArrayList()
    constructor()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.search_custom_dialog, container)
        etwSearchDialog = view.findViewById(R.id.etwSearchDialog)
        rvwDataSearch = view.findViewById(R.id.rvwDataSearch)
        listAuxGuide = arguments?.get("DataList") as List<DocumentIdResponse>
        val linearLayoutManager = LinearLayoutManager(context)
        rvwDataSearch?.layoutManager = linearLayoutManager
        auxGuideAdapter = DriveDocGuideAdapter()
        auxGuideAdapter?.setDataStorage(listAuxGuide)
        rvwDataSearch?.adapter = auxGuideAdapter
        auxGuideAdapter?.setOnItemClickListener(this@DrivePopUpFragment)

        etwSearchDialog?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val datafilte: MutableList<DocumentIdResponse> = ArrayList()
                for (storage in listAuxGuide!!){
                    if (storage.codigo?.uppercase()!!.contains(s?.toString()!!.trim().uppercase()) || storage.descripcion?.uppercase()!!.contains(s?.toString()!!.trim().uppercase())){
                        datafilte.add(storage)
                    }
                }
                val linearLayoutManager = LinearLayoutManager(context)
                rvwDataSearch?.layoutManager = linearLayoutManager
                auxGuideAdapter = DriveDocGuideAdapter()
                auxGuideAdapter?.setDataStorage(datafilte)
                rvwDataSearch?.adapter = auxGuideAdapter
                auxGuideAdapter?.setOnItemClickListener(this@DrivePopUpFragment)
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        return view
    }

    override fun onClickDataListener(objectData: DocumentIdResponse) {
        val activity = activity as newDialoglistenerAuxGuide

        if (objectData.codigo != "NO"){
            activity.closeDialogDriveGuide(objectData)
            this.dismiss()
        }

    }
    interface newDialoglistenerAuxGuide {
        fun closeDialogDriveGuide(data : DocumentIdResponse)
    }
}
