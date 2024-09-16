package com.pedidos.android.persistence.ui.guide

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.model.transfer.OnClickTransfer
import com.pedidos.android.persistence.model.transfer.TransferDataResponse

class TransferGuideAdapter: RecyclerView.Adapter<TransferGuideAdapter.TypeDocumentGuideViewHolder>() {

    var listProducts: List<TransferDataResponse>? = null
    var clickListener: OnClickTransfer<TransferDataResponse>? = null

    fun setDataStorage(listProducts: List<TransferDataResponse>?){
        this.listProducts = listProducts
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TypeDocumentGuideViewHolder {
        clickListener?.let { setOnItemClickListener(it) }
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_transfer_cab, parent, false)
        return TypeDocumentGuideViewHolder(v)
    }

    override fun onBindViewHolder(holder: TypeDocumentGuideViewHolder, position: Int) {
        val dataProducts = listProducts!![position]
        holder.tvwTransferObs.text = dataProducts.observacion
        holder.tvwTransferNumber.text = dataProducts.numdoc
        holder.tvwTransferTienda.text = dataProducts.tienda
        holder.itemView.setOnClickListener {
            if (clickListener != null) {
                clickListener!!.onClickDataListener(dataProducts)
            }
        }
    }

    override fun getItemCount(): Int {
        return listProducts?.size ?: 0
    }
    fun setOnItemClickListener(clickListener: OnClickTransfer<TransferDataResponse>){
        this.clickListener = clickListener
    }
    class TypeDocumentGuideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvwTransferObs: TextView = itemView.findViewById(R.id.tvwTransferObs)
        val tvwTransferNumber: TextView = itemView.findViewById(R.id.tvwTransferNumber)
        val tvwTransferTienda: TextView = itemView.findViewById(R.id.tvwTransferTienda)

    }
}