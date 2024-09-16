package com.pedidos.android.persistence.ui.search

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.db.entity.ProductEntity
import com.pedidos.android.persistence.utils.Formatter

class SearchProductAdapter(val items: MutableList<ProductEntity>, val listener: (ProductEntity) -> Unit) : RecyclerView.Adapter<SearchProductAdapter.SearchProductHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchProductHolder =
            SearchProductHolder(LayoutInflater.from(parent.context).inflate(R.layout.search_item, parent, false))

    override fun getItemCount(): Int = items.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SearchProductHolder, position: Int) {
        val product = items[position]

        if (product.cantidad == 0) product.cantidad = 1

        holder.productName.text = product.descripcion
        holder.productStock.text = product.cantidad.toString()
        holder.productPrice.text = Formatter.DoubleToString(product.precio, product.monedaSimbolo)
    }

    fun updateList(it: List<ProductEntity>) {
        items.clear()
        items.addAll(it)
        notifyDataSetChanged()
    }

    inner class SearchProductHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal val productName: TextView = view.findViewById(R.id.tvwProductName)
        internal val productStock: TextView = view.findViewById(R.id.tvwStock)
        internal val productPrice: TextView = view.findViewById(R.id.tvwPrice)

        init {
            itemView.setOnClickListener { listener(items[adapterPosition]) }
        }
    }
}