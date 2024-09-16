package com.pedidos.android.persistence.ui.guide

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.model.picking.PedidosPicking
import com.pedidos.android.persistence.model.transfer.OnClickTransfer


class ReportPackingAdapter: RecyclerView.Adapter<ReportPackingAdapter.PackingViewHolder>() {

    var listProducts: List<PedidosPicking>? = null
    var clickListener: OnClickTransfer<PedidosPicking>? = null

    fun setDataStorage(listProducts: List<PedidosPicking>?){
        this.listProducts = listProducts
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackingViewHolder {
        clickListener?.let { setOnItemClickListener(it) }
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_report_picking, parent, false)
        return PackingViewHolder(v)
    }

    override fun onBindViewHolder(holder: PackingViewHolder, position: Int) {
        val dataProducts = listProducts!![position]
        //holder.tvwTransferObs.text = dataProducts.estado
        holder.tvwReportPedido.text = dataProducts.pedido
        holder.tvwReportTienda.text = dataProducts.tienda
        holder.imgPrinterReport.setOnClickListener {
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
        val imgPrinterReport: ImageView = itemView.findViewById(R.id.imgPrinterReport)
        val tvwReportPedido: TextView = itemView.findViewById(R.id.tvwReportPedido)
        val tvwReportTienda: TextView = itemView.findViewById(R.id.tvwReportTienda)

    }
}