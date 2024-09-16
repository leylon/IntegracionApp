package com.pedidos.android.persistence.ui.error

import android.os.Bundle
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.ui.menu.MenuActivity
import kotlinx.android.synthetic.main.error_activity.*

class ErrorActivity : MenuActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentViewWithMenu(R.layout.error_activity)
        setSupportActionBarMenu(toolbar)

        error.text = intent.getStringExtra("error")
    }
}
