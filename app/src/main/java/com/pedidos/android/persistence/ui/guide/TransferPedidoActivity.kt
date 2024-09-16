package com.pedidos.android.persistence.ui.guide

import android.app.DatePickerDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RadioButton
import android.widget.RelativeLayout
import com.google.gson.Gson
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.model.transfer.OnClickTransfer
import com.pedidos.android.persistence.model.transfer.TransferDataResponse
import com.pedidos.android.persistence.model.transfer.TransferRequest
import com.pedidos.android.persistence.model.transfer.TransferResponse
import com.pedidos.android.persistence.ui.guide.fragment.OnClickListenerCustom
import com.pedidos.android.persistence.ui.guide.fragment.PlacaGuideAdapter
import com.pedidos.android.persistence.ui.menu.MenuActivity
import com.pedidos.android.persistence.utils.Formatter
import com.pedidos.android.persistence.viewmodel.GuideViewModel
import com.pedidos.android.persistence.viewmodel.SearchProductViewModel
import com.pedidos.android.persistence.viewmodel.TransferViewModel
import kotlinx.android.synthetic.main.inventary_activity.*
import kotlinx.android.synthetic.main.inventary_activity.tielDateGuide
import kotlinx.android.synthetic.main.sales_activity.*
import kotlinx.android.synthetic.main.sales_activity.toolbar
import kotlinx.android.synthetic.main.transfer_cab_activity.*
import java.io.Serializable
import java.util.*

class TransferPedidoActivity: MenuActivity(), OnClickTransfer<TransferDataResponse> {


    lateinit var rdwPedido: RadioButton
    lateinit var rbwFecha: RadioButton
    lateinit var containerTransferFecha: RelativeLayout
    lateinit var containerTransferPedido: RelativeLayout
    lateinit var btnProcessList: AppCompatImageButton
    var dayMin = 0
    var yearMin = 0
    var monthMin = 0
    var curDate: Calendar? = null
    var dateOperacion = ""
    var tipoFilto  = ""
    var dataTipoFiltro = ""
    var transferGuideAdapter: TransferGuideAdapter? = null
    var listProducts: List<TransferDataResponse>? = ArrayList()
    lateinit var context: Context
    private lateinit var transferViewModel: TransferViewModel
    lateinit var fltLoading: FrameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentViewWithMenu(R.layout.transfer_cab_activity)
        setSupportActionBarMenu(toolbar)
        context  =this
        checkSession()
        toolbar.title = "${getString(R.string.title_guide_transfer)}"
        setFechaOpe()
        rdwPedido = findViewById(R.id.rdwPedido)
        rbwFecha = findViewById(R.id.rbwFecha)
        containerTransferPedido = findViewById(R.id.containerTransferPedido)
        containerTransferFecha = findViewById(R.id.containerTransferFecha)
        fltLoading = findViewById(R.id.fltLoading)
        btnProcessList = findViewById(R.id.btnProcessList)
        rdwPedido.setOnClickListener{ radioButtionOption("DOCUMENTO")}
        rbwFecha.setOnClickListener{ radioButtionOption("FECHA")}
        tipoFilto = "FECHA"
        dataTipoFiltro = dateOperacion

        val transferFactory = TransferViewModel.Companion.Factory(application, getSettings().urlbase)
        transferViewModel = ViewModelProviders.of(this, transferFactory).get(TransferViewModel::class.java)



        containerTransferFecha.visibility = View.VISIBLE
        containerTransferPedido.visibility = View.GONE
        containerTransferFecha.setOnClickListener{ showDatePickerDialog() }
        tilDateGuideTransfer.setOnClickListener { showDatePickerDialog() }
        tielDateGuide.setOnClickListener { showDatePickerDialog() }
        imbSearchDateTranfer.setOnClickListener { showDatePickerDialog() }
        val linearLayoutManager = LinearLayoutManager(context)
        rvwProductsTransfer?.layoutManager = linearLayoutManager
        transferGuideAdapter = TransferGuideAdapter()
        transferGuideAdapter?.setDataStorage(listProducts)
        rvwProductsTransfer?.adapter = transferGuideAdapter
        transferGuideAdapter?.setOnItemClickListener(this)
        btnProcessList.setOnClickListener{ listProducts()}
    }

    private fun radioButtionOption(opcion: String) {
        tipoFilto = opcion
        when(opcion){
            "DOCUMENTO" -> {
                containerTransferFecha.visibility = View.GONE
                containerTransferPedido.visibility = View.VISIBLE
            }
            "FECHA" -> {
                containerTransferFecha.visibility = View.VISIBLE
                containerTransferPedido.visibility = View.GONE
            }
        }
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

    fun listProducts(){
        showProgress(true)
        var transferRequest = TransferRequest()
        transferRequest.usuario = getSession().usuario
        transferRequest.tienda = getSession().tienda
        transferRequest.tipofiltro = tipoFilto
        when(tipoFilto){
            "DOCUMENTO" -> {
                transferRequest.filtro = tielTransferPedido.text.toString()
            }
            "FECHA" -> {
                transferRequest.filtro = dateOperacion
            }
        }
        transferViewModel.listProduct(transferRequest,::setListRecycler,::onError)
    }
    fun setListRecycler(dataList: TransferResponse){
        showProgress(false)
        listProducts =dataList.data
        val linearLayoutManager = LinearLayoutManager(context)
        rvwProductsTransfer?.layoutManager = linearLayoutManager
        transferGuideAdapter = TransferGuideAdapter()
        transferGuideAdapter?.setDataStorage(listProducts)
        rvwProductsTransfer?.adapter = transferGuideAdapter
        transferGuideAdapter?.setOnItemClickListener(this)
    }

    private fun onError(it: String) {
        showProgress(false)
        printOnSnackBarCustom(it)
    }
    fun printOnSnackBarCustom(content: String) {
        val view = (findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup
        Snackbar.make(view, content, Snackbar.LENGTH_INDEFINITE)
            .setDuration(2000)
            .setAction("Action", null).show()
    }
    private fun showProgress(show: Boolean) {
        fltLoading.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onClickDataListener(objectData: TransferDataResponse) {
        println("objeData: ${Gson().toJson(objectData)}")
        startActivity(Intent(this, TransferPedidoDetActivity::class.java).apply {
            //putExtra(EndingActivity.EXTRA_ENTITY, guideRequest)
            putExtra("TranferPedido",objectData as Serializable)
            finish()
        })
    }
}