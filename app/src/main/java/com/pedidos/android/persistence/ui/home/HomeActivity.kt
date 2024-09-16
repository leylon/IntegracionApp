package com.pedidos.android.persistence.ui.home

import android.content.Intent
import android.os.Bundle
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.ui.login.LoginActivity
import com.pedidos.android.persistence.ui.menu.MenuActivity
import com.pedidos.android.persistence.ui.sale.SaleActivity
import kotlinx.android.synthetic.main.home_activity.*


class HomeActivity : MenuActivity() {

    companion object {
        val TAG = HomeActivity::class.java.simpleName!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//        val contentView = inflater.inflate(R.layout.home_activity, null, false)
//        drawerLayout.addView(contentView, 0)
        setContentViewWithMenu(R.layout.home_activity)
        checkSession()

        tvwPedidoNuevo.setOnClickListener { startActivity(Intent(this, SaleActivity::class.java)) }
        tvwlogout.setOnClickListener { performLogOut() }
    }

    private fun performLogOut() {
        cleanSession()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
