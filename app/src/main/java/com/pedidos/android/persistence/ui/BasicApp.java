/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pedidos.android.persistence.ui;

import android.app.Application;

import com.pedidos.android.persistence.BuildConfig;
import com.pedidos.android.persistence.api.ApiCorreo;
import com.pedidos.android.persistence.api.CoolboxApi;

/**
 * Android Application class. Used for accessing singletons.
 */
public class BasicApp extends Application {

    //dev
    //public static final String URL_VISA = "https://devapi.vnmpos.com/ecore.gateway/api/v1/transactions";

    //Production
    public static final String URL_VISA = "https://api.vnmpos.com/ecore.gateway/api/v1/transactions";

    public static final String KEY_VISA = "9326EF6A-9CFA-11E4-8CEB-5794F5E17625";

    public static final String DEFAULT_API_VENTA_MOVIL = "SKMWSVentaMovil/api/";
    public static final String DEFAULT_API_FACTURACION = "WSFacturacion/api/";
    public static final String DEFAULT_API_PICKING = "WSPicking/api/";

    public static final String PEDIDO_PAGAR_NUEVO = BuildConfig.URL_PEDIDO_PAGAR_NUEVO;

    public static final String DEFAULT_BASE_URL_DEBUG = "https://wsfacturacion.coolbox.com.pe:9443/SKM/";
    //public static final String DEFAULT_BASE_URL_DEBUG = "http://192.168.1.2/SKMWSVentaMovil/api/";
    public static final String DEFAULT_BASE_URL = "http://192.168.1.2/SKMWSVentaMovil/api/";

    private CoolboxApi mrepository;

    private ApiCorreo mrepositoryApiCorreo;

    //Serial version
    public static final String APP_VERSION = BuildConfig.VERSION_APP;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public CoolboxApi getApiRepository(String urlBase) {
        if (mrepository == null) {
            mrepository = CoolboxApi.Companion.create(urlBase);
        }

        return mrepository;
    }
    public ApiCorreo getApiRepositoryCorreo(String urlBase) {
        if (mrepositoryApiCorreo == null) {
            mrepositoryApiCorreo = ApiCorreo.Companion.create(urlBase);
        }

        return mrepositoryApiCorreo;
    }
}
