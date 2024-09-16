package com.pedidos.android.persistence.ui.reports

import android.annotation.SuppressLint
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.db.entity.GeneratedDocumentEntity
import com.pedidos.android.persistence.utils.Defaults
import com.pedidos.android.persistence.utils.Formatter

class GeneratedDocumentsAdapter(val items: MutableList<GeneratedDocumentEntity>, val listener: (GeneratedDocumentEntity) -> Unit) : RecyclerView.Adapter<GeneratedDocumentsAdapter.GeneratedDocumentsHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeneratedDocumentsHolder =
            GeneratedDocumentsHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.generated_document_item, parent, false))

    override fun getItemCount(): Int = items.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: GeneratedDocumentsHolder, position: Int) {
        val sale = items[position]

        holder.documentType.text = Defaults.BOLETA
        holder.documentNumber.text = sale.documentNumber
        holder.client.text = sale.clientCode
        holder.total.text = Formatter.DoubleToString(sale.total, sale.currencySimbol)

    }

    fun updateList(it: List<GeneratedDocumentEntity>) {
        items.clear()
        items.addAll(it)
        notifyDataSetChanged()
    }

    inner class GeneratedDocumentsHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal val documentType: TextView = view.findViewById(R.id.tvwDocumentType)
        internal val documentNumber: TextView = view.findViewById(R.id.tvwDocumentNumber)
        internal val client: TextView = view.findViewById(R.id.tvwClient)
        internal val total: TextView = view.findViewById(R.id.tvwTotal)
        private val printButton: AppCompatImageButton = view.findViewById(R.id.ibwPrint)

        init {
            printButton.setOnClickListener { listener(items[adapterPosition]) }
        }
    }
}