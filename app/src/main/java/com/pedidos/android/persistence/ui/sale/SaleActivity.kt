package com.pedidos.android.persistence.ui.sale

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.jakewharton.rxbinding.widget.RxTextView
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.db.entity.ProductEntity
import com.pedidos.android.persistence.db.entity.SaleEntity
import com.pedidos.android.persistence.db.entity.SaleSubItemEntity
import com.pedidos.android.persistence.model.SaleSubItem
import com.pedidos.android.persistence.model.SelectedCreditCard
import com.pedidos.android.persistence.model.guide.DataResponse
import com.pedidos.android.persistence.model.pluging.Linea
import com.pedidos.android.persistence.model.pluging.PluginDataResponse
import com.pedidos.android.persistence.model.sale.*
import com.pedidos.android.persistence.model.transfer.TransferDataResponse
import com.pedidos.android.persistence.ui.BasicApp
import com.pedidos.android.persistence.ui.ClientPopUpFragment
import com.pedidos.android.persistence.ui.ending.EndingActivity
import com.pedidos.android.persistence.ui.guide.fragment.CityPopUpFragment
import com.pedidos.android.persistence.ui.menu.MenuActivity
import com.pedidos.android.persistence.ui.sale.fragment.QuestionPopUpFragment
import com.pedidos.android.persistence.ui.sale.fragment.SendCodPopUpFragment
import com.pedidos.android.persistence.ui.search.SearchProductActivity
import com.pedidos.android.persistence.utils.Defaults
import com.pedidos.android.persistence.utils.Formatter
import com.pedidos.android.persistence.utils.complementProductTempCode
import com.pedidos.android.persistence.viewmodel.SaleViewModel
import com.pedidos.android.persistence.viewmodel.SearchProductViewModel
import kotlinx.android.synthetic.main.sales_activity.*
import kotlinx.android.synthetic.main.search_imei_dialog.view.*
import rx.android.schedulers.AndroidSchedulers
import java.io.Serializable
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class SaleActivity : MenuActivity(), QuestionPopUpFragment.newDialoglistenerQuestion,SendCodPopUpFragment.newDialoglistenerSendCod {

    private lateinit var saleViewModel: SaleViewModel
    private lateinit var searchViewModel: SearchProductViewModel
    private lateinit var saleAdapter: SaleAdapter
    lateinit var ventaProductoResponse: VentaProductoResponse
    lateinit var envioCorreoResponse: EnvioCorreoResponse
    private var listProdu = ArrayList<VentaProductoRequest>()
    private var dialog: AlertDialog? = null
    private var view: View? = null
    lateinit var  dialgCustomSenCod: SendCodPopUpFragment
    var flag_pop: Boolean = false
    var parametros: Bundle? = null
    var pluginDataResponseData: PluginDataResponse? = null
    var secuencialOtro = 0
    var sizeProductoSearch = 0
    var listSaleSubItem: MutableList<SaleSubItem> = mutableListOf()
    lateinit var dataItemLinea: Linea

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentViewWithMenu(R.layout.sales_activity)
        setSupportActionBarMenu(toolbar)


        checkSession()

        rvwProducts.layoutManager = LinearLayoutManager(this)
        saleAdapter = SaleAdapter(mutableListOf()) { saleSubItem ->
            saleViewModel.deleteItem(saleSubItem)
        }
        saleAdapter.compProductActionCall = {complementProductTempCode = null}
        toolbar.title = "${getString(R.string.title_sale_tienda)} ${getSession().tienda}"
        textVersion.text = "Version : " + BasicApp.APP_VERSION
        rvwProducts.adapter = saleAdapter
        val cont = 0
        btnProcess.setOnClickListener {
            println("click.."+cont.inc())
            btnProcess.isEnabled = false
            validaProssesSale()} //processSale() }
        imbwAddProductCombined.setOnClickListener { productSearchCombined() }
        imbwAddProductoWithCamera.setOnClickListener {
            flag_pop = false
            productSearch() }
        imbwAddProductManualOnly.setOnClickListener { productManualSearch() }
        btnSelectClient.setOnClickListener { showClientPopUp() }

        //this init the viewModel
        val saleFactory = SaleViewModel.Companion.Factory(application, getSettings().urlbase)
        val searchFactory = SearchProductViewModel.Companion.Factory(application, getSettings().urlbase)
        saleViewModel = ViewModelProviders.of(this, saleFactory).get(SaleViewModel::class.java)
        searchViewModel = ViewModelProviders.of(this, searchFactory).get(SearchProductViewModel::class.java)
        searchViewModel.searchResults.observe(this, Observer {
            checkResult(it, dataItemLinea)
        })
        searchViewModel.errorResults.observe(this, Observer { showError(it) })

        subscribeToModel(saleViewModel)

  /*      RxTextView.textChanges(etwAddProduct)
                .filter { it.length > 2 }
                .debounce(600, TimeUnit.MILLISECONDS)
                //.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    productSearchCombined()
                }
*/
        initSale()
        parametros = intent.extras

        if (parametros != null){
            //if (!parametros!!.getBoolean("acction")){
             //   onBackPressed()
            //}else{
            Log.v("ley", "transferDataResponse: ${Gson().toJson(parametros!!.getString("PLUGIN_DATA"))}")
            pluginDataResponseData  = Gson().fromJson(parametros!!.getString("PLUGIN_DATA"), PluginDataResponse::class.java) // parametros!!.get("PLUGIN_DATA") as PluginDataResponse
            Log.v("ley","Ley Detalle: ${Gson().toJson(pluginDataResponseData?.lineas)}")
            if (getActiveLog() == 1){
                Log.v("ley","Ley Detalle: ${Gson().toJson(pluginDataResponseData?.lineas)}")
                messageLog(Gson().toJson(parametros!!.getString("PLUGIN_DATA")))
            }
            //
            if(pluginDataResponseData?.lineas?.size!!>0) {
                    sizeProductoSearch = pluginDataResponseData?.lineas?.size!!
                    saleViewModel.run {
                        for (dataResponse in pluginDataResponseData?.lineas!!) {
                            //  showProgress(true)

                            val productEntity = ProductEntity()
                            productEntity.codigo = dataResponse.referencia
                            productEntity.cantidad = dataResponse.unidades
                            productEntity.precio = dataResponse.precioiva
                            productEntity.imei = dataResponse.color

                            //productEntity.de = dataResponse.dto
                            secuencialOtro = dataResponse.numlinea
                            /*productEntity.descripcion = dataResponse.descripcion
                            productEntity.precio = dataResponse.preciobruto
                            productEntity.monedaSimbolo = dataResponse.
                            productEntity.stimei = dataResponse.stimei
                            productEntity.stimei2 = dataResponse.stimei2
                            productEntity.codigoVenta = dataResponse.codigoVenta
                            productEntity.complementaryRowColor = dataResponse.complementaryRowColor
                            productEntity.secgaraexte = dataResponse.secgaraexte
                            productEntity.codgaraexte = dataResponse.codgaraexte*/

                            productSearchCombined(dataResponse)
                            // searchViewModel?.searchProduct()

                        }
                    }

                }

           // }
        }else{
            println("no hay parametros!!!")
        }
    }



    override fun onBackPressed() {
       /* val resultIntent = Intent()
        resultIntent.putExtra("PLUGIN_RESPONSE", "{\n" +
                "\t\"msg\": \"\",\n" +
                "\t\"borr_articulos\": [\n" +
                "\t\t{\n" +
                "\t\t\t\"referencia\": \"2740632\",\n" +
                "\t\t\t\"codbarras\": \"040293132125\",\n" +
                "\t\t\t\"signo\": \"-\"\n" +
                "\t\t}\n" +
                "\t],\n" +
                "\t\"ejecucionexterna\": {\n" +
                "\t\t\"correcta\": 1\n" +
                "\t}\n" +
                "}")
        //if (resultCode == Activity.RESULT_OK) {
            println("ley: "+ resultIntent?.getStringExtra("PLUGIN_RESPONSE"))
            setResult(Activity.RESULT_OK, resultIntent)
            finish()

        */
        //}
        finish()
        this@SaleActivity.finishAffinity()
    }

    private fun showError(it: String?) {
        showProgress(false)
        if (it != null)
            printOnSnackBar(it)
    }

    private fun checkResult(productEntity: ProductEntity?,dataPLugin: Linea) {
        dataItemLinea =dataPLugin
        if (productEntity != null) {
            if (productEntity.stimei) {
                //request IMEI
                if (TextUtils.isEmpty(productEntity.imei)) {

                    val dialogView = LayoutInflater.from(this).inflate(R.layout.search_imei_dialog, lltRoot, false)
                    dialogView?.textTitle?.setText("${productEntity.codigo}: ${productEntity.descripcion}")
                    val mydialog = AlertDialog.Builder(this)
                            .setView(dialogView)
                            .setCancelable(false)
                            //.setTitle("\n${productEntity.codigo}: ${productEntity.descripcion}")
                            .show()

                    dialogView?.btnScan?.setOnClickListener {
                        val integrator = IntentIntegrator(this)
                        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                        integrator.setPrompt("ESCANEAR IMEI: \n${productEntity.descripcion}")
                        integrator.setOrientationLocked(false)
                        integrator.setBeepEnabled(true)
                        integrator.setBarcodeImageEnabled(true)
                        integrator.setRequestCode(113)
                        integrator.initiateScan()
                    }
                    dialogView?.tvwAccept?.setOnClickListener {

                        println("ley: IMEI: "+ mydialog?.isShowing)
                        mydialog?.dismiss()
                        fltLoading.visibility = View.VISIBLE
                        if (!dialogView?.edtImei?.text.toString().isEmpty()) {
                            productEntity.imei = dialogView.edtImei?.text.toString()

                        }else {
                            productEntity.imei = view?.edtImei?.text.toString()
                        }
                        checkResult(productEntity,dataPLugin)
                        //if(!TextUtils.isEmpty(productEntity.imei)){
                           // mydialog?.dismiss()

                        //}

                    }
                    dialogView?.tvwCancelar?.setOnClickListener {
                        onBackPressed()
                    }
                } else {
                   // dialog?.dismiss()
                   // addItem(productEntity, true)
                    saleViewModel.checkAutomatically(dataPLugin,productEntity, ::resultSearch, ::onErrorImei)
                   // searchViewModel.checkAutomatically(productEntity)
                }
            } else if (productEntity.stimei2) {
                if (TextUtils.isEmpty(productEntity.imei2)) {
                    //request imei2
                    val dialogView = LayoutInflater.from(this).inflate(R.layout.search_imei_dialog, lltRoot, false)
                    val mydialog2 = AlertDialog.Builder(this)
                            .setView(dialogView)
                            .setCancelable(false)
                            .setTitle(R.string.propt_imei_title2)
                            .show()

                    dialogView?.btnScan?.setOnClickListener {
                        val integrator = IntentIntegrator(this)
                        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                        integrator.setPrompt("Escanear Imei 2")
                        integrator.setOrientationLocked(false)
                        integrator.setBeepEnabled(true)
                        integrator.setBarcodeImageEnabled(true)
                        integrator.setRequestCode(115)
                        integrator.initiateScan()
                    }
                    dialogView?.tvwAccept?.setOnClickListener {
                        fltLoading.visibility = View.VISIBLE
                        if (!dialogView?.edtImei?.text.toString().isEmpty()) {
                            productEntity.imei2 = dialogView?.edtImei?.text.toString()
                            mydialog2?.dismiss()
                        }else {
                            productEntity.imei = view?.edtImei?.text.toString()
                        }
                        checkResult(productEntity,dataPLugin)
                        //productEntity.imei2 = dialogView?.edtImei?.text.toString()
                        //mydialog2?.dismiss()
                        //checkResult(productEntity)
                        //
                        dialogView?.tvwCancelar?.setOnClickListener {
                            onBackPressed()
                        }
                    }
                } else {
                    addItem(productEntity, true)
                }
            } else {
                // get product from search edittext
                complementProductTempCode = productEntity.codigo
                addItem(productEntity,true)
                //mydialog?.dismiss()
               // if (dialog?.isShowing == true)
                 //   dialog?.dismiss()
            }
        } else {
            showProgress(false)
            val intent = Intent(this, SearchProductActivity::class.java)
            startActivityForResult(intent, SEARCH_REQUEST)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("result_bar: "+data?.dataString)
        when (requestCode) {
            SEARCH_REQUEST -> {
                if (Activity.RESULT_OK == resultCode) {
                    val productEntity: ProductEntity? = data?.getParcelableExtra(SearchProductActivity.PRODUCT_KEY)
                    if(productEntity != null) addItem(productEntity,false)
                }
            }

            GARANTIE_REQUEST -> {
                if (Activity.RESULT_OK == resultCode) {
                    val productEntityExt: ProductEntity? = data?.getParcelableExtra(SearchProductActivity.PRODUCT_KEY_EXT)
                    val productEntityDamage: ProductEntity? = data?.getParcelableExtra(SearchProductActivity.PRODUCT_KEY_DAMAGE)

                    if(productEntityExt != null) addItem(productEntityExt,false)
                    if(productEntityDamage != null)addItem(productEntityDamage,false)
                }
            }

            COMPLEMENTARY_REQUEST -> {
                if( Activity.RESULT_OK == resultCode) {
                    var sizeList = data!!.getIntExtra(SearchProductActivity.SIZE_PRODUCTS, 0)

                    for (index in 0 .. (sizeList - 1)) {
                        val productEntity: ProductEntity? = data?.getParcelableExtra(SearchProductActivity.PRODUCT_KEY+index)
                        if(productEntity != null) addItem(productEntity,false)
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
                            checkResult(searchViewModel.searchResults.value,dataItemLinea)
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
                            checkResult(searchViewModel.searchResults.value,dataItemLinea)
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
                            if (flag_pop){
                                sendBarCodManual(result.contents ?: "")
                            }else {
                                searchViewModel.searchProductDirectly(result.contents ?: "")
                            }
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
            999 -> {
                val resultado = data?.getStringExtra("PLUGIN_RESPONSE")
                val optionBack = data?.getStringExtra("BACK")
                println("ley: PLUGIN_RESPONSE_BACK: "+ resultado)
                val resultIntent = Intent()
                resultIntent.putExtra("PLUGIN_RESPONSE", "{\n" +
                        "\t\"ejecucionexterna\": {\n" +
                        "\t\t\"correcta\": 0,\n" +
                        "\t\t\"ifejecucionnookinitventa\": 1\n" +
                        "\t}\n" +
                        "}")
                if (resultCode == Activity.RESULT_OK && optionBack == null) {
                    println("ley: "+ resultIntent?.getStringExtra("PLUGIN_RESPONSE"))
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun addItem(productEntity: ProductEntity, isGarantie: Boolean) {
        var lastSecuencialOrDefault =  0
        if (saleViewModel.saleLiveData.value != null) {
            if (saleViewModel.saleLiveData.value!!.productos.size > 0)
                lastSecuencialOrDefault = saleViewModel.saleLiveData.value!!.productos[saleViewModel.saleLiveData.value!!.productos.size - 1].secuencial
        }

            var saleSubItem = SaleSubItemEntity().apply {
                secuencial = lastSecuencialOrDefault //+ 1
                codigoventa = productEntity.codigoVenta
                codigoProducto = productEntity.codigo
                descripcion = productEntity.descripcion
                pcdcto = if (!isGarantie) 0.0 else dataItemLinea.dto
                cantidad = dataItemLinea.unidades
                precio = if (!isGarantie) productEntity.precio  else  dataItemLinea.precioiva
                imei = productEntity.imei
                imei2 = productEntity.imei2
                monedaSimbolo = productEntity.monedaSimbolo
                complementaryRowColor = productEntity.complementaryRowColor
                secgaraexte = productEntity.secgaraexte
                codgaraexte = productEntity.codgaraexte
                totaldetalle = if (!isGarantie) productEntity.precio else dataItemLinea.precioneto
            }

        listSaleSubItem.add(saleSubItem)
        println("ley: listSaleSubItem: "+Gson().toJson(listSaleSubItem))
        if (listSaleSubItem.size == sizeProductoSearch) {
            showProgress(true)
            saleViewModel.saveDetail(listSaleSubItem)
        }
        if (!isGarantie) {
            saleViewModel.saveDetail(saleSubItem)
        }



       // saleViewModel.saveDetail(saleSubItem)
    }
    private fun addItem(productEntity: ProductEntity, isGarantie: Boolean, dataPLugin : Linea) {
        var lastSecuencialOrDefault =  0
        if (saleViewModel.saleLiveData.value != null) {
            if (saleViewModel.saleLiveData.value!!.productos.size > 0)
                lastSecuencialOrDefault = saleViewModel.saleLiveData.value!!.productos[saleViewModel.saleLiveData.value!!.productos.size - 1].secuencial
        }

        var saleSubItem = SaleSubItemEntity().apply {
            secuencial = lastSecuencialOrDefault //+ 1
            codigoventa = productEntity.codigoVenta
            codigoProducto = productEntity.codigo
            descripcion = productEntity.descripcion
            pcdcto = dataPLugin.dto
            totaldetalle = dataPLugin.precioneto
            cantidad = dataPLugin.unidades
            precio = if (!isGarantie) dataPLugin.precioiva else productEntity.precio
            imei = productEntity.imei
            imei2 = productEntity.imei2
            monedaSimbolo = productEntity.monedaSimbolo
            complementaryRowColor = productEntity.complementaryRowColor
            secgaraexte = productEntity.secgaraexte
            codgaraexte = productEntity.codgaraexte
        }

        listSaleSubItem.add(saleSubItem)
        println("ley: listSaleSubItem: "+Gson().toJson(listSaleSubItem))
        if (listSaleSubItem.size == sizeProductoSearch) {
            showProgress(true)
            saleViewModel.saveDetail(listSaleSubItem)
        }
        if (!isGarantie) {
            saleViewModel.saveDetail(saleSubItem)
        }



        // saleViewModel.saveDetail(saleSubItem)
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

    private fun subscribeToModel(viewModel: SaleViewModel) {
        viewModel.saleLiveData.observe(this, Observer { newItem ->
            showProgress(false)
            if (newItem != null) {
               // (rvwProducts.adapter as SaleAdapter).items.removeAll { true }
                (rvwProducts.adapter as SaleAdapter).clearItems()
                (rvwProducts.adapter as SaleAdapter).addItems(newItem.productos)
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
        tvwClient.text = "${entity.clienteCodigo} ${entity.clienteNombres}"
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
        saleViewModel.saveSale(::goToResumenPedido, ::onError)
    }
    fun validaProssesSale() {
        btnProcess.isEnabled = false
        val userInfo = getSession()
        val listProducts = saleAdapter.items
        if (saleViewModel.saleLiveData.value!!.productos.size == 0) {
            AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
                .setTitle(R.string.app_name)
                .setMessage(getString(R.string.sale_validation_no_products))
                .setPositiveButton(R.string.aceptar) { d, _ -> d.dismiss() }
                .setCancelable(false)
                .create().show()
            btnProcess.isEnabled = true
            return
        }
        /*
        saleAdapter.items.forEach {
            data ->
            val dataRequest  = VentaProductoRequest(
                coEmpr = userInfo.empresa,
                nuSecu = data.secuencial,
                coItem = data.codigoProducto,
                coVent = data.codigoventa,
                deItem = data.descripcion,
                caDocu = data.cantidad,
                prVent =  data.precio

            )
        }*/
         listProdu = ArrayList<VentaProductoRequest>()
        saleAdapter.items.forEach { data ->
            listProdu.add(
                VentaProductoRequest(
                    coEmpr = userInfo.empresa,
                    nuSecu = data.secuencial,
                    coItem = data.codigoProducto,
                    coVent = data.codigoventa,
                    deItem = data.descripcion,
                    caDocu = data.cantidad,
                    prVent = data.precio

                )
            )
        }
        /*listProdu.add(VentaProductoRequest(
            coEmpr = userInfo.empresa,
            nuSecu = 1,
            coItem = "CE104MOT81",
            coVent = "ABC",
            deItem = "ABC",
            caDocu = 1,
            prVent =  9.9
        ))*/
        saleViewModel.ventaProducto(listProdu,::showQuestionsConfirm, ::onError)
        //showQuestionsConfirm()
    }

    fun showQuestionsConfirm(response: VentaProductoResponse){

        showProgress(false)
        if(response.muestramensaje){
            val dialgCustom = QuestionPopUpFragment()
            dialgCustom.show(supportFragmentManager, "P")
            val args = Bundle()
            args.putString("OptionQuestion","Question")
            args.putSerializable("DataList", response as Serializable)
            dialgCustom.arguments = args
            val fragment = supportFragmentManager.findFragmentByTag("P")
            if (fragment != null) {
                supportFragmentManager.beginTransaction().remove(fragment).commit()
            }
        }else {
            processSale()
        }
    }
    fun showSendCodConfirm(optionData: VentaProductoResponse){
        ventaProductoResponse = optionData
        showProgress(false)
        dialgCustomSenCod = SendCodPopUpFragment()
        dialgCustomSenCod.isCancelable = false
        dialgCustomSenCod.show(supportFragmentManager, "P")
        val args = Bundle()
        args.putString("OptionQuestion","SendCod")
        args.putSerializable("DataList", ventaProductoResponse as Serializable)
        dialgCustomSenCod.arguments = args
        val fragment = supportFragmentManager.findFragmentByTag("P")
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
    }

    private fun goToResumenPedido(entity: SaleEntity) {
        val androidID: String =
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        //prevent nulls after back pressed
        saleViewModel.saleLiveData.postValue(setSessionInfo(saleViewModel.saleLiveData.value!!))
        val currentSaleEntity : SaleEntity?  = saleViewModel.saleLiveData.value
        if (currentSaleEntity != null) {
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
            currentSaleEntity.tipodocumentogenera = entity.tipodocumentogenera
           // currentSaleEntity.androidimei = "a9731e8ca60a4207"
        }
        //end nulls prevent

        startActivityForResult(Intent(this, EndingActivity::class.java).apply {
            putExtra(EndingActivity.EXTRA_ENTITY, currentSaleEntity)
        },999)
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
    private fun productSearchCombined(productCodeIn: Linea) {
        val productCode = productCodeIn

        if (!productCode.referencia.isNullOrEmpty()) {
            showProgress(true)
            println("ley: productCode: "+ Gson().toJson(productCodeIn))
            saleViewModel.searchProductDirectly(productCode,::resultSearch,::onError)
        }
    }

    private fun resultSearch(entity: ProductEntity,dataPLugin : Linea){
        val newSaleEntity = saleViewModel.saleLiveData.value!!
        setSessionInfo(newSaleEntity)
        println("ley: entity: "+ Gson().toJson(entity))
        println("ley: dataPLugin: "+ Gson().toJson(dataPLugin))
        newSaleEntity.vendedorCodigo = pluginDataResponseData?.codvendedor.toString()
        if (pluginDataResponseData?.cliente.toString()!= "0") {
            newSaleEntity.clienteCodigo = "*"+ pluginDataResponseData?.cliente.toString()
        }
        newSaleEntity.tienda = pluginDataResponseData?.lineas?.get(0)?.almacen.toString()
        toolbar.title = "${getString(R.string.title_sale_tienda)} ${newSaleEntity.tienda}"
        saleViewModel.saleLiveData.postValue(newSaleEntity)

        dataItemLinea = dataPLugin
        checkResult(entity,dataPLugin)
        //addItem(entity,true,dataPLugin)
    }
    private fun resultSearch(entity: ProductEntity){
        println("ley: resultSearch: entity: "+ Gson().toJson(entity))
        //println("ley: dataPLugin: "+ Gson().toJson(dataPLugin))
        val newSaleEntity = saleViewModel.saleLiveData.value!!
        setSessionInfo(newSaleEntity)
        newSaleEntity.vendedorCodigo = pluginDataResponseData?.codvendedor.toString()
        newSaleEntity.tienda = pluginDataResponseData?.lineas?.get(0)?.almacen.toString()
        toolbar.title = "${getString(R.string.title_sale_tienda)} ${newSaleEntity.tienda}"
        saleViewModel.saleLiveData.postValue(newSaleEntity)
        //dataItemLinea = dataPLugin
       // checkResult(entity,)
        //addItem(entity,true,dataPLugin)
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

    private fun showClientPopUp() {
        val ft = supportFragmentManager.beginTransaction()

        val prev = supportFragmentManager.findFragmentByTag("dialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)

        val codigoCliente = saleViewModel.saleLiveData.value!!.clienteCodigo
        val data = saleViewModel.saleLiveData.value
        val listTipoDocumento = saleViewModel.listTipoDocumento
        val mapTipoDocumento = LinkedHashMap<Int, String>()
        for (list in listTipoDocumento.value!!){
            mapTipoDocumento[list.codigo]= list.description
        }
        val popUpFragment = ClientPopUpFragment.createFragment(codigoCliente,
                data!!.clienteTipoDocumento,
                getSettings().urlbase,
                getSession(),
                onSelectClient = { client ->
                    run {
                        data!!.clienteTipoDocumento = client.identityDocumentType
                        data.clienteCodigo = client.documentNumber
                        data.clienteNombres = client.fullName
                        data.telefono = client.phone
                        data.email = client.email
                        saleViewModel.saleLiveData.postValue(data)
                    }
                },
            mapTipoDocumento)

        popUpFragment.show(ft, "ClientPopup")
    }





    private fun onError(message: String) {
        btnProcess.isEnabled = true
        Log.e(TAG, message)

        AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
                .setTitle(R.string.app_name)
                .setMessage(message)
                .setPositiveButton(R.string.aceptar) { d, _ -> d.dismiss() }
                .setCancelable(false)
                .create().show()
    }
    private fun onErrorImei(message: String, productEntity: ProductEntity) {
        Log.e(TAG, message)
        dialog?.dismiss()
        AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
            .setTitle(R.string.app_name)
            .setMessage(message)
            .setPositiveButton(R.string.aceptar) { d, _ ->
                d.dismiss()
                checkResult(productEntity,dataItemLinea)
                }
            .setCancelable(false)
            .create().show()
    }

    private fun showProgress(show: Boolean) {
        //fltLoading.visibility =
            if (show) showProgressBar() else hideProgressBar()
    }
    private fun toggleButtons(root: ViewGroup, isEnabled: Boolean) {
        for (i in 0 until root.childCount) {
            val child = root.getChildAt(i)
            when (child) {
                is Button -> child.isEnabled = isEnabled
                is RecyclerView -> child.isEnabled = isEnabled
                is ViewGroup -> toggleButtons(child, isEnabled) // Recursión para layouts anidados
            }
        }
    }
    private fun showProgressBar() {
        val fltLoading = findViewById<View>(R.id.fltLoading)
        fltLoading.visibility = View.VISIBLE

        val rootLayout = findViewById<ViewGroup>(R.id.lltRoot)
        toggleButtons(rootLayout, false) // Bloquea botones
    }
    private fun hideProgressBar() {
        val fltLoading = findViewById<View>(R.id.fltLoading)
        fltLoading.visibility = View.GONE

        val rootLayout = findViewById<ViewGroup>(R.id.lltRoot)
        toggleButtons(rootLayout, true) // Habilita botones
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
        println("urlCorreo: ${userInfo.urlcorreo}")
        println("urlCorreoRespuesta: ${userInfo.urlcorreorespuesta}")
        saleEntity.vendedorCodigo = userInfo.vendedorcodigo
        saleEntity.usuario = userInfo.usuario
        saleEntity.cajaCodigo = userInfo.cajacodigo
        saleEntity.tienda = userInfo.tienda
        saleEntity.androidimei = userInfo.imei
       // saleEntity.email = userInfo.email
        return saleEntity
    }

    companion object {
        private const val SEARCH_REQUEST = 1234
        const val COMPLEMENTARY_REQUEST = 543
        const val GARANTIE_REQUEST = 544
        val TAG = SaleActivity::class.java.simpleName!!
    }

    override fun closeDialogQuestion(data: String, optionData: VentaProductoResponse) {
        when(data){
            "SI" -> {
                showSendCodConfirm(optionData)
            }
            "NO" -> {
                println("se cancelo el dialog questions")
            }
        }

    }
    fun sendMail() {

        var request = EnvioCorreoRequest(
            coTien = getSession().tienda,
            deCodi = ventaProductoResponse.codigoautorizacion,
            feCodi = Formatter.DateToString(Date()),
            deMail = ventaProductoResponse.correo,
            tiDocuIden = Defaults.Cliente.identityDocumentType.toString(),
            nuDocuIden = Defaults.Cliente.documentNumber,
            noClie =  Defaults.Cliente.fullName,
            coVend = getSession().vendedorcodigo,
            noVend = getSession().vendedornombre,
            detalle = listProdu
        )
        println("request: ${Gson().toJson(request)}")

        saleViewModel.ventaProductoEnvioCorreo(getSession().urlcorreo,request,::responseSendMail,::error)
    }
    fun responseSendMail(dataResponse: EnvioCorreoResponse,flag: Boolean){
        envioCorreoResponse = dataResponse
        if (dataResponse.result){
            if (flag){
                println("esperando respuesta")
                saleViewModel.envioCodigoRespuesta(
                    getSession().urlcorreo,
                    EnvioCodigoRequest(
                        coEmpr = "01",
                        deCodi = ventaProductoResponse.codigoautorizacion
                    ), ::goToResumenValid ,::error)
                Thread.sleep(3000L)
            }else{
                //error("El usuario Aprobo el pedido!")
                Toast.makeText(this,"No esta aprobado!!",Toast.LENGTH_LONG).show()
                println("Termino")
            }

        }else {
           // error("No esta aprobado!!")
            println("No esta aprobado!!")
            Toast.makeText(this,"No esta aprobado!!",Toast.LENGTH_LONG).show()
        }
    }
    fun goToResumenValid(data: EnvioCodigoResponse){
        when(data.deResu){
            "" -> {
                responseSendMail(envioCorreoResponse,true)
            }
            "ACT" -> {
                responseSendMail(envioCorreoResponse,true)
            }
            "APR" -> {
                dialgCustomSenCod.dismiss()
                processSale()

               // responseSendMail(envioCorreoResponse,false)
            }
            "DSP" -> {
                dialgCustomSenCod.dismiss()
                //error("")
                Toast.makeText(this,"usuario DESAPROBO la autorización",Toast.LENGTH_LONG).show()
            }
            else -> {
                dialgCustomSenCod.dismiss()
                println("La respuesta no se reconoce.: ${data.deResu}")
            }
        }
        if(data.deResu.length == 0){
            responseSendMail(envioCorreoResponse,true)
        }

    }
    override fun closeDialogSendCod(data: String, optionData: String) {
        when (data) {
            "SEND_MAIL" -> {
                sendMail()
            }
            "VALID_COD" -> {
                sendCodManual(optionData)
            }
            "VALID_BAR_COD" -> {
                sendBarCodManual(optionData)
            }
            "CAM_COD" ->{
                flag_pop = true
                productSearch()
            }
        }

    }
    fun sendCodManual(codigo: String){
        var request = VentaProductoValidaCodigoRequest(
            coTien = getSession().tienda,
            deCodi = ventaProductoResponse.codigoautorizacion,
            coEmpr = "01",
            deCodiIngr = codigo,
            opcion = "VALIDA_AUTORIZACION"
        )
        println("request_sendCodManual: ${Gson().toJson(request)}")
        saleViewModel.ventaProductoValidaCodigo(request,::responseSendCod,::error)
    }
    fun sendBarCodManual(codigo: String){
        var request = VentaProductoValidaCodigoRequest(
            coTien = getSession().tienda,
            deCodi = ventaProductoResponse.codigoautorizacion,
            coEmpr = "01",
            deCodiIngr = codigo,
            opcion = "VALIDA_FOTOCHECK"
        )
        println("request_sendCodManual: ${Gson().toJson(request)}")
        saleViewModel.ventaProductoValidaCodigo(request,::responseSendCod,::error)
    }
    fun responseSendCod(data: VentaProductoValidaCodigoResponse){
        if (data.result){
            //error("¡El pedido esta aprobado!")
            dialgCustomSenCod.dismiss()
            Toast.makeText(this,"el pedido esta aprobado",Toast.LENGTH_LONG).show()

            //error("el pedido esta aprobado")
            processSale()
        }else {
            Toast.makeText(this,data.mensaje,Toast.LENGTH_LONG).show()
            dialgCustomSenCod.dismiss()
            //error(data.mensaje)

        }
    }

    override fun onResume() {
        super.onResume()
        println("onResume")
        btnProcess.isEnabled = true
    }
}



