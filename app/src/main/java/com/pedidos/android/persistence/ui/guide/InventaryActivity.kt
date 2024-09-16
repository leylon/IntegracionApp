package com.pedidos.android.persistence.ui.guide

import android.Manifest
import android.annotation.SuppressLint
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
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.jakewharton.rxbinding.widget.RxTextView
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.db.entity.ProductEntity
import com.pedidos.android.persistence.db.entity.SaleEntity
import com.pedidos.android.persistence.db.entity.SaleSubItemEntity
import com.pedidos.android.persistence.model.SaleSubItem
import com.pedidos.android.persistence.model.guide.GuideDetail
import com.pedidos.android.persistence.model.inventary.*
import com.pedidos.android.persistence.ui.BasicApp
import com.pedidos.android.persistence.ui.ending.EndingActivity
import com.pedidos.android.persistence.ui.guide.fragment.InventaryStatusPopUpFragment
import com.pedidos.android.persistence.ui.guide.fragment.OnClickListenerCustom
import com.pedidos.android.persistence.ui.guide.fragment.PlacaPopUpFragment
import com.pedidos.android.persistence.ui.menu.MenuActivity
import com.pedidos.android.persistence.ui.sale.SaleAdapter
import com.pedidos.android.persistence.ui.search.SearchProductActivity
import com.pedidos.android.persistence.utils.Defaults
import com.pedidos.android.persistence.utils.Formatter
import com.pedidos.android.persistence.utils.complementProductTempCode
import com.pedidos.android.persistence.viewmodel.GuideViewModel
import com.pedidos.android.persistence.viewmodel.InventaryViewModel
import com.pedidos.android.persistence.viewmodel.SaleViewModel
import com.pedidos.android.persistence.viewmodel.SearchProductViewModel
import kotlinx.android.synthetic.main.fragment_guide.*
import kotlinx.android.synthetic.main.fragment_guide.btnProcessGuide
import kotlinx.android.synthetic.main.guide_head_activity.*
import kotlinx.android.synthetic.main.inventary_activity.*
import kotlinx.android.synthetic.main.inventary_activity.tielDateGuide
import kotlinx.android.synthetic.main.sales_activity.*
import kotlinx.android.synthetic.main.sales_activity.etwAddProduct
import kotlinx.android.synthetic.main.sales_activity.fltLoading
import kotlinx.android.synthetic.main.sales_activity.imbwAddProductCombined
import kotlinx.android.synthetic.main.sales_activity.imbwAddProductManualOnly
import kotlinx.android.synthetic.main.sales_activity.imbwAddProductoWithCamera
import kotlinx.android.synthetic.main.sales_activity.rvwProducts
import kotlinx.android.synthetic.main.sales_activity.textVersion
import kotlinx.android.synthetic.main.sales_activity.toolbar
import kotlinx.android.synthetic.main.sales_activity.tvwOrderDate
import kotlinx.android.synthetic.main.sales_activity.tvwOrderNumber
import kotlinx.android.synthetic.main.sales_activity.tvwSaleTotal
import kotlinx.android.synthetic.main.search_imei_dialog.view.*
import rx.android.schedulers.AndroidSchedulers
import java.io.Serializable
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class InventaryActivity : MenuActivity(), InventaryStatusPopUpFragment.newDialoglistenerAuxGuide {

    private lateinit var saleViewModel: GuideViewModel
    private lateinit var searchViewModel: SearchProductViewModel
    private lateinit var inventaryViewModel: InventaryViewModel
    private lateinit var guideAdapter: InventaryAdapter
    private lateinit var guideDetail: GuideDetail
    private lateinit var listInventary: ArrayList<InventaryItem>
    lateinit var listGuideDetail: MutableList<GuideDetail>
     var inventaryResponse = InventaryResponse()
    private var dialog: AlertDialog? = null
    private var view: View? = null
    var dayMin = 0
    var yearMin = 0
    var monthMin = 0
    var curDate: Calendar? = null
    var dateOperacion = ""
    var dataOption  = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentViewWithMenu(R.layout.inventary_activity)
        setSupportActionBarMenu(toolbar)

        checkSession()
        setFechaOpe()
        listGuideDetail = ArrayList()
        listInventary = ArrayList()
        rvwProducts.layoutManager = LinearLayoutManager(this)
        guideAdapter = InventaryAdapter(mutableListOf()) { saleSubItem ->
            saleViewModel.deleteItem(saleSubItem)
        }
        guideAdapter.compProductActionCall = { complementProductTempCode = null }
        toolbar.title = "${getString(R.string.title_sale_tienda)} ${getSession().tienda}"
        textVersion.text = "Version : " + BasicApp.APP_VERSION
        rvwProducts.adapter = guideAdapter
        tielDateGuide.setOnClickListener { showDatePickerDialog() } //imbSearchDate
        imbSearchDateInventary.setOnClickListener { showDatePickerDialog() }
        btnProcessGuide.setOnClickListener { processInventario() }
        imbwAddProductCombined.setOnClickListener { productSearchCombined() }
        imbwAddProductoWithCamera.setOnClickListener { productSearch() }
        imbwAddProductManualOnly.setOnClickListener { productManualSearch() }
        btnProcessList.setOnClickListener { showListInventary() }
        //btnSelectClient.setOnClickListener { showClientPopUp() }

        //this init the viewModel
        val saleFactory = GuideViewModel.Companion.Factory(application, getSettings().urlbase)
        val searchFactory =
            SearchProductViewModel.Companion.Factory(application, getSettings().urlbase)
        val invetaryFactory =
            InventaryViewModel.Companion.Factory(application, getSettings().urlbase)

        saleViewModel = ViewModelProviders.of(this, saleFactory).get(GuideViewModel::class.java)
        searchViewModel =
            ViewModelProviders.of(this, searchFactory).get(SearchProductViewModel::class.java)
        inventaryViewModel =
            ViewModelProviders.of(this, invetaryFactory).get(InventaryViewModel::class.java)
        searchViewModel.searchResults.observe(this, Observer { checkResult(it) })
        searchViewModel.errorResults.observe(this, Observer { showError(it) })

        subscribeToModel(saleViewModel)

        RxTextView.textChanges(etwAddProduct)
            .filter { it.length > 2 }
            .debounce(600, TimeUnit.MILLISECONDS)
            //.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                productSearchCombined()
            }

        initSale()
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

        val saleSubItem = SaleSubItemEntity().apply {
            secuencial = lastSecuencialOrDefault + 1
            codigoventa = productEntity.codigoVenta
            codigoProducto = productEntity.codigo
            descripcion = productEntity.descripcion
            cantidad = 1
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
                //(rvwProducts.adapter as _root_ide_package_.com.pedidos.android.persistence.ui.guide.InventaryAdapter).items.removeAll { true }
                (rvwProducts.adapter as InventaryAdapter).addItems(newItem.productos)
                updateScreen(newItem)
            }
        })

        viewModel.showProgress.observe(this, Observer {
            showProgress(it!!)
        })

        viewModel.message.observe(this, Observer {
            if (it != null) {
                printOnSnackBar(it)
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun updateScreen(entity: SaleEntity) {
        //  tvwOrderNumber.text = entity.documento
        tvwOrderDate.text = entity.fecha //format
        tvwSaleTotal.text = Formatter.DoubleToString(entity.total, entity.monedaSimbolo)
        //tvwClient.text = "${entity.clienteCodigo} ${entity.clienteNombres}"
        etwAddProduct.text = Editable.Factory.getInstance().newEditable("")
    }



    private fun processInventario() {
        showProgress(true)
        var inventaryGenerateRequest= InventaryGenerateRequest()
        inventaryGenerateRequest.usuario = getSession().usuario
        inventaryGenerateRequest.opcion = "SIGUIENTE"
        inventaryGenerateRequest.tienda = getSession().tienda
        inventaryGenerateRequest.fecha = dateOperacion
        inventaryGenerateRequest.documento = inventaryResponse.documento
        inventaryGenerateRequest.idlista = inventaryResponse.idlista
        var listaDetalleInventary: ArrayList<InventaryGenerateItem> = ArrayList()
        for (data in guideAdapter.items){
            var inventaryGenerateItem = InventaryGenerateItem()
            inventaryGenerateItem.cantidad = data.cantidad.toDouble()
            inventaryGenerateItem.codigo = data.codigoProducto
            inventaryGenerateItem.descripcion = data.descripcion
            inventaryGenerateItem.imei = data.imei
            listaDetalleInventary.add(inventaryGenerateItem)
        }
        inventaryGenerateRequest.detalle = listaDetalleInventary
        println("dataRequestInventary: ${Gson().toJson(inventaryGenerateRequest)}")
        inventaryViewModel.generateInventary(inventaryGenerateRequest,::onStatusInventary, ::onError)
    }

    fun onStatusInventary(inventaryResponseStatus: InventaryResponseStatus){

        println("dataResponsetInventary: ${Gson().toJson(inventaryResponseStatus)}")
        showProgress(false)
        val dialgCustom = InventaryStatusPopUpFragment()
        dialgCustom.show(supportFragmentManager, "P")
        val args = Bundle()
        args.putSerializable("DataList", inventaryResponseStatus as Serializable)
        args.putString("Modulo","inventary")
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

                guideAdapter.items[i].cantidad++
                etwAddProduct.setText("")
                guideAdapter.notifyDataSetChanged()
                break
            }
        }
        if (!bandera){
            printOnSnackBarTop("El producto no está disponible en este inventario!")
        }
    }
    fun findImeiInventario(entity: ProductEntity ){
        showProgress(false)
        var bandera = false

        for (i in guideAdapter.items.indices){
                if (guideAdapter.items[i].codigoProducto == entity.codigo ){
                if (guideAdapter.items[i].stimei && guideAdapter.items[i].imei == entity.imei) {
                    printOnSnackBarTop("El producto ya esta en el inventario!")

                }else {

                }
               // println("encontrado...")
                /*if (view?.edtImei?.text.toString().length>0){
                    guideAdapter.items[i].imei = view?.edtImei?.text.toString()
                }*/
                /*
                guideAdapter.items[i].cantidad++
                etwAddProduct.setText("")
                guideAdapter.notifyDataSetChanged()*/
                    break
                break
            }
        }
        if (!bandera){
            printOnSnackBarTop("El producto no está disponible en este inventario!")
        }
    }
    fun actualizarInventarioCheck(entity: ProductEntity,input_imei: String ){
        showProgress(false)
        var bandera = false

        for (i in guideAdapter.items.indices){
            if (guideAdapter.items[i].codigoProducto == entity.codigo){
                bandera = true
                if (guideAdapter.items[i].stimei ) {
                    guideAdapter.items[i].imei = ""
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

                        guideAdapter.items[i].imei = view?.edtImei?.text.toString()
                        guideAdapter.items[i].stimei = true
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

                guideAdapter.items[i].cantidad++
                etwAddProduct.setText("")
                guideAdapter.notifyDataSetChanged()
                break
            }
        }
        if (!bandera){
            printOnSnackBarTop("El producto no está disponible en este inventario!")
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

        AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
                .setTitle(R.string.app_name)
                .setMessage(message)
                .setPositiveButton(R.string.aceptar) { d, _ -> d.dismiss() }
                .setCancelable(false)
                .create().show()
    }
     fun showListInventary(){

         //if (listInventary.size == 0) {
             showProgress(true)
             var inventaryRequest = InventaryRequest()
             inventaryRequest.usuario = getSession().usuario
             inventaryRequest.tienda = getSession().tienda
             inventaryRequest.fecha = dateOperacion
             inventaryViewModel.listInventary(inventaryRequest, ::putDataInventary, ::onError)
         //}
    }

    fun putDataInventary(listProduct: InventaryResponse){
        showProgress(false)
        if (listProduct.result == true){
            rvwProducts.layoutManager = LinearLayoutManager(this)
            guideAdapter = InventaryAdapter(mutableListOf()) { saleSubItem ->
                saleViewModel.deleteItem(saleSubItem)
            }
            rvwProducts.adapter = guideAdapter
            inventaryResponse = listProduct
            if (inventaryResponse.documento!!.isNotEmpty()){
                tvwDocumentInventary.text = "Documento: ${inventaryResponse.documento} "
            }
            listInventary = ArrayList()
            listInventary = listProduct.data as ArrayList<InventaryItem>
            println("dataInventary: ${Gson().toJson(listProduct)}")
            var items: ArrayList<SaleSubItem> = ArrayList()
            for (data in listInventary){
                //var saleSubItem = SaleSubItem()
                val saleSubItem = SaleSubItemEntity().apply {
                    codigoProducto = data.codigo!!
                    descripcion = data.descripcion!!
                    cantidad = data.cantidad!!.toInt()
                    imei = data.imei!!
                    stimei = data.serie!!
                }
                items.add(saleSubItem)
            }
            println("items: ${Gson().toJson(items)}")
            (rvwProducts.adapter as InventaryAdapter).deleteItems(items)
            (rvwProducts.adapter as InventaryAdapter).addItems(items)
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
        val TAG = InventaryActivity::class.java.simpleName!!
    }

    override fun closeDialogDriveGuide(data: InventaryResponseStatus) {
        println("dataPopPupStatus: ${Gson().toJson(data)}")
        inventaryResponse.documento = data.documento
        tvwDocumentInventary.text = "Documento: ${data.documento} "
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
        inventaryViewModel.generateInventary(inventaryGenerateRequest,::onStatusPopUp, ::onError)
    }

    fun onStatusPopUp(data: InventaryResponseStatus){
        var statusMessage = ""
        println("onStatusPopUp: ${Gson().toJson(data)}")
        inventaryResponse.documento = data.documento
        tvwDocumentInventary.text = "Documento: ${data.documento} "
        if(data.result!!){
            when(dataOption){
                "FINILIZAR" -> {
                    statusMessage = "SE REGISTRO INVENTARIO"
                    finish()
                }
                "MASTARDE" -> {
                    statusMessage = "SE REGISTRO PARCIALMENTE EL INVENTARIO"
                }
            }
            AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
                .setTitle("Ventas SKM")
                .setMessage(statusMessage)
                .setPositiveButton(R.string.aceptar) { d, _ -> d.dismiss() }
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
}



