package com.pedidos.android.persistence.ui.menu

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.db.entity.SettingsEntity
import com.pedidos.android.persistence.ui.BaseActivity
import com.pedidos.android.persistence.ui.cancel.CancelActivity
import com.pedidos.android.persistence.ui.guide.*
import com.pedidos.android.persistence.ui.login.LoginActivity
import com.pedidos.android.persistence.ui.reports.CashBalanceActivity
import com.pedidos.android.persistence.ui.reports.GeneratedDocumentsActivity
import com.pedidos.android.persistence.ui.reports.OperationsReportActivity
import com.pedidos.android.persistence.ui.sale.SaleActivity
import com.pedidos.android.persistence.utils.ExceptionHandler
import kotlinx.android.synthetic.main.menu_activity.*

open class MenuActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        const val SETTINGS = "com.example.android.persistance.ui.menu.settingsActivity.settings"
        const val NAMESPACE = "com.example.android.persistence.ui.menu"
        const val SETTINGS_REQUEST = 1235
    }

    protected lateinit var drawerLayout: DrawerLayout
    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //if (!BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))
        //}

        setContentView(R.layout.menu_activity)

        drawerLayout = drawer_layout
        nav_view.setNavigationItemSelectedListener(this)
        nav_view.getHeaderView(0).findViewById<TextView>(R.id.textViewBienvenida).text = getString(R.string.nav_header_subtitle) + " " + getSession().vendedornombre
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                startActivityForResult(Intent(this, SettingsActivity::class.java).putExtra(SettingsActivity.SETTINGS_KEY, getSettings())
                        , SETTINGS_REQUEST)
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_pedido -> {
                finish()
                startActivity(Intent(this, SaleActivity::class.java))
            }
            /* R.id.nav_pedido_anular -> {
                finish()
                startActivity(Intent(this, CancelActivity::class.java))
            }
            R.id.nav_pedido_reporte_operaciones -> {
                finish()
                startActivity(Intent(this, OperationsReportActivity::class.java))
            } */
            R.id.nav_cierre_caja -> {
                finish()
                startActivity(Intent(this, CashBalanceActivity::class.java))
            }
            R.id.nav_documentos_generados -> {
                finish()
                startActivity(Intent(this, GeneratedDocumentsActivity::class.java))
            }
            R.id.nav_log_out -> {
                performLogOut()
            }
            /*R.id.nav_guia_remision -> {
                finish()
                startActivity(Intent(this, GuideHeadActivity::class.java))
            }
            R.id.nav_invetary -> {
                finish()
                startActivity(Intent(this, InventaryActivity::class.java))
            }
            R.id.nav_confirmar_transferencia -> {
                finish()
                startActivity(Intent(this, TransferPedidoActivity::class.java))
            }
            R.id.nav_packing -> {
                finish()
                startActivity(Intent(this, PackingActivity::class.java))
            }
            R.id.nav_packing -> {
                finish()
                startActivity(Intent(this, PackingActivity::class.java))
            }
            R.id.nav_report_packing -> {
                finish()
                startActivity(Intent(this, ReportPackingPrincipal::class.java))

            }  */

        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SETTINGS_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    val settingsEntity: SettingsEntity = data!!.getParcelableExtra(SettingsActivity.SETTINGS_KEY)
                    saveSetting(settingsEntity)
                    printOnSnackBar("setted: " + settingsEntity.urlbase)
                }
            }
        }
    }

    private fun performLogOut() {
        cleanSession()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    protected fun setContentViewWithMenu(@LayoutRes layoutResID: Int) {
        val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val contentView = inflater.inflate(layoutResID, null, false)
        drawerLayout.addView(contentView, 0)
    }

    protected fun setSupportActionBarMenu(toolBar: Toolbar) {
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
    }
}
