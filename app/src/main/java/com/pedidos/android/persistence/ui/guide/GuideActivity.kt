package com.pedidos.android.persistence.ui.guide

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
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
import com.pedidos.android.persistence.model.guide.GuideDetail
import com.pedidos.android.persistence.model.guide.GuideRequest
import com.pedidos.android.persistence.model.guide.GuideResponse
import com.pedidos.android.persistence.ui.BasicApp
import com.pedidos.android.persistence.ui.ending.EndingActivity
import com.pedidos.android.persistence.ui.guide.fragment.OnClickListenerCustom
import com.pedidos.android.persistence.ui.menu.MenuActivity
import com.pedidos.android.persistence.ui.sale.SaleAdapter
import com.pedidos.android.persistence.ui.search.SearchProductActivity
import com.pedidos.android.persistence.utils.Defaults
import com.pedidos.android.persistence.utils.Formatter
import com.pedidos.android.persistence.utils.complementProductTempCode
import com.pedidos.android.persistence.viewmodel.GuideViewModel
import com.pedidos.android.persistence.viewmodel.SaleViewModel
import com.pedidos.android.persistence.viewmodel.SearchProductViewModel
import kotlinx.android.synthetic.main.fragment_guide.*
import kotlinx.android.synthetic.main.guide_head_activity.*
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

class GuideActivity : MenuActivity() {

    private lateinit var saleViewModel: GuideViewModel
    private lateinit var searchViewModel: SearchProductViewModel
    private lateinit var guideAdapter: GuideAdapter
    private lateinit var guideDetail : GuideDetail
     lateinit var listGuideDetail: MutableList<GuideDetail>
    private var dialog: AlertDialog? = null
    private var view: View? = null
    var parametros: Bundle? = null
    var guideRequest = GuideRequest()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentViewWithMenu(R.layout.guide_activity)
        setSupportActionBarMenu(toolbar)

        checkSession()
        listGuideDetail = ArrayList()

        parametros = intent.extras
        guideRequest = parametros!!.get("Guide") as GuideRequest
        println("data_guia_cab: ${Gson().toJson(guideRequest)}")

        rvwProducts.layoutManager = LinearLayoutManager(this)
        guideAdapter = GuideAdapter(mutableListOf()) { saleSubItem ->
           //  saleViewModel.deleteItem(saleSubItem)
        }
        guideAdapter.compProductActionCall = {complementProductTempCode = null}
        toolbar.title = "${getString(R.string.title_sale_tienda)} ${getSession().tienda}"
        toolbar.setNavigationIcon(R.drawable.ic_back_white_25)
       // setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            //this.onBackPressed()
            finish()
            startActivity(Intent(this, GuideHeadActivity::class.java))
        }
        textVersion.text = "Version : " + BasicApp.APP_VERSION
        rvwProducts.adapter = guideAdapter
        btnProcessGuide.setOnClickListener { processSale() }
        imbwAddProductCombined.setOnClickListener { productSearchCombined() }
        imbwAddProductoWithCamera.setOnClickListener { productSearch() }
        imbwAddProductManualOnly.setOnClickListener { productManualSearch() }
        //btnSelectClient.setOnClickListener { showClientPopUp() }

        //this init the viewModel
        val saleFactory = GuideViewModel.Companion.Factory(application, getSettings().urlbase)
        val searchFactory = SearchProductViewModel.Companion.Factory(application, getSettings().urlbase)
        saleViewModel = ViewModelProviders.of(this, saleFactory).get(GuideViewModel::class.java)
        searchViewModel = ViewModelProviders.of(this, searchFactory).get(SearchProductViewModel::class.java)
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

                    view = LayoutInflater.from(this).inflate(R.layout.search_imei_dialog, lltRoot, false)
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
                }
                else {
                   // addItem(productEntity)
                    //searchViewModel.checkAutomatically(productEntity)
                }
            } else if (productEntity.stimei2) {
                if (TextUtils.isEmpty(productEntity.imei2)) {
                    //request imei2
                    view = LayoutInflater.from(this).inflate(R.layout.search_imei_dialog, lltRoot, false)
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

    fun resultGuia(guideResponse: GuideResponse) {
        showProgress(false)
        println("data_guia_response: ${Gson().toJson(guideResponse)}")
        AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
            .setTitle(R.string.app_name)
            .setMessage("${guideResponse.message}")
            .setPositiveButton(R.string.aceptar) { d, _ ->
                if (guideResponse.result == true){
                    finish()
                    startActivity(Intent(this, GuideHeadActivity::class.java))
                }else {
                    d.dismiss()
                } }
            .setCancelable(false)
            .create().show()
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            SEARCH_REQUEST -> {
                if (Activity.RESULT_OK == resultCode) {
                    val productEntity: ProductEntity? = data?.getParcelableExtra(SearchProductActivity.PRODUCT_KEY)
                    if(productEntity != null) addItem(productEntity)
                }
            }

            GARANTIE_REQUEST -> {
                if (Activity.RESULT_OK == resultCode) {
                    val productEntityExt: ProductEntity? = data?.getParcelableExtra(SearchProductActivity.PRODUCT_KEY_EXT)
                    val productEntityDamage: ProductEntity? = data?.getParcelableExtra(SearchProductActivity.PRODUCT_KEY_DAMAGE)

                    if(productEntityExt != null) addItem(productEntityExt)
                    if(productEntityDamage != null)addItem(productEntityDamage)
                }
            }

            COMPLEMENTARY_REQUEST -> {
                if( Activity.RESULT_OK == resultCode) {
                    var sizeList = data!!.getIntExtra(SearchProductActivity.SIZE_PRODUCTS, 0)

                    for (index in 0 .. (sizeList - 1)) {
                        val productEntity: ProductEntity? = data?.getParcelableExtra(SearchProductActivity.PRODUCT_KEY+index)
                        if(productEntity != null) addItem(productEntity)
                    }
                }
            }
            113 -> {
                if (data != null) {
                    val result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data)
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
                            showProgress(true)
                            searchViewModel.searchResults.value?.imei = result.contents
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
            115 -> {
                if (data != null) {
                    val result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data)
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
                    val result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data)
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
                            showProgress(true)
                            searchViewModel.searchProductDirectly(result.contents ?: "")
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
                lastSecuencialOrDefault = saleViewModel.saleLiveData.value!!.productos[saleViewModel.saleLiveData.value!!.productos.size - 1].secuencial
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
        showProgress(true)
        saleViewModel.saveDetail(saleSubItem)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
                //(rvwProducts.adapter as GuideAdapter).items.removeAll { true }
                (rvwProducts.adapter as GuideAdapter).addItems(newItem.productos)
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
        tvwOrderNumber.text = entity.documento
        tvwOrderDate.text = entity.fecha //format
        tvwSaleTotal.text = Formatter.DoubleToString(entity.total, entity.monedaSimbolo)
        //tvwClient.text = "${entity.clienteCodigo} ${entity.clienteNombres}"
        etwAddProduct.text = Editable.Factory.getInstance().newEditable("")
    }

    private fun processSale() {
        if (saleViewModel.saleLiveData.value!!.productos.size == 0) {
            AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
                    .setTitle(R.string.app_name)
                    .setMessage(getString(R.string.sale_validation_no_products))
                    .setPositiveButton(R.string.aceptar) { d, _ -> d.dismiss() }
                    .setCancelable(false)
                    .create().show()
            return
        }

        showProgress(true)
       // saleViewModel.saveSale(::goToResumenPedido, ::onError)
        goToResumenPedido(saleViewModel.saleLiveData.value!!)
    }

    private fun goToResumenPedido(entity: SaleEntity) {
        //prevent nulls after back pressed
       // saleViewModel.saleLiveData.postValue(setSessionInfo(saleViewModel.saleLiveData.value!!))
        val currentSaleEntity : SaleEntity?  = saleViewModel.saleLiveData.value
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
        for (data in guideAdapter.items){
            guideDetail = GuideDetail()
            guideDetail.secuencial = data.secuencial
            guideDetail.cantidad = data.cantidad
            guideDetail.codigoitem = data.codigoProducto
            guideDetail.descripcionitem = data.descripcion
            guideDetail.imei = data.imei
            listGuideDetail.add(guideDetail)
        }

        showProgress(true)
        guideRequest.empresa = getSession().empresa
        guideRequest.usuario = getSession().usuario
         guideRequest.detalle = listGuideDetail
        println("data_guia: ${Gson().toJson(guideRequest)}")

        saleViewModel.saveGuide(guideRequest, ::resultGuia, ::onError)

       /* startActivity(Intent(this, GuideHeadActivity::class.java).apply {
            putExtra(EndingActivity.EXTRA_ENTITY, currentSaleEntity)
            putExtra("detailGuide",listGuideDetail as Serializable )
        })
*/
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

    private fun productSearchCombined() {
        val productCode = etwAddProduct.text.toString()
        if (!productCode.isNullOrEmpty()) {
            showProgress(true)

            searchViewModel.searchProductDirectly(productCode)
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
        val TAG = GuideActivity::class.java.simpleName!!
    }
}



