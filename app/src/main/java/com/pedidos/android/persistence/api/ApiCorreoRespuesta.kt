package com.pedidos.android.persistence.api

import android.util.Log
import com.google.gson.GsonBuilder
import com.pedidos.android.persistence.db.entity.SaleSubItemEntity
import com.pedidos.android.persistence.model.SaleSubItem
import com.pedidos.android.persistence.model.sale.EnvioCodigoRequest
import com.pedidos.android.persistence.model.sale.EnvioCodigoResponse
import com.pedidos.android.persistence.model.sale.EnvioCorreoRequest
import com.pedidos.android.persistence.model.sale.EnvioCorreoResponse
import com.pedidos.android.persistence.ui.BasicApp
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface ApiCorreoRespuesta {
    @POST(BasicApp.DEFAULT_API_FACTURACION+"venta1producto_enviocorreo")
fun ventaProductoEnvioCorreo(@Body body: EnvioCorreoRequest): Call<EnvioCorreoResponse>

    @POST(BasicApp.DEFAULT_API_FACTURACION+"enviocodigorespuesta")
    fun envioCodigoRespuesta(@Body body: EnvioCodigoRequest): Call<EnvioCodigoResponse>


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
                .registerTypeAdapter(
                    SaleSubItem::class.java, InterfaceAdapter<SaleSubItem>(
                        SaleSubItemEntity::class.java))

            return Retrofit.Builder()
                .baseUrl(httpUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                .build()
                .create(CoolboxApi::class.java)
        }
    }
}