package com.pedidos.android.persistence.ui.guide

import android.app.DatePickerDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import com.google.gson.Gson
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.model.guide.*
import com.pedidos.android.persistence.ui.ending.EndingActivity
import com.pedidos.android.persistence.ui.guide.fragment.*
import com.pedidos.android.persistence.ui.menu.MenuActivity
import com.pedidos.android.persistence.utils.Formatter
import com.pedidos.android.persistence.viewmodel.GuideViewModel
import com.pedidos.android.persistence.viewmodel.SearchProductViewModel
import kotlinx.android.synthetic.main.activity_pago_link.*
import kotlinx.android.synthetic.main.guide_head_activity.*
import kotlinx.android.synthetic.main.guide_head_activity.fltLoading
import kotlinx.android.synthetic.main.sales_activity.toolbar
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

class GuideHeadActivity : MenuActivity(), StoragePopUpFragment.newDialoglistenerStorage,
    TypeDocPopUpFragment.newDialoglistenerTypeDocGuide,
    OperationGuidePopUpFragment.newDialoglistenerTypeDocGuide,
    AuxPopUpFragment.newDialoglistenerAuxGuide,
    CityPopUpFragment.newDialoglistenerCity,
    TypeDocumentIdPopUpFragment.newDialoglistenerTypeDocumentId,
    PlacaPopUpFragment.newDialoglistenerPlacaGuide,
    DriveDocumentPopUpFragment.newDialoglistenerDriveGuide,
    TransportPopUpFragment.newDialoglistenerTransportGuide{
    private lateinit var saleViewModel: GuideViewModel
    private lateinit var searchViewModel: SearchProductViewModel
    private lateinit var guideAdapter: GuideAdapter
    lateinit var context: Context
    private var dialog: AlertDialog? = null
    private var view: View? = null
    var dayMin = 0
    var yearMin = 0
    var monthMin = 0
    var curDate: Calendar? = null
    var guideRequest = GuideRequest()
    var optionTypeDoc = ""
    var optionNumberDoc = ""
    var optionCity = ""
    var optionCityAll = ""
    //var  listGuideDetail: List<GuideDetail> = ArrayList()
    var parametros: Bundle? = null
    var dateGuide = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentViewWithMenu(R.layout.guide_head_activity)
        setSupportActionBarMenu(toolbar)
        showProgress(false)
       // parametros = intent.extras
        context = this
        checkSession()
        setFechaOpe()

        println("empresa: ${getSession().empresa}")
        toolbar.title = "${getString(R.string.title_guide_tienda)} ${getSession().tienda}"
        /*toolbar.setNavigationIcon(R.drawable.ic_back_white_25)
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }*/
        val saleFactory = GuideViewModel.Companion.Factory(application, getSettings().urlbase)
        val searchFactory =
            SearchProductViewModel.Companion.Factory(application, getSettings().urlbase)
        saleViewModel = ViewModelProviders.of(this, saleFactory).get(GuideViewModel::class.java)
        searchViewModel =
            ViewModelProviders.of(this, searchFactory).get(SearchProductViewModel::class.java)
        parametros = intent.extras
       // listGuideDetail = parametros!!.get("detailGuide") as List<GuideDetail>
        println("sesion: ${Gson().toJson(getSession())}")
        //println("products: ${Gson().toJson(listGuideDetail)}")
        tielDateGuide.setOnClickListener { showDatePickerDialog() } //imbSearchDate
        imbSearchDate.setOnClickListener { showDatePickerDialog() }
        //cardwHead.setOnClickListener { showHeadGuide() }
        //cardwCarGuia.setOnClickListener { showCarGuide() }
        //cardwDriverGuia.setOnClickListener { showDriveGuide() }
        //cardwTransportGuia.setOnClickListener { showTransportGuide() }
        imbSearchStorage.setOnClickListener { findStorage() }
        imbSearchTypeDocumentGuia.setOnClickListener { findTypeGuide() }
        imbSearchOperation.setOnClickListener { findOperationGuide() }
        imbSearchPlaca.setOnClickListener { findPlacaGuia() }
        imbSearchPlacaAll.setOnClickListener { retrieveAllPlaca() }
        imbSearchDocumentDrive.setOnClickListener { searchTypeDocumentId("drive")  }
        imbSearchNumberDocumentDrive.setOnClickListener { searchNumberDocumentId("drive")  }
        imbSearchStorageAll.setOnClickListener { retrieveAllStorage() }
        imbOperationGuiaAll.setOnClickListener { retrieveAllOperationGuide() }
        imbTypeDocumentGuiaAll.setOnClickListener { retrieveAllTypeDocumentGuide() }
        imbSearchTypeAux.setOnClickListener {  findTypeAux()}
        imbSearchAux.setOnClickListener { findAux()}
        imbSearchAllAux.setOnClickListener { searchAllAux() }
        imbSearchCity.setOnClickListener { searchCity("start") }
        imbSearchAllCity.setOnClickListener { retrieveAllCity("start") }
        imbSearchTypeDocumentId.setOnClickListener { searchTypeDocumentId("cab") }
        imbSearchNumberDocumentId.setOnClickListener { searchNumberDocumentId("cab") }
        imbSearchCityFinish.setOnClickListener { searchCity("finish") }
        imbSearchAllCityFinish.setOnClickListener { retrieveAllCity("finish") }
        imbSearchModeTransport.setOnClickListener { findModeTransporteGuide() }
        tielTypeDocumentTransportGuide.setOnClickListener { searchTypeDocumentId("transport") }
        imbSearchNumberDocTransport.setOnClickListener {
            if (tielNumberDocumentTransportGuide.text.toString().isNotEmpty()) {
                searchNumberDocumentId("transport")
            }
        }
        imbSearchNumberDocTransportAll.setOnClickListener { retriveAllNumberDocumentId("transport") }
        imbSearchNumberDocumentDriveAll.setOnClickListener { retriveAllNumberDocumentId("drive") }
        imbSearchCityTransport.setOnClickListener { searchCity("transport") }
        imbSearchAllCityTransport.setOnClickListener { retrieveAllCity("transport") }
        //imbSearchCity.setOnClickListener { searchCity("drive") }
        imbSearchCityDrive.setOnClickListener { searchCity("drive") }
        imbSearchAllCityDrive.setOnClickListener { retrieveAllCity("drive") }

        btnFinishGuide.setOnClickListener { Confirmacion() }
        findStorageInit()
        findTypeGuideInit()
    }

    private fun findDocumentDrive() {
       // saleViewModel.
    }
    private fun retrieveAllPlaca() {
        showProgress(true)
        var placaCarGuideRequest= PlacaCarGuideRequest()
        placaCarGuideRequest.empresa= getSession().empresa
        placaCarGuideRequest.usuario= getSession().usuario
        saleViewModel.getPlacaCarGuide(placaCarGuideRequest,::findPlacaAll,::onError)
    }
    fun findPlacaAll(dataList: List<DataResponse>) {
        showProgress(false)
        val dialgCustom = PlacaPopUpFragment()
        dialgCustom.show(supportFragmentManager, "P")
        val args = Bundle()
        args.putSerializable("DataList", dataList as Serializable)
        dialgCustom.arguments = args
        val fragment = supportFragmentManager.findFragmentByTag("P")
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
    }
    private fun findPlacaGuia() {
        showProgress(true)
        var placaCarGuideRequest= PlacaCarGuideRequest()
        placaCarGuideRequest.empresa= getSession().empresa
        placaCarGuideRequest.usuario= getSession().usuario
        placaCarGuideRequest.vehiculo = tielIdCarGuide.text.toString()
        if (placaCarGuideRequest.vehiculo != null || placaCarGuideRequest.vehiculo != ""){
            saleViewModel.getPlacaCarGuide(placaCarGuideRequest,::setPlacaGuia,::onError)
        }else {
            etiIdCarGuide.error = "El numero de placa esta vacio"
        }

    }
    fun setPlacaGuia(dataList: List<DataResponse>){
        showProgress(false)
        if (dataList.isNotEmpty()) {
            tielIdCarGuide.setText(dataList[0].codigo.toString())
            tielDescriptionCarGuide.setText(dataList[0].descripcion.toString())
            guideRequest.placavehiculo = dataList[0].codigo.toString()
            guideRequest.descripcionvehiculo= dataList[0].descripcion.toString()
        }else {

        }
    }
    private fun findModeTransporteGuide() {
        val alertDialog: AlertDialog.Builder? = context.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle("Modo de Transporte")
                    .setItems(R.array.array_modo_transport) { dialog, which ->

                        when (which) {
                            0 -> {
                                tielModeTransportGuide.setText("PÚBLICO")
                                guideRequest.modalidadtransporte = "PUBLICO"
                            }
                            1 -> {
                                tielModeTransportGuide.setText("PRIVADO")
                                guideRequest.modalidadtransporte = "PRIVADO"
                            }
                        }

                    }
            }
        }
        val alert: AlertDialog? = alertDialog?.create()
        alert?.show()
    }


    fun retriveAllNumberDocumentId(data : String){
        showProgress(true)
        optionNumberDoc = data
        var documentIdRequest = DocumentIdAllRequest()
        documentIdRequest.empresa = getSession().empresa
        documentIdRequest.usuario =  getSession().usuario
        when (optionNumberDoc){
            "cab" -> {
                documentIdRequest.tipdoccond = guideRequest.tipodocidentidad
                saleViewModel.getDocumentIdAllGuide(documentIdRequest,::searchAllNumberDocumentId,::onError)
            }
            "transport"->{
                var documentTransportGuideRequest = DocumentTransportGuideRequest()
                documentTransportGuideRequest.empresa = getSession().empresa
                documentTransportGuideRequest.usuario = getSession().usuario
                documentTransportGuideRequest.tipdoctrans = guideRequest.tipodoctransportista
                saleViewModel.getDataTransport(documentTransportGuideRequest,::searchAllTransportNumberDocumentId,::onError)
            }
            "drive"->  {
                documentIdRequest.tipdoccond = guideRequest.tipodocconductor
                saleViewModel.getDocumentIdAllGuide(documentIdRequest,::searchAllDriveNumberDocumentId,::onError)
            }
        }
    }
    fun searchAllTransportNumberDocumentId(dataList: List<DocumentTransportGuideResponse>) {
        showProgress(false)
        val dialgCustom = TransportPopUpFragment()
        dialgCustom.show(supportFragmentManager, "P")
        val args = Bundle()
        args.putSerializable("DataList", dataList as Serializable)
        dialgCustom.arguments = args
        val fragment = supportFragmentManager.findFragmentByTag("P")
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
    }
    fun searchAllNumberDocumentId(dataList: List<DocumentIdResponse>) {
        showProgress(false)
        val dialgCustom = DrivePopUpFragment()
        dialgCustom.show(supportFragmentManager, "P")
        val args = Bundle()
        args.putSerializable("DataList", dataList as Serializable)
        dialgCustom.arguments = args
        val fragment = supportFragmentManager.findFragmentByTag("P")
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
    }
    fun searchAllDriveNumberDocumentId(dataList: List<DocumentIdResponse>) {
        showProgress(false)
        val dialgCustom = DriveDocumentPopUpFragment()
        dialgCustom.show(supportFragmentManager, "P")
        val args = Bundle()
        args.putSerializable("DataList", dataList as Serializable)
        dialgCustom.arguments = args
        val fragment = supportFragmentManager.findFragmentByTag("P")
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
    }
    fun searchNumberDocumentId(data : String){
        showProgress(true)
        optionNumberDoc = data
        var documentIdRequest = DocumentIdRequest()
        documentIdRequest.empresa = getSession().empresa
        documentIdRequest.usuario =  getSession().usuario
        when (optionNumberDoc){
            "cab" -> {
                documentIdRequest.tipodocuiden = guideRequest.tipodocidentidad
                documentIdRequest.docuiden = tielNumberDocumentIdGuide.text.toString()
                saleViewModel.getDocumentIdGuide(documentIdRequest,::setDocumentId,::onError)
            }
            "transport"->{
                documentIdRequest.tipodocuiden = guideRequest.tipodoctransportista
                documentIdRequest.docuiden = tielNumberDocumentTransportGuide.text.toString()
                saleViewModel.getDocumentIdGuide(documentIdRequest,::setDocumentId,::onError)
            }
            "drive"->  {
                documentIdRequest.tipodocuiden = guideRequest.tipodocconductor
                documentIdRequest.docuiden = tielDriverDocumentGuide.text.toString()
                saleViewModel.getDocumentIdGuide(documentIdRequest,::setDocumentId,::onError)
            }
        }

    }
    fun setDocumentId(dataList: List<DocumentIdResponse>){
        showProgress(false)
        if (dataList.isNotEmpty()) {
            when(optionNumberDoc){
                "cab" -> {
                    tielNumberDocumentIdGuide.setText(dataList[0].descripcion.toString())
                    etiNumberDocumentIdGuide.hint = "Número Documento Identidad - ${dataList[0].codigo.toString()}"
                    etiNumberDocumentIdGuide.error = ""
                    guideRequest.distritollegada = dataList[0].ubigeollegada.toString()
                    guideRequest.direccionllegada = dataList[0].direccionllegada.toString()
                    guideRequest.numerodocidentidad =  dataList[0].codigo.toString()
                    tielFinishCityGuide.setText(dataList[0].ubigeollegada.toString())
                    tielFinishAddressGuide.setText(dataList[0].direccionllegada.toString())
                    searchCity("finish")
                }
                "transport" -> {
                    tielNumberDocumentTransportGuide.setText( dataList[0].codigo.toString())
                    tielNameTransportGuide.setText(dataList[0].descripcion.toString())
                    tielCityTransportGuide.setText( dataList[0].ubigeollegada.toString())
                    tielAdrressTransportGuide.setText( dataList[0].direccionllegada.toString())
                    guideRequest.numerodoctransportista = dataList[0].codigo.toString()
                    guideRequest.nombretransportista = dataList[0].descripcion.toString()
                    guideRequest.distritotransportista = dataList[0].ubigeollegada.toString()
                    if (dataList[0].ubigeollegada.toString().isNotEmpty()){
                        searchCity("transport")
                    }

                  //  etiCityTransportGuide.hint
                }
                "drive" -> {
                    tielDriverDocumentGuide.setText( dataList[0].codigo.toString())
                    tielDriverNameGuide.setText(dataList[0].descripcion.toString())
                    tielDriverAddressGuide.setText(dataList[0].direccionllegada.toString())
                    tielCityDriverGuide.setText( dataList[0].ubigeollegada.toString())
                    if (dataList[0].ubigeollegada.toString().isNotEmpty()){
                        searchCity("drive")
                    }
                }
            }


        } else {
            etiNumberDocumentIdGuide.error = "No existe el Documento."
            tielNumberDocumentIdGuide.setText("")
            etiNumberDocumentIdGuide.hint = "Número Documento Identidad"
            tielFinishCityGuide.setText("")
            tielFinishAddressGuide.setText("")
            guideRequest.distritopartida = null
            guideRequest.distritollegada = null
            guideRequest.direccionllegada = null
            guideRequest.numerodocidentidad = null
        }
    }


    private fun searchTypeDocumentId(dato : String) {
        showProgress(true)
        optionTypeDoc = dato
        var typeDocumentIdRequest = TypeDocumentRequest()
        typeDocumentIdRequest.empresa = getSession().empresa
        typeDocumentIdRequest.usuario = getSession().usuario
        saleViewModel.getTypeDocumentId(typeDocumentIdRequest,::finTypeDocumentoIdGuide,::onError)
    }
    fun finTypeDocumentoIdGuide(dataList: List<DataResponse>) {
        showProgress(false)
        val dialgCustom = TypeDocumentIdPopUpFragment()
        dialgCustom.show(supportFragmentManager, "P")
        val args = Bundle()
        args.putSerializable("DataList", dataList as Serializable)
        dialgCustom.arguments = args
        val fragment = supportFragmentManager.findFragmentByTag("P")
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
    }

    private fun searchCity(dato : String) {
        showProgress(true)
        optionCity = dato
        var ubigeoRequest = UbigeoRequest()
        ubigeoRequest.empresa = getSession().empresa
        ubigeoRequest.usuario = getSession().usuario

        when (dato) {
            "start" -> {
                ubigeoRequest.ubigeo = tielStartCityGuide.text.toString()
                saleViewModel.getUbigeo(ubigeoRequest, ::setCityStart, ::onError)
            }
            "finish" -> {
                ubigeoRequest.ubigeo = tielFinishCityGuide.text.toString()
                saleViewModel.getUbigeo(ubigeoRequest, ::setCityFinish, ::onError)
            }
            "transport" ->{
                ubigeoRequest.ubigeo = tielCityTransportGuide.text.toString()
                saleViewModel.getUbigeo(ubigeoRequest, ::setCity, ::onError)
            }
            "drive"  -> {
                ubigeoRequest.ubigeo = tielCityDriverGuide.text.toString()
                saleViewModel.getUbigeo(ubigeoRequest, ::setCity, ::onError)
            }
        }
    }


    fun setCityStart(dataList: List<DataResponse>){
        showProgress(false)
        if (dataList.isNotEmpty()) {
            tielStartCityGuide.setText(dataList[0].descripcion.toString())
            etiStartCityGuide.hint = "Distrito de Partida - ${dataList[0].codigo.toString()}"
            etiStartCityGuide.error = ""
            guideRequest.distritopartida = dataList[0].codigo.toString()

        } else {
            etiStartCityGuide.error = "No existe el distrito."
            tielStartCityGuide.setText("")
            etiStartCityGuide.hint = "Distrito de Partida"
            guideRequest.distritopartida = null
        }
    }
    fun setCity(dataList: List<DataResponse>){
        showProgress(false)
        when (optionCity) {
            "start" -> {
                if (dataList.isNotEmpty()) {
                    tielStartCityGuide.setText(dataList[0].descripcion.toString())
                    etiStartCityGuide.hint = "Distrito de Partida - ${dataList[0].codigo.toString()}"
                    etiStartCityGuide.error = ""
                    guideRequest.distritopartida = dataList[0].codigo.toString()

                } else {
                    etiStartCityGuide.error = "No existe el distrito."
                    tielStartCityGuide.setText("")
                    etiStartCityGuide.hint = "Distrito de Partida"
                    guideRequest.distritopartida = null
                }
            }
            "finish" ->{
                if (dataList.isNotEmpty()) {
                    tielFinishCityGuide.setText(dataList[0].descripcion.toString())
                    etiFinishCityGuide.hint = "Distrito de Llegada - ${dataList[0].codigo.toString()}"
                    etiFinishCityGuide.error = ""
                    guideRequest.distritollegada = dataList[0].codigo.toString()

                } else {
                    etiFinishCityGuide.error = "No existe el distrito."
                    tielFinishCityGuide.setText("")
                    etiFinishCityGuide.hint = "Distrito de Llegada"
                    guideRequest.distritollegada = null
                }
            }
            "transport"->{
                if (dataList.isNotEmpty()) {
                    tielCityTransportGuide.setText(dataList[0].descripcion.toString())
                    etiCityTransportGuide.hint = "Distrito de Transportista - ${dataList[0].codigo.toString()}"
                    etiCityTransportGuide.error = ""
                    guideRequest.distritotransportista = dataList[0].codigo.toString()

                } else {
                    etiCityTransportGuide.error = "No existe el distrito."
                    tielCityTransportGuide.setText("")
                    etiCityTransportGuide.hint = "Distrito de Transportista"
                    guideRequest.distritotransportista = null
                }
            }
            "drive" ->{
                if (dataList.isNotEmpty()) {
                    tielCityDriverGuide.setText(dataList[0].descripcion.toString())
                    etiCityDriverGuide.hint = "Distrito de Conductor - ${dataList[0].codigo.toString()}"
                    etiCityDriverGuide.error = ""
                    guideRequest.distritoconductor = dataList[0].codigo.toString()

                } else {
                    etiCityDriverGuide.error = "No existe el distrito."
                    tielCityDriverGuide.setText("")
                    etiCityDriverGuide.hint = "Distrito de Conductor"
                    guideRequest.distritoconductor = null
                }
            }
        }

    }
    fun setCityFinish(dataList: List<DataResponse>){
        showProgress(false)
        if (dataList.isNotEmpty()) {
            tielFinishCityGuide.setText(dataList[0].descripcion.toString())
            etiFinishCityGuide.hint = "Distrito de Llegada - ${dataList[0].codigo.toString()}"
            etiFinishCityGuide.error = ""
            guideRequest.distritollegada = dataList[0].codigo.toString()

        } else {
            etiFinishCityGuide.error = "No existe el distrito."
            tielFinishCityGuide.setText("")
            etiFinishCityGuide.hint = "Distrito de Llegada"
            guideRequest.distritollegada = null
        }
    }
    fun findCityFinishAll(dataList: List<DataResponse>){
        showProgress(false)
        val dialgCustom = CityPopUpFragment()
        dialgCustom.show(supportFragmentManager, "P")
        val args = Bundle()
        args.putString("OptionCity","finish")
        args.putSerializable("DataList", dataList as Serializable)
        dialgCustom.arguments = args
        val fragment = supportFragmentManager.findFragmentByTag("P")
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
    }
    fun findCityStartAll(dataList: List<DataResponse>){
        showProgress(false)
        val dialgCustom = CityPopUpFragment()
        dialgCustom.show(supportFragmentManager, "P")
        val args = Bundle()
        args.putString("OptionCity","start")
        args.putSerializable("DataList", dataList as Serializable)
        dialgCustom.arguments = args
        val fragment = supportFragmentManager.findFragmentByTag("P")
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
    }
    fun findCityAll(dataList: List<DataResponse>){
        showProgress(false)
        val dialgCustom = CityPopUpFragment()
        dialgCustom.show(supportFragmentManager, "P")
        val args = Bundle()
        args.putString("OptionCity",optionCityAll)
        args.putSerializable("DataList", dataList as Serializable)
        dialgCustom.arguments = args
        val fragment = supportFragmentManager.findFragmentByTag("P")
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
    }
    private fun retrieveAllCity(dato : String) {
        optionCityAll = dato
        showProgress(true)
        var ubigeoRequest = UbigeoRequest()
        ubigeoRequest.empresa = getSession().empresa
        ubigeoRequest.usuario = getSession().usuario
        when (dato) {
            "start" -> {
                saleViewModel.getUbigeo(ubigeoRequest, ::findCityStartAll, ::onError)
            }
            "finish" -> {
                saleViewModel.getUbigeo(ubigeoRequest, ::findCityFinishAll, ::onError)
            }
            "transport" -> {
                saleViewModel.getUbigeo(ubigeoRequest, ::findCityAll, ::onError)
            }
            "drive" -> {
                saleViewModel.getUbigeo(ubigeoRequest, ::findCityAll, ::onError)
            }
        }

    }

    private fun retrieveAllStorage() {
        showProgress(true)
        var storageRequest = StorageRequest()
        storageRequest.empresa = getSession().empresa
        storageRequest.usuario = getSession().usuario

        saleViewModel.getListStorage(storageRequest, ::findStorageAll, ::onError)
    }

    private fun retrieveAllTypeDocumentGuide() {
        showProgress(true)
        var typeDocumentRequest = TypeDocumentRequest()
        typeDocumentRequest.empresa = getSession().empresa
        typeDocumentRequest.usuario = getSession().usuario
        saleViewModel.getTypeDocumentGuide(
            typeDocumentRequest,
            ::findTypeDocumentoGuideAll,
            ::onError
        )
    }

    private fun retrieveAllOperationGuide() {
        showProgress(true)
        var typeDocumentRequest = OperationGuideRequest()
        typeDocumentRequest.empresa = getSession().empresa
        typeDocumentRequest.usuario = getSession().usuario
        typeDocumentRequest.tipodoc = guideRequest.tipodocumento
        typeDocumentRequest.almacen = guideRequest.almacen
        saleViewModel.getOperationGuide(typeDocumentRequest, ::findOperationGuideAll, ::onError)
    }


    private fun findOperationGuide() {
        showProgress(true)
        var typeDocumentRequest = OperationGuideRequest()
        typeDocumentRequest.empresa = getSession().empresa
        typeDocumentRequest.usuario = getSession().usuario
        typeDocumentRequest.tipodoc = guideRequest.tipodocumento
        typeDocumentRequest.operacion = tielOperationGuide.text.toString()
        typeDocumentRequest.almacen = guideRequest.almacen
        saleViewModel.getOperationGuide(typeDocumentRequest, ::setOperationGuide, ::onError)
    }

    private fun findTypeGuide() {
        showProgress(true)
        var typeDocumentRequest = TypeDocumentRequest()
        typeDocumentRequest.empresa = getSession().empresa
        typeDocumentRequest.usuario = getSession().usuario
        typeDocumentRequest.tipodocuiden = tielTypeGuide.text.toString()
        saleViewModel.getTypeDocumentGuide(typeDocumentRequest, ::setTypeDocumentGuide, ::onError)
    }
    private fun findTypeGuideInit() {
        showProgress(true)
        var typeDocumentRequest = TypeDocumentRequest()
        typeDocumentRequest.empresa = getSession().empresa
        typeDocumentRequest.usuario = getSession().usuario
        typeDocumentRequest.tipodocuiden = "GUR"
        saleViewModel.getTypeDocumentGuide(typeDocumentRequest, ::setTypeDocumentGuide, ::onError)
    }

    private fun showHeadGuide() {
        if (llContainerHeadGuide.visibility == View.GONE) {
            llContainerHeadGuide.visibility = View.VISIBLE
        } else {
            llContainerHeadGuide.visibility = View.GONE
        }
    }

    private fun showTransportGuide() {
        if (llContainerTransport.visibility == View.GONE) {
            llContainerTransport.visibility = View.VISIBLE
        } else {
            llContainerTransport.visibility = View.GONE
        }
    }

    private fun showCarGuide() {
        if (llContainerCar.visibility == View.GONE) {
            llContainerCar.visibility = View.VISIBLE
        } else {
            llContainerCar.visibility = View.GONE
        }
    }

    private fun showDriveGuide() {
        if (llContainerDriver.visibility == View.GONE) {
            llContainerDriver.visibility = View.VISIBLE
        } else {
            llContainerDriver.visibility = View.GONE
        }
    }


    private fun processSale() {
        showProgress(true)
        guideRequest.empresa = getSession().empresa
        guideRequest.usuario = getSession().usuario
        guideRequest.fecha = dateGuide
        guideRequest.observacion  = tielObservationGuide.text.toString()
        guideRequest.numerodoctransportista = tielNumberDocumentTransportGuide.text.toString()
        guideRequest.nombretransportista = tielNameTransportGuide.text.toString()
        guideRequest.tolvavehiculo = tielHopperCarGuide.text.toString()
        guideRequest.direccionconductor = tielDriverAddressGuide.text.toString()
        guideRequest.numerodocconductor = tielDriverDocumentGuide.text.toString()
        guideRequest.nombreconductor = tielDriverNameGuide.text.toString()
        guideRequest.licenciaconductor = tielLicenseDriveGuide.text.toString()
        guideRequest.licenciaconductor = tielLicenseDriveGuide.text.toString()
       // guideRequest.detalle = listGuideDetail
        println("data_guia: ${Gson().toJson(guideRequest)}")

       // saleViewModel.saveGuide(guideRequest, ::resultGuia, ::onError)
        //  saleViewModel.saveSale(::goToResumenPedido, ::onError)
        finish()
        startActivity(Intent(this, GuideActivity::class.java).apply {
            //putExtra(EndingActivity.EXTRA_ENTITY, guideRequest)
            putExtra("Guide",guideRequest as Serializable )
        })

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
            }else {
                d.dismiss()
            } }
        .setCancelable(false)
        .create().show()
}
    private fun showProgress(show: Boolean) {
           fltLoading.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun setFechaOpe() {
        //showDatePickerDialog()
        curDate = Calendar.getInstance()
        // curDate?.add(Calendar.DATE, -1)
        dayMin = curDate?.get(Calendar.DAY_OF_MONTH)!!
        yearMin = curDate?.get(Calendar.YEAR)!!
        monthMin = curDate?.get(Calendar.MONTH)!!
        tielDateGuide!!.setText("$dayMin/${monthMin + 1}/$yearMin")
        dateGuide = Formatter.DateToString(Date())
    }

    private fun showDatePickerDialog() {
        val newFragment = DatePickerDialog(
            this,
            { datePicker, year, month, day -> // +1 because January is zero
                val selectedDate = "$day/${month + 1}/$year"
                tielDateGuide!!.setText(selectedDate)
                dateGuide = "$year${Formatter.formato((month + 1).toString(), 2, "0")}${
                    Formatter.formato(
                        day.toString(),
                        2,
                        "0"
                    )
                }"
            }, yearMin, monthMin, dayMin
        )

        newFragment.show()
    }

    fun findStorageInit() {
        showProgress(true)
        var storageRequest = StorageRequest()
        storageRequest.empresa = getSession().empresa
        storageRequest.usuario = getSession().usuario
        storageRequest.almacen = getSession().tienda
        saleViewModel.getListStorage(storageRequest, ::setStorage, ::onError)
    }


    fun findStorage() {
        showProgress(true)
        var storageRequest = StorageRequest()
        storageRequest.empresa = getSession().empresa
        storageRequest.usuario = getSession().usuario
        storageRequest.almacen = tielStorageGuide.text.toString()
        saleViewModel.getListStorage(storageRequest, ::setStorage, ::onError)
    }

    fun setStorage(storage: List<StorageResponse>) {
        showProgress(false)
        if (storage.isNotEmpty()) {
            tielStorageGuide.setText(storage[0].descripcion.toString())
            etiStorageGuide.hint = "Almacén - ${storage[0].codigo.toString()}"
            etiStorageGuide.error = ""
            guideRequest.almacen = storage[0].codigo.toString()
        } else {
            etiStorageGuide.error = "No existe el almacén."
            tielStorageGuide.setText("")
            etiStorageGuide.hint = "Almacén"
            guideRequest.almacen = null
        }
    }
    fun findTypeAux(){
        showProgress(true)
        var typeAuxGuideRequest =TypeAuxGuideRequest()
        typeAuxGuideRequest.empresa = getSession().empresa
        typeAuxGuideRequest.usuario = getSession().usuario
        typeAuxGuideRequest.tipodoc = guideRequest.tipodocumento
        typeAuxGuideRequest.operacion = guideRequest.operacion
        typeAuxGuideRequest.tipoauxiliar = tielTypeAuxGuide.text.toString()
        if( typeAuxGuideRequest.tipoauxiliar!!.isNotEmpty()){
            saleViewModel.getTypeAuxGuide(typeAuxGuideRequest,::setTypeAux, ::onError )
        }else{
            showProgress(false)
            tielTypeAuxGuide.error = "Esta vacio el campo Tipo auxiliar."
        }
    }
    fun setTypeAux(data: List<DataResponse>) {
        showProgress(false)
        if (data.isNotEmpty()) {
            tielTypeAuxGuide.setText(data[0].descripcion.toString())
            etiTypeAuxGuide.hint = "Tipo Auxiliar - ${data[0].codigo.toString()}"
            etiTypeAuxGuide.error = ""
            guideRequest.tipoauxiliar = data[0].codigo.toString()
        } else {
            etiTypeAuxGuide.error = "No existe el tipo auxiliar."
            tielTypeAuxGuide.setText("")
            etiTypeAuxGuide.hint = "Tipo Auxiliar"
            guideRequest.tipoauxiliar = null
        }
    }
    fun findAux(){
        showProgress(true)
        var auxGuideRequest =AuxGuideRequest()
        auxGuideRequest.empresa = getSession().empresa
        auxGuideRequest.usuario = getSession().usuario
        auxGuideRequest.tipoauxiliar = guideRequest.tipoauxiliar
        auxGuideRequest.auxiliar =  tielAuxGuide.text.toString()
        if(auxGuideRequest.auxiliar != null || auxGuideRequest.auxiliar != ""){
            saleViewModel.getAuxGuide(auxGuideRequest,::setAux, ::onError )
        }else {
            showProgress(false)
            etiAuxGuide.error = "Esta vacio el campo Auxiliar."
        }

    }
    fun searchAllAux(){
        showProgress(true)
        var auxGuideRequest =AuxGuideRequest()
        auxGuideRequest.empresa = getSession().empresa
        auxGuideRequest.usuario = getSession().usuario
        auxGuideRequest.tipoauxiliar = guideRequest.tipoauxiliar
        saleViewModel.getAuxGuide(auxGuideRequest,::findAllAux, ::onError )
    }
    fun setAux(data: List<DataResponse>) {
        showProgress(false)
        if (data.isNotEmpty()) {
            tielAuxGuide.setText(data[0].descripcion.toString())
            etiAuxGuide.hint = "Auxiliar - ${data[0].codigo.toString()}"
            etiAuxGuide.error = ""
            guideRequest.codigoauxiliar = data[0].codigo.toString()
        } else {
            etiAuxGuide.error = "No existe el dato auxiliar."
            tielAuxGuide.setText("")
            etiAuxGuide.hint = "Auxiliar"
            guideRequest.codigoauxiliar = null
        }
    }

    fun setTypeDocumentGuide(data: List<DataResponse>) {
        showProgress(false)
        if (data.isNotEmpty()) {
            tielTypeGuide.setText(data[0].descripcion.toString())
            etiTypeGuide.hint = "Tipo Documento - ${data[0].codigo.toString()}"
            etiTypeGuide.error = ""
            guideRequest.tipodocumento = data[0].codigo.toString()
        } else {
            etiTypeGuide.error = "No existe el almacén."
            tielTypeGuide.setText("")
            etiTypeGuide.hint = "Tipo Documento"
            guideRequest.tipodocumento = null
        }
    }

    fun setOperationGuide(data: List<OperationGuideResponse>) {
        showProgress(false)
        if (data.size > 0) {
            tielOperationGuide.setText(data.get(0).descripcion.toString())
            etiOperationGuide.hint = "Operación - ${data.get(0).codigo.toString()}"
            etiOperationGuide.error = ""
            tielStartAdrressGuide.setText(data.get(0).direccionpartida.toString())
            tielFinishAddressGuide.setText(data.get(0).direccionllegada.toString())
            tielAuxGuide.setText(data.get(0).auxiliar.toString())
            tielTypeAuxGuide.setText(data.get(0).tipoauxiliar.toString())
            tielFinishCityGuide.setText(data.get(0).ubigeollegada.toString())
            tielStartCityGuide.setText(data.get(0).ubigeopartida.toString())
            tielDocumentIdGuide.setText(data.get(0).tipodocuiden.toString())
            tielNumberDocumentIdGuide.setText(data.get(0).docuiden.toString())
            guideRequest.operacion = data.get(0).codigo.toString()
            guideRequest.direccionpartida = data.get(0).direccionpartida.toString()
            guideRequest.direccionllegada = data.get(0).direccionllegada.toString()
            guideRequest.codigoauxiliar =data.get(0).auxiliar.toString()
            guideRequest.tipoauxiliar =data.get(0).tipoauxiliar.toString()
            guideRequest.distritollegada =data.get(0).ubigeollegada.toString()
            guideRequest.distritopartida =data.get(0).ubigeopartida.toString()
            guideRequest.tipodocidentidad =data.get(0).tipodocuiden.toString()
            guideRequest.numerodocidentidad =data.get(0).docuiden.toString()
            findTypeAux()
            findAux()
            searchCity("start")
            searchCity("finish")
        } else {
            etiOperationGuide.error = "No existe el almacén."
            etiOperationGuide.hint = "Operación"

        }
    }

    private fun onError(message: String) {
        Log.e(GuideActivity.TAG, message)
        AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
            .setTitle(R.string.app_name)
            .setMessage(message)
            .setPositiveButton(R.string.aceptar) { d, _ -> d.dismiss() }
            .setCancelable(false)
            .create().show()
    }

    override fun closeDialogStorage(data: StorageResponse) {
        showProgress(false)
        tielStorageGuide.setText(data.descripcion.toString())
        etiStorageGuide.hint = "Almacén - ${data.codigo.toString()}"
        etiStorageGuide.error = ""
        guideRequest.almacen = data.codigo.toString()
    }

    override fun closeDialogTypeDocGuide(data: DataResponse) {
        showProgress(false)
        tielTypeGuide.setText(data.descripcion.toString())
        etiTypeGuide.hint = "Tipo Documento - ${data.codigo.toString()}"
        etiTypeGuide.error = ""
        guideRequest.tipodocumento = data.codigo.toString()
    }

    fun findStorageAll(listaStorage: List<StorageResponse>) {
        showProgress(false)
        val myDialogFragmentPersonas = StoragePopUpFragment()
        myDialogFragmentPersonas.show(supportFragmentManager, "P")
        val args = Bundle()
        args.putSerializable("DataList", listaStorage as Serializable)
        myDialogFragmentPersonas.arguments = args
        val fragment = supportFragmentManager.findFragmentByTag("P")
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
    }

    fun findTypeDocumentoGuideAll(dataList: List<DataResponse>) {
        showProgress(false)
        val dialgCustom = TypeDocPopUpFragment()
        dialgCustom.show(supportFragmentManager, "P")
        val args = Bundle()
        args.putSerializable("DataList", dataList as Serializable)
        dialgCustom.arguments = args
        val fragment = supportFragmentManager.findFragmentByTag("P")
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
    }

    fun findOperationGuideAll(dataList: List<OperationGuideResponse>) {
        showProgress(false)
        val dialgCustom = OperationGuidePopUpFragment()
        dialgCustom.show(supportFragmentManager, "P")
        val args = Bundle()
        args.putSerializable("DataList", dataList as Serializable)
        dialgCustom.arguments = args
        val fragment = supportFragmentManager.findFragmentByTag("P")
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
    }
    fun findAllAux(dataList: List<DataResponse>) {
        showProgress(false)
        val dialgCustom = AuxPopUpFragment()
        dialgCustom.show(supportFragmentManager, "P")
        val args = Bundle()
        args.putSerializable("DataList", dataList as Serializable)
        dialgCustom.arguments = args
        val fragment = supportFragmentManager.findFragmentByTag("P")
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commit()
        }
    }


    override fun closeDialogOperationGuide(data: OperationGuideResponse) {
        tielOperationGuide.setText(data.descripcion.toString())
        etiOperationGuide.hint = "Operacion - ${data.codigo.toString()}"
        etiOperationGuide.error = ""
        tielStartAdrressGuide.setText(data.direccionpartida.toString())
        tielFinishAddressGuide.setText(data.direccionllegada.toString())
        tielAuxGuide.setText(data.auxiliar.toString())
        tielTypeAuxGuide.setText(data.tipoauxiliar.toString())
        tielFinishCityGuide.setText(data.ubigeollegada.toString())
        tielStartCityGuide.setText(data.ubigeopartida.toString())
        tielDocumentIdGuide.setText(data.tipodocuiden.toString())
        tielNumberDocumentIdGuide.setText(data.docuiden.toString())
        guideRequest.operacion = data.codigo.toString()
        guideRequest.direccionpartida = data.direccionpartida.toString()
        guideRequest.direccionllegada = data.direccionllegada.toString()
        guideRequest.codigoauxiliar =data.auxiliar.toString()
        guideRequest.tipoauxiliar =data.tipoauxiliar.toString()
        guideRequest.distritollegada =data.ubigeollegada.toString()
        guideRequest.distritopartida =data.ubigeopartida.toString()
        guideRequest.tipodocidentidad =data.tipodocuiden.toString()
        guideRequest.numerodocidentidad =data.docuiden.toString()
        findTypeAux()
        findAux()
        searchCity("start")
        searchCity("finish")
    }

    override fun closeDialogAuxGuide(data: DataResponse) {

        tielAuxGuide.setText(data.descripcion.toString())
        etiAuxGuide.hint = "Auxiliar - ${data.codigo.toString()}"
        etiAuxGuide.error = ""
        guideRequest.codigoauxiliar = data.codigo.toString()

    }

    override fun closeDialogCity(data: DataResponse, optionData: String) {
        when (optionData) {
            "start" -> {
                tielStartCityGuide.setText(data.descripcion.toString())
                etiStartCityGuide.hint =  "Distrito de Partida - ${data.codigo.toString()}"
                etiStartCityGuide.error = ""
                guideRequest.distritopartida = data.codigo
            }
            "finish" -> {
                tielFinishCityGuide.setText(data.descripcion.toString())
                etiFinishCityGuide.hint =  "Distrito de Llegada - ${data.codigo.toString()}"
                etiFinishCityGuide.error = ""
                guideRequest.distritollegada = data.codigo
            }
            "transport"->{

                    tielCityTransportGuide.setText(data.descripcion.toString())
                    etiCityTransportGuide.hint = "Distrito de Transportista - ${data.codigo.toString()}"
                    etiCityTransportGuide.error = ""
                    guideRequest.distritotransportista = data.codigo.toString()


            }
            "drive" ->{
                    tielCityDriverGuide.setText(data.descripcion.toString())
                    etiCityDriverGuide.hint = "Distrito de Conductor - ${data.codigo.toString()}"
                    etiCityDriverGuide.error = ""
                    guideRequest.distritoconductor = data.codigo.toString()


            }
        }
    }

    override fun closeDialogTypeDocumentIdGuide(data: DataResponse) {
        when(optionTypeDoc){
            "cab" ->{
                tielDocumentIdGuide.setText(data.descripcion.toString())
                etiDocumentIdGuide.hint = "Documento de Identidad - ${data.codigo.toString()}"
                etiDocumentIdGuide.error = ""
                guideRequest.tipodocidentidad = data.codigo.toString()
            }
            "transport" ->{

                tielTypeDocumentTransportGuide.setText(data.descripcion.toString())
                etiTypeDocumentTransportGuide.hint = "Tipo Documento de Transportista - ${data.codigo.toString()}"
                etiTypeDocumentTransportGuide.error = ""
                guideRequest.tipodoctransportista = data.codigo.toString()
            }
            "drive"->{
                tielTypeDocumentDriverGuide.setText(data.descripcion.toString())
                etiTypeDocumentDriverGuide.hint = "Tipo Documento de Conductor - ${data.codigo.toString()}"
                guideRequest.tipodocconductor =  data.codigo.toString()
            }
        }

    }

    override fun closeDialogPlacaGuide(data: DataResponse) {
        tielIdCarGuide.setText(data.codigo.toString())
        tielDescriptionCarGuide.setText(data.descripcion.toString())
        guideRequest.placavehiculo = data.codigo.toString()
        guideRequest.descripcionvehiculo= data.descripcion.toString()
    }

    private fun Confirmacion() {
        val alertDialog: AlertDialog.Builder? = context.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {

                setTitle("Guía de Remision")
                    .setMessage("¿Estas seguro que deseas Continuar?")
                    .setCancelable(false)
                    .setPositiveButton("Si", DialogInterface.OnClickListener { dialog, id ->
                        processSale()
                    }
                    )
                    .setNegativeButton("No",
                        DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })

            }
        }
        val alert: AlertDialog? = alertDialog?.create()
        alert?.show()
    }

    override fun closeDialogDriveGuide(data: DocumentIdResponse) {
        when(optionNumberDoc){
            "cab" -> {
                tielNumberDocumentIdGuide.setText(data.descripcion.toString())
                etiNumberDocumentIdGuide.hint = "Número Documento Identidad - ${data.codigo.toString()}"
                etiNumberDocumentIdGuide.error = ""
                guideRequest.distritollegada = data.ubigeollegada.toString()
                guideRequest.direccionllegada = data.direccionllegada.toString()
                guideRequest.numerodocidentidad =  data.codigo.toString()
                tielFinishCityGuide.setText(data.ubigeollegada.toString())
                tielFinishAddressGuide.setText(data.direccionllegada.toString())
                searchCity("finish")
            }
            "transport" -> {
                tielNumberDocumentTransportGuide.setText( data.codigo.toString())
                tielNameTransportGuide.setText(data.descripcion.toString())
                tielCityTransportGuide.setText( data.ubigeollegada.toString())
                tielAdrressTransportGuide.setText(data.direccionllegada.toString())
                guideRequest.numerodoctransportista = data.codigo.toString()
                guideRequest.nombretransportista = data.descripcion.toString()
                guideRequest.distritotransportista = data.ubigeollegada.toString()
                guideRequest.direcciontransportista = data.direccionllegada.toString()
                if (data.ubigeollegada.toString().isNotEmpty()){
                    searchCity("transport")
                }

                //  etiCityTransportGuide.hint
            }
            "drive" -> {
                tielDriverDocumentGuide.setText( data.codigo.toString())
                tielDriverNameGuide.setText(data.descripcion.toString())
                tielDriverAddressGuide.setText(data.direccionllegada.toString())
                tielCityDriverGuide.setText( data.ubigeollegada.toString())
                if (data.ubigeollegada.toString().isNotEmpty()){
                    searchCity("drive")
                }
            }
        }
    }

    override fun closeDialogTransportGuide(data: DocumentTransportGuideResponse) {
        when(optionNumberDoc){

            "transport" -> {
                tielNumberDocumentTransportGuide.setText( data.codigo.toString())
                tielNameTransportGuide.setText(data.descripcion.toString())
                tielCityTransportGuide.setText( data.ubigeo.toString())
                tielAdrressTransportGuide.setText(data.direccion.toString())
                guideRequest.numerodoctransportista = data.codigo.toString()
                guideRequest.nombretransportista = data.descripcion.toString()
                guideRequest.distritotransportista = data.ubigeo.toString()
                guideRequest.direcciontransportista = data.direccion.toString()
                if (data.ubigeo.toString().isNotEmpty()){
                    searchCity("transport")
                }

                //  etiCityTransportGuide.hint
            }

        }
    }

}