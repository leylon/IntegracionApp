package com.pedidos.android.persistence.api

import android.util.Log
import com.google.gson.GsonBuilder
import com.pedidos.android.persistence.db.entity.SaleSubItemEntity
import com.pedidos.android.persistence.model.SaleSubItem
import com.pedidos.android.persistence.model.pagos.PaymentNcrRequest
import com.pedidos.android.persistence.model.pagos.PaymentNcrResponse
import com.pedidos.android.persistence.model.pagos.PaymentValeRequest
import com.pedidos.android.persistence.model.pagos.PaymentValeResponse
import com.pedidos.android.persistence.model.picking.PickingTerminarRequest
import com.pedidos.android.persistence.model.picking.PickingTerminarResponse
import com.pedidos.android.persistence.ui.BasicApp
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

interface ApiExternas {

    @POST
    fun pagovale(@Url url:String, @Body body: PaymentValeRequest): Call<PaymentValeResponse>

    @POST
    fun pagoNcr(@Url url:String,@Body body: PaymentNcrRequest): Call<PaymentNcrResponse>



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