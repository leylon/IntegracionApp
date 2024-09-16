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
import com.pedidos.android.persistence.model.inventary.InventaryResponse
import com.pedidos.android.persistence.model.inventary.InventaryResponseStatus
import com.pedidos.android.persistence.model.inventary.InventaryStatusItem


class TransferStatusPopUpFragment : DialogFragment, OnClickListenerCustom<InventaryResponseStatus>  {

    lateinit var tvItemIventaryStatus: TextView
    lateinit var tvItemIventaryMoreThan: TextView
    lateinit var tvItemIventaryFinish: TextView
    lateinit var tvTitlePopup: TextView
    lateinit var imgCheckInventary: ImageView
    var rvwDataInventary: RecyclerView? = null
    var auxGuideAdapter: InventaryStatusItemAdapter? = null
    var inventaryStatus: InventaryResponseStatus = InventaryResponseStatus()
    constructor()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.status_dialog, container)
        tvTitlePopup = view.findViewById(R.id.tvTitlePopup)
        tvItemIventaryStatus = view.findViewById(R.id.tvItemIventaryStatus)
        tvItemIventaryMoreThan = view.findViewById(R.id.tvItemIventaryMoreThan)
        tvItemIventaryFinish = view.findViewById(R.id.tvItemIventaryFinish)
        imgCheckInventary = view.findViewById(R.id.imgCheckInventary)
        rvwDataInventary = view.findViewById(R.id.rvwProductsInvetary)
        inventaryStatus = arguments?.get("DataList") as InventaryResponseStatus
        val linearLayoutManager = LinearLayoutManager(context)
        rvwDataInventary?.layoutManager = linearLayoutManager
        auxGuideAdapter = InventaryStatusItemAdapter()
        auxGuideAdapter?.setDataStorage(inventaryStatus.data)
        rvwDataInventary?.adapter = auxGuideAdapter
        tvItemIventaryStatus.text = inventaryStatus.message
        tvItemIventaryMoreThan.setOnClickListener{
            val activity = activity as newDialoglistenerAuxGuide
            inventaryStatus.opcion = "MASTARDE"
            activity.closeDialogDriveGuide(inventaryStatus)
            this.dismiss()
        }
        tvItemIventaryFinish.setOnClickListener{
            val activity = activity as newDialoglistenerAuxGuide
            inventaryStatus.opcion = "FINALIZAR"
            activity.closeDialogDriveGuide(inventaryStatus)
            this.dismiss()
        }
        return view
    }

    override fun onClickDataListener(objectData: InventaryResponseStatus) {
        val activity = activity as newDialoglistenerAuxGuide


            activity.closeDialogDriveGuide(objectData)
            this.dismiss()


    }
    interface newDialoglistenerAuxGuide {
        fun closeDialogDriveGuide(data : InventaryResponseStatus)
    }
}
