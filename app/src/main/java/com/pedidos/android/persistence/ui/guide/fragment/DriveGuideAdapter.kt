package com.pedidos.android.persistence.ui.guide.fragment

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.model.guide.DataResponse

class DriveGuideAdapter: RecyclerView.Adapter<DriveGuideAdapter.TypeDocumentGuideViewHolder>() {

    var listStorage: List<DataResponse>? = null
    var clickListener: OnClickListenerCustom<DataResponse>? = null

    fun setDataStorage(listStorage: List<DataResponse>?){
        this.listStorage = listStorage
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TypeDocumentGuideViewHolder {
        clickListener?.let { setOnItemClickListener(it) }
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_data_dialog, parent, false)
        return TypeDocumentGuideViewHolder(v)
    }

    override fun onBindViewHolder(holder: TypeDocumentGuideViewHolder, position: Int) {
        val dataStorage = listStorage!![position]
        holder.tvItemDataDialog.text = "${dataStorage.codigo} - ${dataStorage.descripcion}"
        holder.itemView.setOnClickListener {
            if (clickListener != null) {
                clickListener!!.onClickDataListener(dataStorage)
            }
        }
    }

    override fun getItemCount(): Int {
        return listStorage?.size ?: 0
    }
    fun setOnItemClickListener(clickListener: OnClickListenerCustom<DataResponse>){
        this.clickListener = clickListener
    }
    class TypeDocumentGuideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvItemDataDialog: TextView = itemView.findViewById(R.id.tvItemDataDialog)
    }
}