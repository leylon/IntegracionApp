package com.pedidos.android.persistence.ui.sale

import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.model.SaleSubItem
import com.pedidos.android.persistence.utils.Formatter
import com.pedidos.android.persistence.utils.complementProductTempCode

class SaleAdapter(var items: MutableList<SaleSubItem>, val onItemDelete: (SaleSubItem) -> Unit) :
    RecyclerView.Adapter<SaleAdapter.SaleHolder>() {
    lateinit var compProductActionCall : (() -> Unit) // se usa cuando el producto ya se viene del search por primera vez para abrir complementos
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleHolder =
        SaleHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.sales_sub_item, parent, false)
        )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SaleHolder, position: Int) {
        val subItem = items[position]
        holder.tvwProductName.text = subItem.descripcion
        holder.tvwProductPrice.text = Formatter.DoubleToString(subItem.precio, subItem.monedaSimbolo)
        holder.tvwProductCuantity.text = subItem.cantidad.toString()
        holder.tvwProductDiscount.text =
            Formatter.DoubleToString(subItem.pcdcto, "%")
        holder.tvwProductTotalPrice.text =
            Formatter.DoubleToString(subItem.totaldetalle, subItem.monedaSimbolo)

        holder.tvwProductImei.text = subItem.imei
        if (subItem.imei.isNullOrEmpty()) {
            holder.tvwProductImei.visibility = View.GONE
        } else {
            holder.tvwProductImei.visibility = View.VISIBLE
        }

            if( items[position].productoconcomplemento <= 0) {
            holder.ivbProdComp.visibility = View.GONE
        } else {
            holder.ivbProdComp.visibility = View.VISIBLE
            if(complementProductTempCode != null && complementProductTempCode == items[position].codigoProducto) {
                openComplementary(holder.itemView, position)
                compProductActionCall.invoke()
            }
            //

        }
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
        } else
            holder.lltContainer.setBackgroundColor(Color.TRANSPARENT)
    }

    fun addItems(items: List<SaleSubItem>) {
        try {
            this.items.addAll(items)
            notifyItemRangeChanged(this.items.size - items.size, itemCount)
        }catch (e : Throwable) {
            Log.d("crash", e.message)
        }

    }

    inner class SaleHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal val tvwProductName: TextView = itemView.findViewById(R.id.tvwProductName)
        internal val tvwProductDiscount: TextView = itemView.findViewById(R.id.tvwProductDiscount)
        internal val tvwProductTotalPrice: TextView =
            itemView.findViewById(R.id.tvwProductTotalPrice)
        internal val ivbDelete: ImageButton = itemView.findViewById(R.id.ivbDelete)
        internal val lltContainer: LinearLayout = itemView.findViewById(R.id.lltContainer)
        internal val ivbProdComp : ImageButton = itemView.findViewById(R.id.ivbProdComp)
        internal val tvwProductPrice : TextView = itemView.findViewById(R.id.tvwProductPrice)
        internal val tvwProductCuantity : TextView = itemView.findViewById(R.id.tvwProductCuantity)
        internal val lltContainerPrice: LinearLayout = itemView.findViewById(R.id.lltContainerPrice)
        internal val tvwProductImei: TextView = itemView.findViewById(R.id.tvwProductImei)
        init {
            tvwProductName.setOnClickListener {
                if (itemView.context is AppCompatActivity && !items[adapterPosition].complementaryRowColor.contentEquals(
                        "black"
                    )
                ) {
                    if(checkIsAvailableWarrantyAdd(items[adapterPosition].codigoProducto)) {
                        var intent = Intent(itemView.context,GarantiesProductActivity::class.java)
                        intent.putExtra(GarantiesProductActivity.PROD_ID, items[adapterPosition].codigoProducto)
                        intent.putExtra(GarantiesProductActivity.PROD_DESCRIP, items[adapterPosition].descripcion)
                        intent.putExtra(GarantiesProductActivity.PROD_SEC,items[adapterPosition].secuencial)
                        intent.putExtra(GarantiesProductActivity.PROD_PRICE,items[adapterPosition].precio)
                        intent.putExtra(GarantiesProductActivity.PROD_COIN,items[adapterPosition].monedaSimbolo)
                        (itemView.context as AppCompatActivity)
                            .startActivityForResult(
                                intent, SaleActivity.GARANTIE_REQUEST)

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

            ivbProdComp.setOnClickListener {
               openComplementary(itemView, adapterPosition)
            }

            ivbDelete.setOnClickListener {
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
                onItemDelete(itemToDelete)
                //notifyItemRemoved(adapterPosition)
                notifyDataSetChanged()
            }
        }
    }

    fun openComplementary(itemView : View , adapterPosition : Int ) {
        var intent = Intent(itemView.context,ComplementaryProductActivity::class.java)
        intent.putExtra(ComplementaryProductActivity.PROD_ID, items[adapterPosition].codigoProducto)
        intent.putExtra(ComplementaryProductActivity.PROD_DESCRIP, items[adapterPosition].descripcion)
        intent.putExtra(ComplementaryProductActivity.PROD_SEC,items[adapterPosition].secuencial)
        intent.putExtra(ComplementaryProductActivity.PROD_PRICE,items[adapterPosition].precio)
        intent.putExtra(ComplementaryProductActivity.PROD_COIN,items[adapterPosition].monedaSimbolo)
        (itemView.context as AppCompatActivity)
            .startActivityForResult(
                intent, SaleActivity.COMPLEMENTARY_REQUEST)
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
}