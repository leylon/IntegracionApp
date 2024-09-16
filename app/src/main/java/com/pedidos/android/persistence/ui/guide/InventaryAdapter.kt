package com.pedidos.android.persistence.ui.guide

import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.jakewharton.rxbinding.widget.RxTextView
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.model.SaleSubItem
import com.pedidos.android.persistence.utils.Formatter
import com.pedidos.android.persistence.utils.complementProductTempCode
import kotlinx.android.synthetic.main.sales_activity.*
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class InventaryAdapter(var items: MutableList<SaleSubItem>, val onItemDelete: (SaleSubItem) -> Unit) :
    RecyclerView.Adapter<InventaryAdapter.InvetaryViewHolder>() {
    lateinit var compProductActionCall : (() -> Unit) // se usa cuando el producto ya se viene del search por primera vez para abrir complementos
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvetaryViewHolder =
        InvetaryViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_product_inventary, parent, false)
        )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: InvetaryViewHolder, position: Int) {
        val subItem = items[position]
        holder.tvwProductName.text = subItem.descripcion
        holder.tvwProductCant.text = subItem.cantidad.toString()
        holder.tvwProductCant.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {  }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (holder.tvwProductCant.text.length!! > 0) {
                    subItem.cantidad =holder.tvwProductCant.text.toString().toInt()
                    items[position] = subItem
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        if (!TextUtils.isEmpty(subItem.complementaryRowColor)) {
            when (subItem.complementaryRowColor) {
                "green" -> holder.tvwProductName.setTextColor(
                    Color.rgb(
                        36,
                        86,
                        43
                    )
                )// .lltContainer.setBackgroundColor(Color.GREEN)
                "blue" -> holder.tvwProductName.setTextColor(
                    Color.rgb(
                        36,
                        86,
                        240
                    )
                )// .lltContainer.setBackgroundColor(Color.BLUE)
                "red" -> holder.tvwProductName.setTextColor(Color.RED)
                "black" -> holder.tvwProductName.setTextColor(Color.BLACK)
                "black111" -> holder.lltContainer.setBackgroundColor(Color.TRANSPARENT)
                else -> holder.lltContainer.setBackgroundColor(Color.TRANSPARENT)
            }
        } else {
            holder.lltContainer.setBackgroundColor(Color.TRANSPARENT)
        }
        if (!subItem.stimei){
            holder.tvwCodeProducto.text = subItem.codigoProducto
            holder.lltContainerCant.visibility =  View.VISIBLE
            holder.tvwProductCantLess.visibility = View.VISIBLE
            holder.tvwProductCant.visibility = View.VISIBLE
            holder.tvwProductCant.isEnabled =  true
            holder.tvwProductCantPlus.visibility = View.VISIBLE
        }else{
            holder.tvwCodeProducto.text = subItem.imei
            holder.lltContainerCant.visibility =  View.VISIBLE
            holder.tvwProductCantLess.visibility = View.GONE
            holder.tvwProductCant.visibility = View.VISIBLE
            holder.tvwProductCant.isEnabled =  false
            holder.tvwProductCantPlus.visibility = View.GONE
        }
        if (subItem.cantidad> 1){
            holder.tvwProductCantLess.setTextColor(Color.BLACK)
        }else {
            holder.tvwProductCantLess.setTextColor(Color.WHITE)
        }
    }

    fun addItems(items: List<SaleSubItem>) {
        try {
            this.items.addAll(items)
            notifyItemRangeChanged(this.items.size - items.size, itemCount)
        }catch (e : Throwable) {
            Log.d("crash", e.message)
        }

    }
    fun deleteItems(items: List<SaleSubItem>){
        try {
            for ((cont, item) in items.withIndex()){
                this.items.remove(item)
                notifyItemRemoved(cont)
            }
        }catch (e : Throwable) {
            Log.d("crash", e.message)
        }

    }

    inner class InvetaryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal val tvwProductName: TextView = itemView.findViewById(R.id.tvwProductName)

        internal val ivbDelete: ImageButton = itemView.findViewById(R.id.ivbDelete)
        internal val lltContainer: LinearLayout = itemView.findViewById(R.id.lltContainer)
        internal val lltContainerCant: LinearLayout = itemView.findViewById(R.id.lltContainerCant)
        internal val tvwProductCantLess: TextView = itemView.findViewById(R.id.tvwProductCantLess)
        internal val tvwProductCant: TextView = itemView.findViewById(R.id.tvwProductCant)
        internal val tvwProductCantPlus: TextView = itemView.findViewById(R.id.tvwProductCantPlus)
        internal val tvwCodeProducto: TextView = itemView.findViewById(R.id.tvwCodeProducto)

        init {
            tvwProductName.setOnClickListener {
                if (itemView.context is AppCompatActivity && !items[adapterPosition].complementaryRowColor.contentEquals(
                        "black"
                    )
                ) {
                    if(checkIsAvailableWarrantyAdd(items[adapterPosition].codigoProducto)) {


                    } else {
                        AlertDialog.Builder(itemView.context, R.style.AppTheme_DIALOG)
                            .setTitle(R.string.app_name)
                            .setMessage(itemView.context.getString(R.string.warranty_size_wrong))
                            .setPositiveButton(R.string.aceptar) { d, _ -> d.dismiss() }
                            .setCancelable(false)
                            .create().show()
                    }

                }

            }




            tvwProductCantLess.setOnClickListener{
                val itemToLess = items[adapterPosition]

                //items.removeAt(adapterPosition)
                if (itemToLess.cantidad > 1){
                itemToLess.cantidad = itemToLess.cantidad - 1
                items[adapterPosition] = itemToLess

                notifyDataSetChanged()
                }
            }
            tvwProductCantPlus.setOnClickListener{

                val itemToPluss = items[adapterPosition]
                //items.removeAt(adapterPosition)
                itemToPluss.cantidad = itemToPluss.cantidad + 1
                items[adapterPosition] = itemToPluss
                notifyDataSetChanged()
            }




        }
    }



    private fun checkIsAvailableWarrantyAdd( codeProduct : String) : Boolean {
        var check = true
        items.forEach {
            if(it.codgaraexte == codeProduct) {
                check = false
                return@forEach
            }
        }
        return check
    }
    private fun addCantProduct(adapterPosition : Int){
        val itemToDelete = items[adapterPosition]
        var newList = mutableListOf<SaleSubItem>()
        items.forEach {
            if(it.codgaraexte != itemToDelete.codigoProducto
                &&
                it.codigoProducto != itemToDelete.codigoProducto) {
                newList.add(it)
            }
        }
        //items.removeAt(adapterPosition)
        items = newList
        //notifyItemRemoved(adapterPosition)
        notifyDataSetChanged()
    }
}