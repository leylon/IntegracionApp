package com.pedidos.android.persistence.ui.guide

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.model.picking.PedidosPicking
import com.pedidos.android.persistence.model.transfer.OnClickTransfer


class PackingAdapter: RecyclerView.Adapter<PackingAdapter.PackingViewHolder>() {

    var listProducts: List<PedidosPicking>? = null
    var clickListener: OnClickTransfer<PedidosPicking>? = null

    fun setDataStorage(listProducts: List<PedidosPicking>?){
        this.listProducts = listProducts
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackingViewHolder {
        clickListener?.let { setOnItemClickListener(it) }
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_transfer_cab, parent, false)
        return PackingViewHolder(v)
    }

    override fun onBindViewHolder(holder: PackingViewHolder, position: Int) {
        val dataProducts = listProducts!![position]
        holder.tvwTransferObs.text = dataProducts.estado
        holder.tvwTransferNumber.text = dataProducts.pedido
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
    fun setOnItemClickListener(clickListener: OnClickTransfer<PedidosPicking>){
        this.clickListener = clickListener
    }
    class PackingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val tvwTransferObs: TextView = itemView.findViewById(R.id.tvwTransferObs)
        val tvwTransferNumber: TextView = itemView.findViewById(R.id.tvwTransferNumber)
        val tvwTransferTienda: TextView = itemView.findViewById(R.id.tvwTransferTienda)

    }
}