package com.pedidos.android.persistence.ui.payment

import android.annotation.SuppressLint
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.model.SelectedOtherPayment

class OtherPaymentAdapter(var list: ArrayList<SelectedOtherPayment>) : RecyclerView.Adapter<OtherPaymentAdapter.OthersHolder>() {
    var items =  list
    lateinit var listener : ((position: Int) -> Unit)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OthersHolder =
        OthersHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false))

    override fun getItemCount(): Int = items.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: OthersHolder, position: Int) {
        holder.textDescription.text = items[position].description
        holder.cardImage.setImageResource(items[position].getImageResource(items[position].icon))
        holder.cardRadio.isChecked = items[position].isSelected

        holder.containerCard.setOnClickListener {
            listener.invoke(position)
        }
    }

    fun updateList(list : ArrayList<SelectedOtherPayment>) {
        items = list
        notifyDataSetChanged()
    }

    inner class OthersHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal val cardImage : ImageView = view.findViewById(R.id.imgCard)
        internal val cardRadio : CheckBox = view.findViewById(R.id.rdCard)
        internal val textDescription : TextView = view.findViewById(R.id.txtDescriptionCard)
        internal val containerCard : ConstraintLayout = view.findViewById(R.id.containerCard)
    }

}