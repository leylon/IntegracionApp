package com.pedidos.android.persistence.ui.guide

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.jakewharton.rxbinding.widget.RxTextView
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.db.entity.ProductEntity
import com.pedidos.android.persistence.db.entity.SaleEntity
import com.pedidos.android.persistence.db.entity.SaleSubItemEntity
import com.pedidos.android.persistence.model.SaleSubItem
import com.pedidos.android.persistence.model.guide.GuideDetail
import com.pedidos.android.persistence.model.guide.GuideResponse
import com.pedidos.android.persistence.model.inventary.*
import com.pedidos.android.persistence.model.picking.*
import com.pedidos.android.persistence.model.transfer.*
import com.pedidos.android.persistence.ui.ending.EndingActivity
import com.pedidos.android.persistence.ui.guide.fragment.InventaryStatusPopUpFragment
import com.pedidos.android.persistence.ui.menu.MenuActivity
import com.pedidos.android.persistence.ui.search.SearchProductActivity
import com.pedidos.android.persistence.utils.Defaults
import com.pedidos.android.persistence.utils.Formatter
import com.pedidos.android.persistence.utils.complementProductTempCode
import com.pedidos.android.persistence.viewmodel.*
import kotlinx.android.synthetic.main.inventary_activity.tielDateGuide
import kotlinx.android.synthetic.main.sales_activity.*
import kotlinx.android.synthetic.main.sales_activity.etwAddProduct
import kotlinx.android.synthetic.main.sales_activity.fltLoading
import kotlinx.android.synthetic.main.sales_activity.imbwAddProductCombined
import kotlinx.android.synthetic.main.sales_activity.imbwAddProductManualOnly
import kotlinx.android.synthetic.main.sales_activity.imbwAddProductoWithCamera
import kotlinx.android.synthetic.main.sales_activity.rvwProducts
import kotlinx.android.synthetic.main.sales_activity.toolbar
import kotlinx.android.synthetic.main.search_imei_dialog.view.*
import kotlinx.android.synthetic.main.packing_detail_activity.*
import kotlinx.android.synthetic.main.packing_detail_activity.btnProcessTransfer
import kotlinx.android.synthetic.main.packing_detail_activity.tvwDateTransfer
import kotlinx.android.synthetic.main.packing_detail_activity.tvwDocumentoTransfer
import kotlinx.android.synthetic.main.packing_detail_activity.tvwTiendaTransfer

import rx.android.schedulers.AndroidSchedulers
import java.io.Serializable
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class PackingDetActivity : MenuActivity(), InventaryStatusPopUpFragment.newDialoglistenerAuxGuide, OnClickTransfer<SaleSubItem> {

    private lateinit var saleViewModel: GuideViewModel
    private lateinit var searchViewModel: SearchProductViewModel
    private lateinit var inventaryViewModel: InventaryViewModel
    private lateinit var guideAdapter: PackingDetailAdapter
    private lateinit var guideDetail: GuideDetail
    private lateinit var listInventary: ArrayList<InventaryItem>
    lateinit var listGuideDetail: MutableList<GuideDetail>
    lateinit var transferDataResponse:TransferDataResponse
    lateinit var pedidoDetail: PedidoDetail
     var inventaryResponse : ArrayList<TransferShowDetail>? = ArrayList()
    private var dialog: AlertDialog? = null
    private var view: View? = null
    var dayMin = 0
    var yearMin = 0
    var monthMin = 0
    var curDate: Calendar? = null
    var dateOperacion = ""
    var dataOption  = ""
    var parametros: Bundle? = null
    lateinit var tvwPackingTotal: TextView
    lateinit var btnExtornar: Button
    private lateinit var transferViewModel: TransferViewModel
    var items: ArrayList<SaleSubItem> = ArrayList()
    private lateinit var packingViewModel: PackingViewModel
    private var listPickingRequest: ArrayList<PickingRequest> =ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentViewWithMenu(R.layout.packing_detail_activity)
        setSupportActionBarMenu(toolbar)
        tvwPackingTotal = findViewById(R.id.tvwPackingTotal)
        btnExtornar = findViewById(R.id.btnExtornar)
        checkSession()

        listGuideDetail = ArrayList()
        listInventary = ArrayList()
        rvwProducts.layoutManager = LinearLayoutManager(this)
        guideAdapter = PackingDetailAdapter(mutableListOf()) { saleSubItem ->
            saleViewModel.deleteItem(saleSubItem)
        }
        parametros = intent.extras
        transferDataResponse = parametros!!.get("TranferPedido") as TransferDataResponse
        pedidoDetail = parametros!!.get("PedidoDetail") as PedidoDetail
        guideAdapter.compProductActionCall = { complementProductTempCode = null }
        toolbar.title = "${getString(R.string.title_packing_detail)} "
        rvwProducts.adapter = guideAdapter
        guideAdapter.setOnItemClickListener(this)
        println("PedidoDetail: ${Gson().toJson(pedidoDetail)}")
        tvwTiendaTransfer.text = transferDataResponse.tienda
        tvwDocumentoTransfer.text = transferDataResponse.numdoc
        tvwDateTransfer.text = transferDataResponse.fecha
        tvwPackingTotal.text = "${pedidoDetail.data?.simbolo!!} ${pedidoDetail.data?.total!!.toString()}"
        tvwPackingCliente.text = pedidoDetail.data?.nombre
        tvwPackingDocumento.text = "${pedidoDetail.data?.tipDocCliente} - ${pedidoDetail.data?.numDocClient}"
        tvwPackingTelefono.text = pedidoDetail.data?.telefonoCliente
        tvwPackingDireccion.text = pedidoDetail.data?.direccionCliente
        tvwPackingMail.text = pedidoDetail.data?.mailCliente
        tvwPackingPeso.text = pedidoDetail.data?.peso.toString()
        btnProcessTransfer.setOnClickListener { processInventario() }
        imbwAddProductCombined.setOnClickListener { productSearchCombined() }
        imbwAddProductoWithCamera.setOnClickListener { productSearch() }
        imbwAddProductManualOnly.setOnClickListener { productManualSearch() }
        //btnProcessList.setOnClickListener { showListInventary() }
        //btnSelectClient.setOnClickListener { showClientPopUp() }

        //this init the viewModel
        val saleFactory = GuideViewModel.Companion.Factory(application, getSettings().urlbase)
        val searchFactory =
            SearchProductViewModel.Companion.Factory(application, getSettings().urlbase)
        val invetaryFactory =
            InventaryViewModel.Companion.Factory(application, getSettings().urlbase)

        val transferFactory = TransferViewModel.Companion.Factory(application, getSettings().urlbase)
        transferViewModel = ViewModelProviders.of(this, transferFactory).get(TransferViewModel::class.java)

        saleViewModel = ViewModelProviders.of(this, saleFactory).get(GuideViewModel::class.java)
        searchViewModel =
            ViewModelProviders.of(this, searchFactory).get(SearchProductViewModel::class.java)
        inventaryViewModel =
            ViewModelProviders.of(this, invetaryFactory).get(InventaryViewModel::class.java)
        searchViewModel.searchResults.observe(this, Observer { checkResult(it) })
        searchViewModel.errorResults.observe(this, Observer { showError(it) })
        val packingFactory = PackingViewModel.Companion.Factory(application, getSettings().urlbase)
        packingViewModel = ViewModelProviders.of(this, packingFactory).get(PackingViewModel::class.java)

        subscribeToModel(saleViewModel)

        RxTextView.textChanges(etwAddProduct)
            .filter { it.length > 2 }
            .debounce(600, TimeUnit.MILLISECONDS)
            //.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                productSearchCombined()
            }

        btnExtornar.setOnClickListener { packingExtornar() }
        initSale()
        showListInventary()
    }

    private fun packingExtornar() {
        showProgress(true)
        AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
            .setTitle(R.string.app_name)
            .setMessage(getString(R.string.confirmacion_extorno_picking))
            .setPositiveButton(R.string.aceptar) { d , _ ->

                var pickingExtornoRequest = PickingExtornoRequest(
                    fecha = transferDataResponse.fecha!!,
                    pedido = transferDataResponse.numdoc!!,
                    usuario = getSession().usuario
                )
                packingViewModel.extornarPacking(pickingExtornoRequest,::responseExtornoPicking,::onError)
                listGuideDetail = ArrayList()
                listInventary = ArrayList()
                rvwProducts.layoutManager = LinearLayoutManager(this)
                guideAdapter = PackingDetailAdapter(mutableListOf()) { saleSubItem ->
                    saleViewModel.deleteItem(saleSubItem)
                }
                showListInventary()
                d.dismiss() }
            .setCancelable(false)
            .setNegativeButton(R.string.cancelar){
                d, _ ->
                showProgress(false)
                d.dismiss()
            }
            .create().show()
    }
    fun responseExtornoPicking(pedidoDetail: PickingExtornoTerminarResponse){
        showProgress(false)
        println("responseExtornoPicking: ${Gson().toJson(pedidoDetail)}")
        if (!pedidoDetail.result!!){
            pedidoDetail.message?.let { onError(it) }
        }
    }
    fun responseTerminarPicking(pedidoDetail: PickingTerminarResponse){
        showProgress(false)
        println("responseTerminarPicking: ${Gson().toJson(pedidoDetail)}")
        if (pedidoDetail.result!!){
            finish()
            startActivity(Intent(this, PackingActivity::class.java))
        }else{
            pedidoDetail.message?.let { onError(it) }
        }
    }

    override fun onBackPressed() {
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

    private fun showError(it: String?) {
        showProgress(false)
        if (it != null)
            printOnSnackBar(it)
    }

    private fun checkResult(productEntity: ProductEntity?) {
        if (productEntity != null) {
            if (productEntity.stimei) {
                //request IMEI
                if (TextUtils.isEmpty(productEntity.imei)) {

                    view = LayoutInflater.from(this)
                        .inflate(R.layout.search_imei_dialog, lltRoot, false)
                    dialog = AlertDialog.Builder(this)
                        .setView(view)
                        .setCancelable(false)
                        .setTitle(R.string.propt_imei_title)
                        .show()

                    view?.btnScan?.setOnClickListener {
                        val integrator = IntentIntegrator(this)
                        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                        integrator.setPrompt("ESCANEAR IMEI")
                        integrator.setOrientationLocked(false)
                        integrator.setBeepEnabled(true)
                        integrator.setBarcodeImageEnabled(true)
                        integrator.setRequestCode(113)
                        integrator.initiateScan()
                    }
                    view?.tvwAccept?.setOnClickListener {
                        fltLoading.visibility = View.VISIBLE

                        productEntity.imei = view?.edtImei?.text.toString()
                        // checkResult(productEntity)
                        //addItem(productEntity)
                        dialog?.dismiss()
                        searchViewModel.checkAutomaticallyGuide(productEntity)
                        addItem(productEntity)
                    }
                } else {
                    // addItem(productEntity)
                    //searchViewModel.checkAutomatically(productEntity)
                }
            } else if (productEntity.stimei2) {
                if (TextUtils.isEmpty(productEntity.imei2)) {
                    //request imei2
                    view = LayoutInflater.from(this)
                        .inflate(R.layout.search_imei_dialog, lltRoot, false)
                    dialog = AlertDialog.Builder(this)
                        .setView(view)
                        .setCancelable(false)
                        .setTitle(R.string.propt_imei_title2)
                        .show()

                    view?.btnScan?.setOnClickListener {
                        val integrator = IntentIntegrator(this)
                        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                        integrator.setPrompt("Escanear Imei 2")
                        integrator.setOrientationLocked(false)
                        integrator.setBeepEnabled(true)
                        integrator.setBarcodeImageEnabled(true)
                        integrator.setRequestCode(115)
                        integrator.initiateScan()
                    }
                    view?.tvwAccept?.setOnClickListener {
                        fltLoading.visibility = View.VISIBLE

                        productEntity.imei2 = view?.edtImei?.text.toString()
                        checkResult(productEntity)
                    }
                } else {
                    addItem(productEntity)
                }
            } else {
                // get product from search edittext
                complementProductTempCode = productEntity.codigo
                addItem(productEntity)
            }
        } else {
            showProgress(false)
            val intent = Intent(this, SearchProductActivity::class.java)
            startActivityForResult(intent, SEARCH_REQUEST)
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            SEARCH_REQUEST -> {
                if (Activity.RESULT_OK == resultCode) {
                    val productEntity: ProductEntity? =
                        data?.getParcelableExtra(SearchProductActivity.PRODUCT_KEY)
                    if (productEntity != null) addItem(productEntity)
                }
            }

            GARANTIE_REQUEST -> {
                if (Activity.RESULT_OK == resultCode) {
                    val productEntityExt: ProductEntity? =
                        data?.getParcelableExtra(SearchProductActivity.PRODUCT_KEY_EXT)
                    val productEntityDamage: ProductEntity? =
                        data?.getParcelableExtra(SearchProductActivity.PRODUCT_KEY_DAMAGE)

                    if (productEntityExt != null) addItem(productEntityExt)
                    if (productEntityDamage != null) addItem(productEntityDamage)
                }
            }

            COMPLEMENTARY_REQUEST -> {
                if (Activity.RESULT_OK == resultCode) {
                    var sizeList = data!!.getIntExtra(SearchProductActivity.SIZE_PRODUCTS, 0)

                    for (index in 0..(sizeList - 1)) {
                        val productEntity: ProductEntity? =
                            data?.getParcelableExtra(SearchProductActivity.PRODUCT_KEY + index)
                        if (productEntity != null) addItem(productEntity)
                    }
                }
            }
            113 -> {
                if (data != null) {
                    val result = IntentIntegrator.parseActivityResult(
                        IntentIntegrator.REQUEST_CODE,
                        resultCode,
                        data
                    )
                    if (result != null) {
                        if (result.contents == null) {
                            showProgress(false)

                            AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
                                .setTitle(R.string.app_name)
                                .setMessage("Lectura cancelada")
                                .setPositiveButton(R.string.aceptar) { d, _ -> d.dismiss() }
                                .setCancelable(false)
                                .create().show()
                        } else {
                           // showProgress(true)
                          //  searchViewModel.searchResults.value?.imei = result.contents
                           // checkResult(searchViewModel.searchResults.value)
                            var entity = ProductEntity()
                            entity.stimei = true
                            entity.imei = result.contents
                            actualizarInventario(entity)
                        }
                    } else {
                        showProgress(false)
                        Log.d(TAG, "result returned null")
                    }
                } else {
                    showProgress(false)
                    Log.d(TAG, "data returned null")
                }
            }
            115 -> {
                if (data != null) {
                    val result = IntentIntegrator.parseActivityResult(
                        IntentIntegrator.REQUEST_CODE,
                        resultCode,
                        data
                    )
                    if (result != null) {
                        if (result.contents == null) {
                            showProgress(false)

                            AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
                                .setTitle(R.string.app_name)
                                .setMessage("Lectura cancelada")
                                .setPositiveButton(R.string.aceptar) { d, _ -> d.dismiss() }
                                .setCancelable(false)
                                .create().show()
                        } else {
                            showProgress(false)
                            searchViewModel.searchResults.value?.imei2 = result.contents
                            checkResult(searchViewModel.searchResults.value)
                        }
                    } else {
                        showProgress(false)
                        Log.d(TAG, "result returned null")
                    }
                } else {
                    showProgress(false)
                    Log.d(TAG, "data returned null")
                }
            }
            114 -> {
                if (data != null) {
                    val result = IntentIntegrator.parseActivityResult(
                        IntentIntegrator.REQUEST_CODE,
                        resultCode,
                        data
                    )
                    if (result != null) {
                        if (result.contents == null) {
                            showProgress(false)
                            AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
                                .setTitle(R.string.app_name)
                                .setMessage("Lectura cancelada")
                                .setPositiveButton(R.string.aceptar) { d, _ -> d.dismiss() }
                                .setCancelable(false)
                                .create().show()
                        } else {
                            //showProgress(true)
                            inventaryViewModel.searchProductDirectly(result.contents ?: "",::actualizarInventario,::onError)
                        }
                    } else {
                        showProgress(false)
                        Log.d(TAG, "result returned null")
                    }
                } else {
                    showProgress(false)
                    Log.d(TAG, "data returned null")
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun addItem(productEntity: ProductEntity) {
        var lastSecuencialOrDefault = 0
        if (saleViewModel.saleLiveData.value != null) {
            if (saleViewModel.saleLiveData.value!!.productos.size > 0)
                lastSecuencialOrDefault =
                    saleViewModel.saleLiveData.value!!.productos[saleViewModel.saleLiveData.value!!.productos.size - 1].secuencial
        }
        //for ( transferDataResponse )
        val saleSubItem = SaleSubItemEntity().apply {
            secuencial = lastSecuencialOrDefault + 1
            codigoventa = productEntity.codigoVenta
            codigoProducto = productEntity.codigo
            descripcion = productEntity.descripcion
            cantidad = 1
            productoconcomplemento = 1
            precio = productEntity.precio
            imei = productEntity.imei
            imei2 = productEntity.imei2
            monedaSimbolo = productEntity.monedaSimbolo
            complementaryRowColor = productEntity.complementaryRowColor
            secgaraexte = productEntity.secgaraexte
            codgaraexte = productEntity.codgaraexte
            stimei = productEntity.stimei

        }
        /*guideDetail = GuideDetail()
        guideDetail.secuencial = saleSubItem.secuencial
        guideDetail.cantidad = saleSubItem.cantidad
        guideDetail.codigoitem = saleSubItem.codigoProducto
        guideDetail.descripcionitem = saleSubItem.descripcion
        guideDetail.imei = saleSubItem.imei

        listGuideDetail.add(guideDetail)*/
        items.add(saleSubItem)
        (rvwProducts.adapter as PackingDetailAdapter).deleteItems(items)
        (rvwProducts.adapter as PackingDetailAdapter).addItems(items)
        showProgress(false)
        //saleViewModel.saveDetail(saleSubItem)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isEmpty()) return
        when (requestCode) {
            SearchProductActivity.CAMERA_REQUEST_ID -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    productSearch()
                } else {
                    showMessageForCamPermission(true)
                }
            }
        }
    }

    private fun subscribeToModel(viewModel: GuideViewModel) {
        viewModel.saleLiveData.observe(this, Observer { newItem ->
            showProgress(false)
            if (newItem != null) {
                //(rvwProducts.adapter as _root_ide_package_.com.pedidos.android.persistence.ui.guide.PackingDetailAdapter).items.removeAll { true }
                (rvwProducts.adapter as PackingDetailAdapter).addItems(newItem.productos)
                //updateScreen(newItem)
            }
        })

        viewModel.showProgress.observe(this, Observer {
            showProgress(it!!)
        })

        viewModel.message.observe(this, Observer {
            if (it != null) {
                printOnDialogMessaging(it)
                //printOnSnackBar(it)
            }
        })
    }


    private fun processInventario() {


       /* var inventaryGenerateRequest= InventaryGenerateRequest()
        inventaryGenerateRequest.usuario = getSession().usuario
        inventaryGenerateRequest.opcion = "SIGUIENTE"
        inventaryGenerateRequest.tienda = transferDataResponse.tienda
        inventaryGenerateRequest.fecha = dateOperacion
        inventaryGenerateRequest.documento = transferDataResponse.numdoc
        inventaryGenerateRequest.idlista = 1
        var listaDetalleInventary: ArrayList<InventaryGenerateItem> = ArrayList()
        for (data in inventaryResponse!!){
            var inventaryGenerateItem = InventaryGenerateItem()
            inventaryGenerateItem.cantidad = data.cantidad!!.toDouble()
            inventaryGenerateItem.codigo = data.producto
            inventaryGenerateItem.descripcion = data.descripcion
            inventaryGenerateItem.imei = data.imei
            listaDetalleInventary.add(inventaryGenerateItem)
        }
        var inventaryResponseStatus = InventaryResponseStatus()
        inventaryResponseStatus.documento = transferDataResponse.numdoc
        inventaryResponseStatus.opcion = "SIGUIENTE"
        inventaryResponseStatus.result = true
        inventaryResponseStatus.message = transferDataResponse.observacion
        inventaryResponseStatus.idlista = 1
        inventaryResponseStatus.fecha = transferDataResponse.fecha
        var listStatusItem = ArrayList<InventaryStatusItem>()
        for (datos in guideAdapter.items) {
            var inventaryStatusItem = InventaryStatusItem()
            inventaryStatusItem.cantidad = datos.productoconcomplemento!!.toDouble()
            inventaryStatusItem.codigo = datos.codigoProducto
            inventaryStatusItem.descripcion = datos.descripcion
            inventaryStatusItem.imei = datos.imei
            listStatusItem.add(inventaryStatusItem)

        }
        inventaryResponseStatus.data = listStatusItem
        onStatusInventary(inventaryResponseStatus)*/
       // inventaryViewModel.generateInventary(inventaryGenerateRequest,::onStatusInventary, ::onError)*/
        var bulto = 0
        val builder = AlertDialog.Builder(this)
        builder.setTitle("¿Cuantos Bultos son?")
        builder.setItems(arrayOf("1", "2", "3")) { dialog, which ->
            // Realiza una acción según la opción seleccionada
            when (which) {
                0 -> bulto = 1
                1 -> bulto = 2
                2 -> bulto = 3
            }
            terminarPicking(bulto)
        }

        // Muestra el `AlertDialog`
        val dialog = builder.create()
        dialog.show()
        /*AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
            .setTitle(R.string.app_name)
            .setMessage(getString(R.string.confirmacion_terminar_picking))
            .setPositiveButton(R.string.aceptar) { d , _ ->

        var pickingExtornoRequest = PickingTerminarRequest(
            fecha = transferDataResponse.fecha!!,
            pedido = transferDataResponse.numdoc!!,
            ubicacion = "LOCKER-01",
            usuario = getSession().usuario,
            bulto = 0
        )

        packingViewModel.pickingTerminar(pickingExtornoRequest,::responseTerminarPicking,::onError)
            }.setCancelable(false)
            .setNegativeButton(R.string.cancelar){
                    d, _ ->
                showProgress(false)
                d.dismiss()
            }
            .create().show()*/
    }
    fun terminarPicking(bulto : Int){
        showProgress(true)

       //  PickingRequest

       /* for (item: SaleSubItem in items ){

            listPickingRequest.add(
                PickingRequest(
                    fecha = transferDataResponse.fecha!!,
                    pedido = transferDataResponse.numdoc!!,
                    producto = item.codigoProducto,
                    secuencial = item.secuencial,
                    signo = "",
                    usuario = getSession().usuario,
                    pesoUnitario = item.pesoUnitario,
                    pesoTotal = item.pesoTotal
                )
            )
        }*/

        var pickingExtornoRequest = PickingTerminarRequest(
            fecha = transferDataResponse.fecha!!,
            pedido = transferDataResponse.numdoc!!,
            ubicacion = "LOCKER-01",
            usuario = getSession().usuario,
            bulto = bulto,
            data = items
        )
        println("terminarRequest: ${Gson().toJson(pickingExtornoRequest)}")
        packingViewModel.pickingTerminar(pickingExtornoRequest,::responseTerminarPicking,::onError)
    }

    fun onStatusInventary(inventaryResponseStatus: InventaryResponseStatus){

        println("dataResponsetInventary: ${Gson().toJson(inventaryResponseStatus)}")
        showProgress(false)
        val dialgCustom = InventaryStatusPopUpFragment()
        dialgCustom.show(supportFragmentManager, "P")
        val args = Bundle()
        args.putSerializable("DataList", inventaryResponseStatus as Serializable)
        args.putString("Modulo","transfer")
        dialgCustom.arguments = args
        val fragment = supportFragmentManager.findFragmentByTag("P")
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
    }

    private fun goToResumenPedido(entity: SaleEntity) {
        //prevent nulls after back pressed
        // saleViewModel.saleLiveData.postValue(setSessionInfo(saleViewModel.saleLiveData.value!!))
        val currentSaleEntity: SaleEntity? = saleViewModel.saleLiveData.value
        /*if (currentSaleEntity != null) {
            //update fields
            currentSaleEntity.subTotal = entity.subTotal
            currentSaleEntity.descuento = entity.descuento
            currentSaleEntity.impuesto = entity.impuesto
            currentSaleEntity.impuesto2 = entity.impuesto2
            currentSaleEntity.impuesto3 = entity.impuesto3

            currentSaleEntity.nombreimpuesto1 = entity.nombreimpuesto1
            currentSaleEntity.nombreimpuesto2 = entity.nombreimpuesto2
            currentSaleEntity.nombreimpuesto3 = entity.nombreimpuesto3

            currentSaleEntity.total = entity.total

            currentSaleEntity.evento = entity.evento
            currentSaleEntity.productoconcomplemento = entity.productoconcomplemento
            currentSaleEntity.telefono = entity.telefono
            currentSaleEntity.email = saleViewModel.saleLiveData?.value?.email ?: ""
        }*/
        //end nulls prevent
        showProgress(false)
        for (data in guideAdapter.items) {
            guideDetail = GuideDetail()
            guideDetail.secuencial = data.secuencial
            guideDetail.cantidad = data.cantidad
            guideDetail.codigoitem = data.codigoProducto
            guideDetail.descripcionitem = data.descripcion
            guideDetail.imei = data.imei
            listGuideDetail.add(guideDetail)
        }


        startActivity(Intent(this, GuideHeadActivity::class.java).apply {
            putExtra(EndingActivity.EXTRA_ENTITY, currentSaleEntity)
            putExtra("detailGuide", listGuideDetail as Serializable)
        })

    }


    private fun productSearch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
                )
            ) {
                showMessageForCamPermission()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    SearchProductActivity.CAMERA_REQUEST_ID
                )
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

    private fun productSearchCombined() {
        val productCode = etwAddProduct.text.toString()
        if (!productCode.isNullOrEmpty()) {
            // showProgress(true)

           // searchViewModel.searchProductDirectly(productCode)
            inventaryViewModel.searchProductDirectly(productCode,::actualizarInventario,::onError)
        }
    }

    fun actualizarInventario(entity: ProductEntity ){
        showProgress(false)
        var bandera = false

        for (i in guideAdapter.items.indices){
            if (guideAdapter.items[i].codigoProducto == entity.codigo){
                bandera = true
                if (guideAdapter.items[i].stimei ) {
                    guideAdapter.items[i].stimei = true
                    view = LayoutInflater.from(this)
                        .inflate(R.layout.search_imei_dialog, lltRoot, false)
                    dialog = AlertDialog.Builder(this)
                        .setView(view)
                        .setCancelable(false)
                        .setTitle(R.string.propt_imei_title)
                        .show()

                    view?.btnScan?.setOnClickListener {

                        val integrator = IntentIntegrator(this)
                        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                        integrator.setPrompt("ESCANEAR IMEI")
                        integrator.setOrientationLocked(false)
                        integrator.setBeepEnabled(true)
                        integrator.setBarcodeImageEnabled(true)
                        integrator.setRequestCode(113)
                        integrator.initiateScan()
                    }
                    view?.tvwAccept?.setOnClickListener {
                       // fltLoading.visibility = View.VISIBLE
                        guideAdapter.items[i].stimei = true
                        guideAdapter.items[i].imei = view?.edtImei?.text.toString()
                        guideAdapter.notifyDataSetChanged()

                        // checkResult(productEntity)
                        //addItem(productEntity)
                        dialog?.dismiss()
                        //searchViewModel.checkAutomaticallyGuide(productEntity)
                       // addItem(productEntity)
                    }
                }
                println("encontrado...")
                /*if (view?.edtImei?.text.toString().length>0){
                    guideAdapter.items[i].imei = view?.edtImei?.text.toString()
                }*/

                guideAdapter.items[i].productoconcomplemento++
                guideAdapter.items[i].cantidadpickada++
                etwAddProduct.setText("")
                guideAdapter.notifyDataSetChanged()
                break
            }
        }
        if (!bandera){
            AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
                .setTitle(R.string.app_name)
                .setMessage("No se encontro el Producto.")
                .setPositiveButton("ok") { d, _ ->
                  //  addItem(entity)
                    d.dismiss() }

                .setCancelable(false)
                .create().show()
           // printOnSnackBarTop("El producto no está disponible en este inventario!")
        }
    }


    private fun productManualSearch() {
        val intent = Intent(this, SearchProductActivity::class.java)
        startActivityForResult(intent, SEARCH_REQUEST)
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


    private fun onError(message: String) {
        Log.e(TAG, message)
        printOnDialogMessaging(message)
/*
        AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
                .setTitle(R.string.app_name)
                .setMessage(message)
                .setPositiveButton(R.string.aceptar) { d, _ -> d.dismiss() }
                .setCancelable(false)
                .create().show()*/
    }
     fun showListInventary(){

         //if (listInventary.size == 0) {
             showProgress(true)
            var transferRequest =  TransferShowRequest()
             transferRequest.usuario = getSession().usuario
             transferRequest.tienda = transferDataResponse.tienda
             transferRequest.tipdoc  = transferDataResponse.tipdoc
             transferRequest.numdoc = transferDataResponse.numdoc
            // inventaryViewModel.listInventary(transferRequest, ::putDataInventary, ::onError)
         //transferViewModel.showProductTransfer(transferRequest, ::putDataInventary, ::onError)
         var listProduct =  TransferShowResponse()

         listProduct.result = true
         listProduct.message = ""
         listProduct.data = transferDataResponse

         putDataInventary(listProduct)
         //}
    }

    fun putDataInventary(listProduct: TransferShowResponse){
        showProgress(false)
        if (listProduct.result == true){
            rvwProducts.layoutManager = LinearLayoutManager(this)
            guideAdapter = PackingDetailAdapter(mutableListOf()) { saleSubItem ->
                saleViewModel.deleteItem(saleSubItem)
            }
            rvwProducts.adapter = guideAdapter
            inventaryResponse = listProduct.data?.detalle as ArrayList<TransferShowDetail>

            println("dataInventary: ${Gson().toJson(listProduct)}")
            items = ArrayList()
            for (data in inventaryResponse!!){
                //var saleSubItem = SaleSubItem()
                val saleSubItem = SaleSubItemEntity().apply {
                    secuencial = data.secuencial!!
                    codigoProducto = data.producto!!
                    descripcion = data.descripcion!!
                    cantidad = data.cantidad!!.toInt()
                    imei = data.imei!!
                    stimei = false
                    pesoUnitario = data.pesoUnitario!!
                    pesoTotal = data.pesoTotal!!
                    cantidadpickada = data.cantidadPickada!!

                }
                items.add(saleSubItem)
            }
            println("items: ${Gson().toJson(items)}")
            (rvwProducts.adapter as PackingDetailAdapter).deleteItems(items)
            (rvwProducts.adapter as PackingDetailAdapter).addItems(items)
            guideAdapter.setOnItemClickListener(this)
        } else {
            AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
                .setTitle("Ventas SKM")
                .setMessage(listProduct.message.toString())
                .setPositiveButton(R.string.aceptar) { d, _ -> d.dismiss() }
                .setCancelable(false)
                .create().show()
        }


    }

    private fun showProgress(show: Boolean) {
        fltLoading.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun initSale() {
        val newSaleEntity = SaleEntity()
        setSessionInfo(newSaleEntity)
        newSaleEntity.fecha = Formatter.DateToString(Date())
        newSaleEntity.clienteCodigo = Defaults.Cliente.documentNumber
        newSaleEntity.clienteNombres = Defaults.Cliente.fullName
        newSaleEntity.clienteTipoDocumento = Defaults.Cliente.identityDocumentType
        newSaleEntity.email = Defaults.Cliente.email
        newSaleEntity.telefono = Defaults.Cliente.phone
        saleViewModel.saleLiveData.postValue(newSaleEntity)
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

    companion object {
        private const val SEARCH_REQUEST = 1234
        const val COMPLEMENTARY_REQUEST = 543
        const val GARANTIE_REQUEST = 544
        val TAG = PackingDetActivity::class.java.simpleName!!
    }

    override fun closeDialogDriveGuide(data: InventaryResponseStatus) {
        println("dataPopPupStatus: ${Gson().toJson(data)}")
        //inventaryResponse.documento = data.documento

        dataOption = data.opcion!!
        var inventaryGenerateRequest= InventaryGenerateRequest()
        inventaryGenerateRequest.usuario = getSession().usuario
        inventaryGenerateRequest.opcion = data.opcion
        inventaryGenerateRequest.tienda = getSession().tienda
        inventaryGenerateRequest.fecha = dateOperacion
        inventaryGenerateRequest.documento = data.documento
        inventaryGenerateRequest.idlista = data.idlista
        var listaDetalleInventary: ArrayList<InventaryGenerateItem> = ArrayList()
        for (data in data.data){
            var inventaryGenerateItem = InventaryGenerateItem()
            inventaryGenerateItem.cantidad = data.cantidad
            inventaryGenerateItem.codigo = data.codigo
            inventaryGenerateItem.descripcion = data.descripcion
            inventaryGenerateItem.imei = data.imei
            listaDetalleInventary.add(inventaryGenerateItem)
        }
        inventaryGenerateRequest.detalle = listaDetalleInventary
        println("dataRequestInventary: ${Gson().toJson(inventaryGenerateRequest)}")
        var transferFinishRequest = TransferFinishRequest()
        transferFinishRequest.numdoc = transferDataResponse.numdoc
        transferFinishRequest.tienda = transferDataResponse.tienda
        transferFinishRequest.tipdoc = transferDataResponse.tipdoc
        transferFinishRequest.usuario = getSession().usuario
        var listTransfer = ArrayList<TransferShowDetail>()
        for (datos in guideAdapter.items) {
            var inventaryStatusItem = TransferShowDetail()
            inventaryStatusItem.cantidad = datos.productoconcomplemento
            inventaryStatusItem.producto = datos.codigoProducto
            inventaryStatusItem.descripcion = datos.descripcion
            inventaryStatusItem.imei = datos.imei
            listTransfer.add(inventaryStatusItem)

        }
        transferFinishRequest.detalle = listTransfer
        if (dataOption == "FINALIZAR"){
            transferViewModel.finishTransfer(transferFinishRequest,::onStatusPopUp, ::onError)
        }

       // inventaryViewModel.generateInventary(inventaryGenerateRequest,::onStatusPopUp, ::onError)
    }

    fun onStatusPopUp(data: GuideResponse){
        var statusMessage = ""
        println("onStatusPopUp: ${Gson().toJson(data)}")

        if(data.result!!){
            when(dataOption){
                "FINALIZAR" -> {
                    statusMessage = "SE REGISTRO INVENTARIO"

                }
                "MASTARDE" -> {
                    statusMessage = "SE REGISTRO PARCIALMENTE EL INVENTARIO"
                }
            }
            AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
                .setTitle("Ventas SKM")
                .setMessage(statusMessage)
                .setPositiveButton(R.string.aceptar) { d, _ -> finish()
                    d.dismiss()
                    startActivity(Intent(this, TransferPedidoActivity::class.java))
                }
                .setCancelable(false)
                .create().show()
        }else {
            AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
                .setTitle("Ventas SKM")
                .setMessage(data.message.toString())
                .setPositiveButton(R.string.aceptar) { d, _ -> d.dismiss() }
                .setCancelable(false)
                .create().show()
        }

    }

    override fun onClickDataListener(objectData: SaleSubItem) {
        showProgress(true)
        println("onClickDataListener: SaleSubItem: ${Gson().toJson(objectData)}")
        var pickingRequest = PickingRequest(
          fecha = transferDataResponse.fecha!!,
          pedido = transferDataResponse.numdoc!!,
          producto = objectData.codigoProducto,
          secuencial = objectData.secuencial,
          signo = objectData.saleCodigo,
          usuario = getSession().usuario,
          pesoTotal = objectData.pesoTotal,
          pesoUnitario = objectData.pesoUnitario

        )
        println("onClickDataListener: PickingRequest: ${Gson().toJson(pickingRequest)}")
        packingViewModel.picking(pickingRequest,::responsePicking,::onError)
    }
    fun responsePicking(pedidoDetail: PedidoDetail){
        showProgress(false)
        println("responsePicking: Terminar: ${Gson().toJson(pedidoDetail)}")
        listPickingRequest = ArrayList()
       /* if (pedidoDetail.result!!){
            for (item: Detalle in pedidoDetail.data?.detalle!!){
                listPickingRequest.add(
                    PickingRequest(
                        fecha = transferDataResponse.fecha!!,
                        pedido = transferDataResponse.numdoc!!,
                        producto = item.producto!!,
                        secuencial = item.secuencial!!,
                        signo = "",
                        usuario = getSession().usuario,
                        pesoUnitario = item.pesoUnitario!!,
                        pesoTotal = item.pesoTotal!!
                    )
                )
            }
        }*/
        println("responsePicking: listPickingRequest: ${Gson().toJson(listPickingRequest)}")

    }
}



