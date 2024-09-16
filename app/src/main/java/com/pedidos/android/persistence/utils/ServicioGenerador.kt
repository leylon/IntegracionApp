package com.pedidos.android.persistence.utils

import android.text.TextUtils
import android.util.Log
import com.google.gson.GsonBuilder
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

/**
 * Created by Leyton Oc on 13/08/2018.
 */

object ServicioGenerador {
    private val okHttpClient: OkHttpClient? = null

    fun <S> createService(serviceClass: Class<S>, baseUrl: String): S {
        return createService(serviceClass, baseUrl, null as Interceptor?, null)
    }

    fun <S> createService(serviceClass: Class<S>, baseUrl: String, interceptor: Interceptor): S {
        return createService(serviceClass, baseUrl, interceptor, null)
    }

    fun <S> createService(
        serviceClass: Class<S>,
        baseUrl: String,
        factories: Array<Converter.Factory>
    ): S {
        return createService(serviceClass, baseUrl, null, factories)
    }

    fun <S> createService(
        serviceClass: Class<S>,
        baseUrl: String,
        interceptor: Interceptor?,
        factories: Array<Converter.Factory>?
    ): S {
        // In retrofit 1.9 Retrofit class is RestAdapter, baseUrl(String) is setEndpoint(String); addInterceptor(Interceptor) is setRequestInterceptor(RequestInterceptor)
        // OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        val httpClient = OkHttpClient().newBuilder().addInterceptor(object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                val originalRequest = chain.request()
                Log.v("ley request: ", chain.request().toString())
                // Log.v("ley : ",chain.connection().toString());
                val headers = originalRequest.headers()
                val builder = originalRequest.newBuilder().headers(headers)

                val newRequest = builder.build()
                return chain.proceed(newRequest)
            }
        })

        val gson = GsonBuilder().setLenient().create()
        val builder = Retrofit.Builder()

        //check if contains default converter(JSON)
        var containsDefaultConverter = false
        if (factories != null && factories.size > 0) {
            for (factory in factories) {
                builder.addConverterFactory(factory) //The factory who takes care for serialization and deserialization of objects
                if (factory is GsonConverterFactory)
                    containsDefaultConverter = true
            }
        }

        //if it does not contain default converter, then add it
        if (!containsDefaultConverter)
            builder.addConverterFactory(GsonConverterFactory.create(gson))

        if (!TextUtils.isEmpty(baseUrl))
            builder.baseUrl(HttpUrl.parse(baseUrl)!!)

        if (interceptor != null && !httpClient.interceptors().contains(interceptor))
            httpClient.addInterceptor(interceptor)
      //  builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        builder.client(httpClient.build())
        val retrofit = builder.build()
        return retrofit.create(serviceClass)
    }
}
