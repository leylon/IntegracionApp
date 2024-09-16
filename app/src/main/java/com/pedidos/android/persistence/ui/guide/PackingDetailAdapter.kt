package com.pedidos.android.persistence.ui.guide

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
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.model.SaleSubItem
import com.pedidos.android.persistence.model.transfer.OnClickTransfer
import java.text.DecimalFormat

class PackingDetailAdapter(var items: MutableList<SaleSubItem>, val onItemDelete: (SaleSubItem) -> Unit) :
    RecyclerView.Adapter<PackingDetailAdapter.PackingViewHolder>() {

    var clickListener: OnClickTransfer<SaleSubItem>? = null
    lateinit var compProductActionCall : (() -> Unit) // se usa cuando el producto ya se viene del search por primera vez para abrir complementos
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackingViewHolder {
        clickListener?.let { setOnItemClickListener(it) }
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_product_packing, parent, false)
        return PackingViewHolder(v)
    }


    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: PackingViewHolder, position: Int) {
        val subItem = items[position]
        holder.tvwProductName.text = subItem.descripcion

        if(subItem.pesoUnitario != null){
            holder.editBulto.setText("${subItem.pesoUnitario}")
        }else {

        }

        if (subItem.productoconcomplemento.toString().isNullOrEmpty()){
            subItem.productoconcomplemento = 0
        }
        holder.tvwProductCant.text =subItem.cantidadpickada.toString()
        holder.tvwProductCantidad.text = subItem.cantidad.toString()

        holder.tvwProductCant.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {  }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (holder.tvwProductCant.text.length!! > 0) {
                    if ( subItem.productoconcomplemento <= subItem.cantidad ){
                        subItem.cantidadpickada = holder.tvwProductCant.text.toString().toInt()
                        subItem.productoconcomplemento =holder.tvwProductCant.text.toString().toInt()
                        items[position] = subItem
                    }else{
                        subItem.productoconcomplemento =holder.tvwProductCant.text.toString().toInt() -1
                        subItem.cantidadpickada = holder.tvwProductCant.text.toString().toInt() -1
                        items[position] = subItem
                        AlertDialog.Builder(holder.itemView.context, R.style.AppTheme_DIALOG)
                            .setTitle(R.string.app_name)
                            .setMessage("No se puede Registrar mas de la Cantidad")
                            .setPositiveButton(R.string.aceptar) { d, _ -> d.dismiss() }
                            .setCancelable(false)
                            .create().show()
                    }

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
        if (subItem.productoconcomplemento> 0){
            holder.tvwProductName.setTextColor(Color.BLUE)
        }else {
            holder.tvwProductName.setTextColor(Color.BLACK)
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
    fun setOnItemClickListener(clickListener: OnClickTransfer<SaleSubItem>){
        this.clickListener = clickListener
    }

    inner class PackingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal val tvwProductName: TextView = itemView.findViewById(R.id.tvwProductName)

        internal val ivbDelete: ImageButton = itemView.findViewById(R.id.ivbDelete)
        internal val lltContainer: LinearLayout = itemView.findViewById(R.id.lltContainer)
        internal val lltContainerCant: LinearLayout = itemView.findViewById(R.id.lltContainerCant)
        internal val tvwProductCantLess: TextView = itemView.findViewById(R.id.tvwProductCantLess)
        internal val tvwProductCant: TextView = itemView.findViewById(R.id.tvwProductCant)
        internal val tvwProductCantPlus: TextView = itemView.findViewById(R.id.tvwProductCantPlus)
        internal val tvwCodeProducto: TextView = itemView.findViewById(R.id.tvwCodeProducto)
        internal val tvwProductCantidad: TextView = itemView.findViewById(R.id.tvwProductCantidad)
        internal val editBulto: EditText = itemView.findViewById(R.id.edit_bulto)
        internal val editBultoTotal: TextView = itemView.findViewById(R.id.edit_bulto_total)
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
                itemToLess.saleCodigo = "-"
                //items.removeAt(adapterPosition)
                if (itemToLess.productoconcomplemento > 0){ itemToLess.cantidadpickada = itemToLess.productoconcomplemento - 1
                itemToLess.productoconcomplemento = itemToLess.productoconcomplemento - 1
                //itemToLess.pesoTotal = "N"
                items[adapterPosition] = itemToLess

                notifyDataSetChanged()
                    if (clickListener != null) {
                        clickListener!!.onClickDataListener(itemToLess)
                    }
                }


            }
            tvwProductCantPlus.setOnClickListener{

                val itemToPluss = items[adapterPosition]
                itemToPluss.saleCodigo = "+"
                if (itemToPluss.productoconcomplemento< itemToPluss.cantidad) {
                    //items.removeAt(adapterPosition)
                    itemToPluss.cantidadpickada = itemToPluss.productoconcomplemento + 1
                    itemToPluss.productoconcomplemento = itemToPluss.productoconcomplemento + 1
                    //itemToPluss.pesoTotal = "N"
                    tvwProductName.setTextColor(
                        Color.rgb(
                            36,
                            86,
                            240
                        )
                    )
                    items[adapterPosition] = itemToPluss
                    notifyDataSetChanged()
                    if (clickListener != null) {
                        clickListener!!.onClickDataListener(itemToPluss)
                    }
                }

            }

            editBulto.addTextChangedListener( object : TextWatcher{
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                    }

                    override fun afterTextChanged(s: Editable?) {
                        if(editBulto.text.toString().isNullOrEmpty()){
                            //editBulto.setText("0")
                            editBultoTotal.text = "0"
                        }else{
                        val itemToPluss = items[adapterPosition]
                        //itemToPluss.saleCodigo = "+"
                        val formato = DecimalFormat("#.##")

                        itemToPluss.pesoUnitario =  editBulto.text.toString().toDouble()
                        val numeroRedondeado = formato.format(itemToPluss.pesoUnitario * itemToPluss.cantidad).toDouble()
                        itemToPluss.pesoTotal = numeroRedondeado
                        editBultoTotal.text = "$numeroRedondeado"
                        items[adapterPosition] = itemToPluss
                        //notifyDataSetChanged()
                        /*if (clickListener != null) {
                            clickListener!!.onClickDataListener(itemToPluss)
                        }*/
                        }
                    }
                })


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