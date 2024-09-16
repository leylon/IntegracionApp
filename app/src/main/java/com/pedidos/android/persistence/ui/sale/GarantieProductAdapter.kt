package com.pedidos.android.persistence.ui.sale

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.db.entity.ProductComplementaryEntity
import com.pedidos.android.persistence.utils.Formatter

class GarantieProductAdapter (val items: MutableList<ProductComplementaryEntity>, val listener: (Int) -> Unit) : RecyclerView.Adapter<GarantieProductAdapter.GarantieHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GarantieHolder =
        GarantieHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_garantie_product, parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: GarantieHolder, position: Int) {
        val item = items[position]
        holder.tvwTitle.text = item.codigo
        holder.tvwDesc.text = item.descripcion
        holder.tvwPrice.text = Formatter.DoubleToString(item.precio, item.monedaSimbolo)

        holder.checkProd.isChecked = item.checkStatus == 1
        if (!TextUtils.isEmpty(item.rowColor)) {
            when (item.rowColor) {
                "green" -> holder.tvwDesc.setTextColor(Color.rgb(36,86,43)) //Dark green
                "blue" -> holder.tvwDesc.setTextColor(Color.rgb(36,86,240)) //Dark blue
                "red" -> holder.tvwDesc.setTextColor(Color.RED)
                "black" -> holder.tvwDesc.setTextColor(Color.BLACK)
                "black111" -> holder.lltContainer.setBackgroundColor(Color.TRANSPARENT)
                else -> holder.lltContainer.setBackgroundColor(Color.TRANSPARENT)
            }
        } else
            holder.lltContainer.setBackgroundColor(Color.TRANSPARENT)
    }

    fun addItems(items: List<ProductComplementaryEntity>){
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun addItemsCheck(items : List<ProductComplementaryEntity>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    inner class GarantieHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val lltContainer: LinearLayout = itemView.findViewById(R.id.lltContainer)
        internal val tvwTitle: TextView = itemView.findViewById(R.id.tvwTitle)
        internal val tvwPrice: TextView = itemView.findViewById(R.id.tvwPrice)
        internal val tvwDesc: TextView = itemView.findViewById(R.id.tvwDesc)
        internal val checkProd : CheckBox = itemView.findViewById(R.id.checkGarantie)
        internal val btnCheck : Button = itemView.findViewById(R.id.btnCheckGarantie)
        init {
            btnCheck.setOnClickListener {
                listener(adapterPosition) }
        }
    }
}