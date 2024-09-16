package com.pedidos.android.persistence.ui.guide

import android.Manifest
import android.app.DatePickerDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
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
import com.google.zxing.integration.android.IntentIntegrator
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.model.picking.*
import com.pedidos.android.persistence.model.transfer.*
import com.pedidos.android.persistence.ui.guide.fragment.OnClickListenerCustom
import com.pedidos.android.persistence.ui.guide.fragment.PlacaGuideAdapter
import com.pedidos.android.persistence.ui.menu.MenuActivity
import com.pedidos.android.persistence.ui.search.SearchProductActivity
import com.pedidos.android.persistence.utils.Formatter
import com.pedidos.android.persistence.viewmodel.GuideViewModel
import com.pedidos.android.persistence.viewmodel.PackingViewModel
import com.pedidos.android.persistence.viewmodel.SearchProductViewModel

import kotlinx.android.synthetic.main.inventary_activity.*
import kotlinx.android.synthetic.main.inventary_activity.tielDateGuide
import kotlinx.android.synthetic.main.sales_activity.*
import kotlinx.android.synthetic.main.sales_activity.toolbar
import kotlinx.android.synthetic.main.transfer_cab_activity.*
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

class PackingActivity: MenuActivity(), OnClickTransfer<PedidosPicking> {


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
    var packingAdapter: PackingAdapter? = null
    var listProducts: List<PedidosPicking>? = ArrayList()
    lateinit var context: Context
    private lateinit var packingViewModel: PackingViewModel
    lateinit var fltLoading: FrameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentViewWithMenu(R.layout.transfer_cab_activity)
        setSupportActionBarMenu(toolbar)
        context  =this
        checkSession()
        toolbar.title = "${getString(R.string.title_packing)}"
        setFechaOpe()
        rdwPedido = findViewById(R.id.rdwPedido)
        rbwFecha = findViewById(R.id.rbwFecha)
        containerTransferPedido = findViewById(R.id.containerTransferPedido)
        containerTransferFecha = findViewById(R.id.containerTransferFecha)
        fltLoading = findViewById(R.id.fltLoading)
        btnProcessList = findViewById(R.id.btnProcessList)
        rdwPedido.setOnClickListener{ radioButtionOption("DOCUMENTO")}
        rbwFecha.setOnClickListener{ radioButtionOption("FECHA")}
        tipoFilto = "DOCUMENTO"
        dataTipoFiltro = dateOperacion

        val transferFactory = PackingViewModel.Companion.Factory(application, getSettings().urlbase)
        packingViewModel = ViewModelProviders.of(this, transferFactory).get(PackingViewModel::class.java)



        containerTransferFecha.visibility = View.GONE
        containerTransferPedido.visibility =  View.VISIBLE
        containerTransferFecha.setOnClickListener{ showDatePickerDialog() }
        tilDateGuideTransfer.setOnClickListener { showDatePickerDialog() }
        tielDateGuide.setOnClickListener { showDatePickerDialog() }
        imbSearchDateTranfer.setOnClickListener { showDatePickerDialog() }
        imbwProductoWithCamera.setOnClickListener { productSearch()}
        val linearLayoutManager = LinearLayoutManager(context)
        rvwProductsTransfer?.layoutManager = linearLayoutManager
        packingAdapter = PackingAdapter()
        packingAdapter?.setDataStorage(listProducts)
        rvwProductsTransfer?.adapter = packingAdapter
        packingAdapter?.setOnItemClickListener(this)
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
        var transferRequest = PrincipalPedidoRequest(getSession().usuario,dateOperacion)
       // transferRequest.usuario= getSession().usuario
       // transferRequest.fecha = dateOperacion
        //transferRequest.tipofiltro = tipoFilto
        when(tipoFilto){
            "DOCUMENTO" -> {
                println("documento: ${tielTransferPedido.text.toString()}")
                var transferRequest = PedidoRequest(
                    fecha = dateOperacion,
                    pedido = tielTransferPedido.text.toString(),
                    usuario =  getSession().usuario)
                println("Documento")
                packingViewModel.showPedidos(transferRequest,::showPackingDetail,::onError)
            }
            "FECHA" -> {
                var transferRequest = PrincipalPedidoRequest(getSession().usuario,dateOperacion)
                packingViewModel.listPedidos(transferRequest,::setListRecycler,::onError)
            }
        }

    }
    fun setListRecycler(dataList: PrincipalPedidosPicking){
        showProgress(false)
        listProducts =dataList.data
        val linearLayoutManager = LinearLayoutManager(context)
        rvwProductsTransfer?.layoutManager = linearLayoutManager
        packingAdapter = PackingAdapter()
        packingAdapter?.setDataStorage(listProducts)
        rvwProductsTransfer?.adapter = packingAdapter
        packingAdapter?.setOnItemClickListener(this)
    }

    private fun onError(it: String) {
        showProgress(false)
        printOnDialogMessaging(it)
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

    override fun onClickDataListener(objectData: PedidosPicking) {
        println("objeData: ${Gson().toJson(objectData)}")
        startActivity(Intent(this, PackingDetActivity::class.java).apply {
            //putExtra(EndingActivity.EXTRA_ENTITY, guideRequest)
            putExtra("TranferPedido",objectData as Serializable)
            finish()
        })
    }
    fun showPackingDetail(objectData: PedidoDetail) {
        showProgress(false)
        println("objeData: ${Gson().toJson(objectData)}")
        /*var pedidoDetail = PedidosPicking(
            estado = objectData.data.estado,
            fecha = objectData.data.fecha,
            moneda = objectData.data.moneda,
            pedido = objectData.data.pedido,
            simbolo = objectData.data.simbolo,
            tienda = objectData.data.tienda,
            tipdoc = objectData.data.tipdoc,
            total = objectData.data.total
        )*/
        var listTransferDataResponse = ArrayList<TransferShowDetail>()
        for (data: Detalle in objectData.data!!.detalle!!){
            var transferShowDetail = TransferShowDetail()

            transferShowDetail.cantidad = data.cantidad
            transferShowDetail.descripcion =data.descripcion
            transferShowDetail.imei = data.imei
            transferShowDetail.producto = data.producto
            transferShowDetail.secuencial = data.secuencial
            transferShowDetail.cantidadPickada = data.cantidadpickada
            transferShowDetail.pesoUnitario = data.pesoUnitario
            transferShowDetail.pesoTotal = data.pesoTotal
            listTransferDataResponse.add(transferShowDetail)
        }

        var transferResponse = TransferDataResponse()
        transferResponse.numdoc = objectData.data?.pedido
        transferResponse.estado = objectData.data?.estado
        transferResponse.detalle = listTransferDataResponse
        transferResponse.fecha = objectData.data?.fecha
        transferResponse.tienda = objectData.data?.tienda
        transferResponse.observacion = "obs"

        startActivity(Intent(this, PackingDetActivity::class.java).apply {
            //putExtra(EndingActivity.EXTRA_ENTITY, guideRequest)
            putExtra("TranferPedido",transferResponse as Serializable)
            putExtra("PedidoDetail",objectData as Serializable)
            finish()
        })
    }
    private fun showMessageForCamPermission(messageType: Boolean = false) {
        AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
            .setTitle(R.string.app_name)
            .setMessage(if (messageType) R.string.cam_permission_request_message_dont_show else R.string.cam_permission_request_message)
            .setPositiveButton(R.string.ok) { _, _ ->
                if (!messageType)
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), SearchProductActivity.CAMERA_REQUEST_ID)
            }.show()
    }
    private fun productSearch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                showMessageForCamPermission()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), SearchProductActivity.CAMERA_REQUEST_ID)
            }
        } else {
            val integrator = IntentIntegrator(this)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            integrator.setPrompt("Escanear producto")
            integrator.setOrientationLocked(false)
            integrator.setBeepEnabled(true)
            integrator.setBarcodeImageEnabled(true)
            integrator.setRequestCode(114)
            integrator.initiateScan()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data)

        println("request_code: "+ requestCode + ", result_bar: "+result.contents)
        println("documento: ${tielTransferPedido.text.toString()}")
        tielTransferPedido.setText( result.contents ?: "")
        var transferRequest = PedidoRequest(
            fecha = dateOperacion,
            pedido = tielTransferPedido.text.toString(),
            usuario =  getSession().usuario)
        println("Documento")
        //packingViewModel.showPedidos(transferRequest,::showPackingDetail,::onError)
    }
}