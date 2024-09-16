package com.pedidos.android.persistence.ui.guide

import android.app.DatePickerDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.google.gson.Gson
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.db.entity.SaleEntity
import com.pedidos.android.persistence.model.picking.PedidoRequest
import com.pedidos.android.persistence.model.picking.PedidosPicking
import com.pedidos.android.persistence.model.picking.ReportPickingDet
import com.pedidos.android.persistence.model.picking.ReportPickingResponse
import com.pedidos.android.persistence.ui.menu.MenuActivity
import com.pedidos.android.persistence.utils.Formatter
import com.pedidos.android.persistence.viewmodel.PackingViewModel
import kotlinx.android.synthetic.main.activity_report_picking.*
import kotlinx.android.synthetic.main.inventary_activity.*
import kotlinx.android.synthetic.main.inventary_activity.btnProcessList
import kotlinx.android.synthetic.main.inventary_activity.rvwProducts
import kotlinx.android.synthetic.main.inventary_activity.tielDateGuide
import kotlinx.android.synthetic.main.sales_activity.*
import kotlinx.android.synthetic.main.transfer_cab_activity.*
import java.util.*
import kotlin.collections.ArrayList

class ReportPackingPrincipal: MenuActivity() {
    var dayMin = 0
    var yearMin = 0
    var monthMin = 0
    var curDate: Calendar? = null
    var dateOperacion = ""
    var packingAdapter: ReportPackingAdapter? = null
    var listProducts: ArrayList<PedidosPicking>? = ArrayList()
    lateinit var context: Context
    private lateinit var packingViewModel: PackingViewModel
    lateinit var fltLoading: FrameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_picking)
        context = this
        setFechaOpe()

        fltLoading = findViewById(R.id.fltLoading)
        tielDateGuide.setOnClickListener { showDatePickerDialog() }
        btnProcessList.setOnClickListener { retrieveReportePedidos() }
        listProducts = ArrayList()

        rvwReportePedido.layoutManager = LinearLayoutManager(this)
        packingAdapter?.setDataStorage(listProducts)
        rvwReportePedido.adapter = packingAdapter
        //packingAdapter?.setOnItemClickListener(this)
        val transferFactory = PackingViewModel.Companion.Factory(application, getSettings().urlbase)
        packingViewModel = ViewModelProviders.of(this, transferFactory).get(PackingViewModel::class.java)

    }

    private fun showDatePickerDialog() {
        val newFragment = DatePickerDialog(
            this,
            { datePicker, year, month, day -> // +1 because January is zero
                val selectedDate = "${
                    Formatter.formato(
                        day.toString(),
                        2,
                        "0"
                    )
                }/${Formatter.formato((month + 1).toString(), 2, "0")}/$year"
                dateOperacion = "$year${Formatter.formato((month + 1).toString(), 2, "0")}${
                    Formatter.formato(
                        day.toString(),
                        2,
                        "0"
                    )
                }"
                tielDateGuide!!.setText(selectedDate)

            }, yearMin, monthMin, dayMin
        )

        newFragment.show()
    }
    fun setFechaOpe() {
        //showDatePickerDialog()
        curDate = Calendar.getInstance()
        // curDate?.add(Calendar.DATE, -1)
        dayMin = curDate?.get(Calendar.DAY_OF_MONTH)!!
        yearMin = curDate?.get(Calendar.YEAR)!!
        monthMin = curDate?.get(Calendar.MONTH)!!
        dateOperacion = "$yearMin${Formatter.formato((monthMin + 1).toString(), 2, "0")}${
            Formatter.formato(
                dayMin.toString(),
                2,
                "0"
            )
        }"
        tielDateGuide!!.setText(
            "${
                Formatter.formato(
                    dayMin.toString(),
                    2,
                    "0"
                )
            }/${Formatter.formato((monthMin + 1).toString(), 2, "0")}/$yearMin"
        )
    }

    fun retrieveReportePedidos(){
        showProgress(true)
        packingViewModel.ReportPicking(PedidoRequest(
            fecha = dateOperacion,
            pedido = "",
            usuario = getSession().usuario
        ),::showReportePedidos,::onError)


    }
    private fun showReportePedidos(reportPickingResponse: ReportPickingResponse){
        showProgress(false)
        if(reportPickingResponse.result){
          var  listProducts: ArrayList<PedidosPicking> = ArrayList()  //reportPickingResponse.data as ArrayList<PedidosPicking>
            for (data: ReportPickingDet in reportPickingResponse.data){
                listProducts.add(PedidosPicking(
                    estado =data.estado,
                     fecha= data.fecha,
                     moneda=data.moneda,
                 pedido= data.pedido,
                 simbolo= data.simbolo,
                 tienda= data.tienda,
                 tipdoc= data.tipdoc,
                 total= data.total
                ))
            }
            println("adapterlist: ${Gson().toJson(listProducts)}")
            val linearLayoutManager = LinearLayoutManager(context)
            packingAdapter = ReportPackingAdapter()
            rvwReportePedido.layoutManager = linearLayoutManager
            packingAdapter?.setDataStorage(listProducts)
            rvwReportePedido.adapter = packingAdapter
        }

    }

    private fun setSessionInfo(saleEntity: SaleEntity): SaleEntity {
        val userInfo = getSession()
        saleEntity.vendedorCodigo = userInfo.vendedorcodigo
        saleEntity.usuario = userInfo.usuario
        saleEntity.cajaCodigo = userInfo.cajacodigo
        saleEntity.tienda = userInfo.tienda
        // saleEntity.email = userInfo.email
        return saleEntity
    }
    private fun onError(message: String) {
        showProgress(false)
        Log.e(PackingDetActivity.TAG, message)
        printOnDialogMessaging(message)
    }

    private fun showProgress(show: Boolean) {
        fltLoading.visibility = if (show) View.VISIBLE else View.GONE
    }
}