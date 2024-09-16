package com.pedidos.android.persistence.ui

import android.content.Intent
import com.google.zxing.integration.android.IntentIntegrator

class FragmentIntentIntegrator(private val fragment: android.support.v4.app.Fragment): IntentIntegrator(fragment.activity) {
    override fun startActivityForResult(intent: Intent?, code: Int) {
        fragment.startActivityForResult(intent, code)
    }
}