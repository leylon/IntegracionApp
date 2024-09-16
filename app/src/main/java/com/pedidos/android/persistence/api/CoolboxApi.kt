package com.pedidos.android.persistence.api

import android.util.Log
import com.pedidos.android.persistence.db.entity.*
import com.pedidos.android.persistence.model.*
import com.pedidos.android.persistence.utils.ApiWrapper
import com.google.gson.GsonBuilder
import com.pedidos.android.persistence.model.guide.*
import com.pedidos.android.persistence.model.inventary.InventaryGenerateRequest
import com.pedidos.android.persistence.model.inventary.InventaryRequest
import com.pedidos.android.persistence.model.inventary.InventaryResponse
import com.pedidos.android.persistence.model.inventary.InventaryResponseStatus
import com.pedidos.android.persistence.model.pagos.PagoValeRequest
import com.pedidos.android.persistence.model.pagos.PagoValeResponse
import com.pedidos.android.persistence.model.picking.*
import com.pedidos.android.persistence.model.sale.*
import com.pedidos.android.persistence.model.transfer.*
import com.pedidos.android.persistence.ui.BasicApp
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface CoolboxApi {
    //login
    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"login")
    fun login(@Body login: Login): Call<ApiWrapper<LoginResponse>>

    //client
    @GET(BasicApp.DEFAULT_API_VENTA_MOVIL+"client_search")
    fun getClients(@Query("codigo") codigo: String, @Query("tipo") documentType: Int): Call<ApiWrapper<ClientResponseEntity>>

    //client
    @GET(BasicApp.DEFAULT_API_VENTA_MOVIL+"client_find")
    fun getClientsByReniecSunat(@Query("codigo") codigo: String, @Query("tipo") documentType: Int): Call<ApiWrapper<ClientResponseEntity>>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"clientecrea")
    fun insertClient(@Body client: ClientEntity): Call<ApiWrapper<ClientResponseEntity>>

    //product complementary
    @GET(BasicApp.DEFAULT_API_VENTA_MOVIL+"productoscomplementarios")
    fun getProductComplementary(@Query("codigoVenta") codigoProducto: String
                                , @Query("precioventa") precioVenta : String): Call<ApiWrapper<List<ProductComplementaryEntity>>>

    @GET(BasicApp.DEFAULT_API_VENTA_MOVIL+"ProductosComplementarios")
    fun getComplementaryProducts(@Query("codigoVenta") codigoProducto: String
    ): Call<ApiWrapper<List<ProductComplementaryEntity>>>
    //product garantie
    @GET(BasicApp.DEFAULT_API_VENTA_MOVIL+"garantiaextendida")
    fun getProductsGarantie(@Query("codigoventa") codigoProducto: String
                                , @Query("precioventa") precioVenta : String): Call<ApiWrapper<List<ProductComplementaryEntity>>>


    //sales
    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"pedido")
    fun insertSale(@Body body: SaleEntity): Call<ApiWrapper<SaleEntity>>

    // cards
    @GET(BasicApp.DEFAULT_API_VENTA_MOVIL+"tarjeta?codigo")
    fun cardsAvailable(): Call<ApiWrapper<ArrayList<SelectedCreditCard>>>

    //other payments
    @GET(BasicApp.DEFAULT_API_VENTA_MOVIL+"formapago?codigo")
    fun otherPatments(): Call<ApiWrapper<ArrayList<SelectedOtherPayment>>>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"pedidodetalle")
    fun insertSaleSubItem(@Body body: SaleEntity): Call<ApiWrapper<SaleEntity>>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"pedidodetalle2")
    fun insertSaleSubItem2(@Body body: SaleEntity): Call<ApiWrapper<SaleEntity>>

    @GET(BasicApp.DEFAULT_API_VENTA_MOVIL+"Product")
    fun searchProduct(@Query("codigoVenta") codigoProducto: String): Call<ApiWrapper<ProductEntity>>

    @GET(BasicApp.DEFAULT_API_VENTA_MOVIL+"listaproducto")
    fun searchProductDescription(@Query("codigoVenta") codigoProducto: String): Call<ApiWrapper<List<ProductEntity>>>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"pedidoimprimir")
    fun getReceipt(@Body body: ReceiptRequest): Call<ApiWrapper<ReceiptEntity>>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+BasicApp.PEDIDO_PAGAR_NUEVO)
    fun payReceiptNew(@Body body: PaymentEntity): Call<ApiWrapper<PaymentResponseEntity>>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"pagolinkintencionpago")
    fun getPagoLink(@Body body : PaymentIntentionsEntity) : Call<PaymentIntentionResponseEntity>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"pagofpayintencionpago")
    fun getFpay(@Body body : PaymentIntentionsEntity) : Call<PaymentIntentionResponseEntity>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"pagofpayconsultapago")
    fun checkPaymentFpay(@Body body: PaymentIntentionResponseEntity) : Call<PaymentIntentionResponseEntity>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"pagofpayreversapago")
    fun reverseFpayPayment(@Body body : PaymentIntentionResponseEntity) : Call<PaymentIntentionResponseEntity>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"pagolinkconsultapago")
    fun checkPaymentPLink(@Body body: PaymentIntentionResponseEntity) : Call<PaymentIntentionResponseEntity>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"reimprimirticket")
    fun getReceiptForReprint(@Body body: ReceiptRequest): Call<ApiWrapper<ReceiptEntity>>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"documentopdfimprimir")
    fun getReceiptPDF(@Body body: ReceiptRequest): Call<ApiWrapper<ReceiptEntity>>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"pedidopagar")
    fun payReceipt(@Body body: PaymentEntity): Call<ApiWrapper<PaymentResponseEntity>>

    @GET(BasicApp.DEFAULT_API_VENTA_MOVIL+"imei")
    fun checkImei(@Query("codigoventa") saleCode: String, @Query("codigoimei") imei: String): Call<ApiWrapper<CheckImeiResponse>>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"PedidoAnular")
    fun cancelSale(@Body body: CancelSaleEntity): Call<ApiWrapper<CancelSaleResponseEntity>>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"PedidoReporte")
    fun reportOperations(@Body body: OperationReportEntity): Call<ApiWrapper<OperationReportResponseEntity>>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"pedidoCierre")
    fun cashBalance(@Body body: CashBalanceEntity): Call<ApiWrapper<CashBalanceResponseEntity>>

    //Generated documents
    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"listadodocumentos")
    fun generatedDocuments(@Body body: CashBalanceEntity): Call<ApiWrapper<List<GeneratedDocumentEntity>>>

    //List Storage
    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"gr/almacen")
    fun getStorageGr(@Body body: StorageRequest): Call<List<StorageResponse>>

    //List Type Documeto
    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"gr/tipodocu")
    fun getTypeDocumentGuide(@Body body: TypeDocumentRequest): Call<List<DataResponse>>

    //Operation of Guide
    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"gr/operacion")
    fun getOperationGuide(@Body body: OperationGuideRequest): Call<List<OperationGuideResponse>>

    //List of Type AuxGuide
    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"gr/tipoauxiliar")
    fun getTypeAuxGuide(@Body body: TypeAuxGuideRequest): Call<List<DataResponse>>

    //AuxGuide
    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"gr/auxiliar")
    fun getAuxGuide(@Body body: AuxGuideRequest): Call<List<DataResponse>>

    //List Ubigeo
    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"gr/ubigeo")
    fun getUbigeo(@Body body: UbigeoRequest): Call<List<DataResponse>>

    //List Type Documents
    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"gr/tipodocuiden")
    fun getTypeDocumentId(@Body body: TypeDocumentRequest): Call<List<DataResponse>>

    //DocumentId Guide
    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"gr/docuiden")
    fun getDocumentIdGuide(@Body body: DocumentIdRequest): Call<List<DocumentIdResponse>>

    //DocumentId Drive Guide
    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"gr/docuiden")
    fun getDocumentIdAllGuide(@Body body: DocumentIdAllRequest): Call<List<DocumentIdResponse>>


    //Data Tranport Guide
    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"gr/transportista")
    fun getDataTranport(@Body body: DocumentTransportGuideRequest): Call<List<DocumentTransportGuideResponse>>

    //Data Car Guide
    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"gr/vehiculo")
    fun getPlacaCarGuide(@Body body: PlacaCarGuideRequest): Call<List<DataResponse>>

    // Save Guide Head and Detail
    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"gr/registroguia")
    fun saveGuide(@Body body: GuideRequest): Call<GuideResponse>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"inventariolista")
    fun listaInventary(@Body body: InventaryRequest): Call<InventaryResponse>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"inventariogenerar")
    fun generateInventary(@Body body: InventaryGenerateRequest): Call<InventaryResponseStatus>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"pagovale")
    fun pagovale(@Body body: PagoValeRequest): Call<PagoValeResponse>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"listarguiatransferencia")
    fun listaGuideTransfer(@Body body: TransferRequest): Call<TransferResponse>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"mostrarguiatransferencia")
    fun showGuideTranfer(@Body body: TransferShowRequest): Call<TransferShowResponse>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"confirmacionguiatransferencia")
    fun confirmarGuideTranfer(@Body body: TransferFinishRequest): Call<GuideResponse>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"venta1producto")
    fun ventaProducto(@Body body: List<VentaProductoRequest>): Call<VentaProductoResponse>
    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"venta1producto")
    fun ventaProducto(@Body body: VentaProductoRequest): Call<VentaProductoResponse>

    @POST(BasicApp.DEFAULT_API_FACTURACION+"venta1producto_enviocorreo")
    fun ventaProductoEnvioCorreo(@Body body: EnvioCorreoRequest): Call<EnvioCorreoResponse>

    @POST(BasicApp.DEFAULT_API_FACTURACION+"enviocodigorespuesta")
    fun envioCodigoRespuesta(@Body body: EnvioCodigoRequest): Call<EnvioCodigoResponse>

    @POST(BasicApp.DEFAULT_API_VENTA_MOVIL+"venta1producto_validacodigo")
    fun ventaProductoValidaCodigo(@Body body: VentaProductoValidaCodigoRequest): Call<VentaProductoValidaCodigoResponse>

    @POST(BasicApp.DEFAULT_API_PICKING+"listapedido")
    fun obtenerPedidoPacking(@Body body: PrincipalPedidoRequest): Call<PrincipalPedidosPicking>

    @POST(BasicApp.DEFAULT_API_PICKING+"verpedido")
    fun obtenerPedido(@Body body: PedidoRequest): Call<PedidoDetail>

    @POST(BasicApp.DEFAULT_API_PICKING+"pickingextorno")
    fun pedidoExtorno(@Body body: PickingExtornoRequest): Call<PickingExtornoTerminarResponse>

    @POST(BasicApp.DEFAULT_API_PICKING+"pickingterminar")
    fun pickingTerminar(@Body body: PickingTerminarRequest): Call<PickingTerminarResponse>

    @POST(BasicApp.DEFAULT_API_PICKING+"pickado")
    fun pickado(@Body body: PickingRequest): Call<PedidoDetail>

    @POST(BasicApp.DEFAULT_API_PICKING+"guiascompletas")
    fun reportPickado(@Body body: PedidoRequest): Call<ReportPickingResponse>

    companion object {
        fun create(urlBase: String): CoolboxApi = create(HttpUrl.parse(urlBase)!!)

        fun create(httpUrl: HttpUrl): CoolboxApi {
            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { it ->
                Log.d("API", it)
            })
            logger.level = HttpLoggingInterceptor.Level.BODY

            val client = OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build()

            val gsonBuilder = GsonBuilder()
                    //.registerTypeAdapter(SaleSubItem::class.java, SaleSubItemEntityCreator())
                    //InstanceCreator()todo: try it
                    .registerTypeAdapter(SaleSubItem::class.java, InterfaceAdapter<SaleSubItem>(SaleSubItemEntity::class.java))

            return Retrofit.Builder()
                    .baseUrl(httpUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                    .build()
                    .create(CoolboxApi::class.java)
        }
    }
}