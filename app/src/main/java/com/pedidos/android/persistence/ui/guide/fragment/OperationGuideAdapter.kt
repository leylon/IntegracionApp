package com.pedidos.android.persistence.ui.guide.fragment

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.model.guide.OperationGuideResponse

class OperationGuideAdapter: RecyclerView.Adapter<OperationGuideAdapter.OperationGuideViewHolder>() {

    var listData: List<OperationGuideResponse>? = null
    var clickListener: OnClickListenerCustom<OperationGuideResponse>? = null

    fun setDataStorage(listData: List<OperationGuideResponse>?){
        this.listData = listData
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OperationGuideViewHolder {
        clickListener?.let { setOnItemClickListener(it) }
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_data_dialog, parent, false)
        return OperationGuideViewHolder(v)
    }

    override fun onBindViewHolder(holder: OperationGuideViewHolder, position: Int) {
        val dataStorage = listData!![position]
        holder.tvItemDataDialog.text = "${dataStorage.codigo} - ${dataStorage.descripcion}"
        holder.itemView.setOnClickListener {
            if (clickListener != null) {
                clickListener!!.onClickDataListener(dataStorage)
            }
        }
    }

    override fun getItemCount(): Int {
        return listData?.size ?: 0
    }
    fun setOnItemClickListener(clickListener: OnClickListenerCustom<OperationGuideResponse>){
        this.clickListener = clickListener
    }
    class OperationGuideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvItemDataDialog: TextView = itemView.findViewById(R.id.tvItemDataDialog)
    }
}