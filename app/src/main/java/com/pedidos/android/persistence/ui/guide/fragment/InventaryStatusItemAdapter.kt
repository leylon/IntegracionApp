package com.pedidos.android.persistence.ui.guide.fragment

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.model.guide.OperationGuideResponse
import com.pedidos.android.persistence.model.inventary.InventaryStatusItem

class InventaryStatusItemAdapter: RecyclerView.Adapter<InventaryStatusItemAdapter.InventaryStatusItemViewHolder>() {

    var listData: List<InventaryStatusItem>? = null
    fun setDataStorage(listData: List<InventaryStatusItem>?){
        this.listData = listData
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventaryStatusItemViewHolder {

        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_status_inventary, parent, false)
        return InventaryStatusItemViewHolder(v)
    }

    override fun onBindViewHolder(holder: InventaryStatusItemViewHolder, position: Int) {
        val dataStorage = listData!![position]
        holder.tvItemInventaryDescription.text = dataStorage.descripcion
        holder.tvItemInventaryCod.text = dataStorage.codigo.toString()
        holder.tvItemIventaryCant.text = dataStorage.cantidad.toString() +  "  UND"
        holder.tvItemDataIventaryImei.text = dataStorage.imei.toString()
        if (dataStorage.imei.toString().isNotEmpty()){
            holder.lltContainer.visibility = View.VISIBLE
        }else{
            holder.lltContainer.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return listData?.size ?: 0
    }

    class InventaryStatusItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val lltContainer: LinearLayout = itemView.findViewById(R.id.llContainerInventaryImei)
        val tvItemInventaryCod: TextView = itemView.findViewById(R.id.tvItemInventaryCod)
        val tvItemInventaryDescription: TextView = itemView.findViewById(R.id.tvItemInventaryDescription)
        val tvItemIventaryCant: TextView = itemView.findViewById(R.id.tvItemIventaryCant)
        val tvItemDataIventaryImei: TextView = itemView.findViewById(R.id.tvItemDataIventaryImei)
    }
}