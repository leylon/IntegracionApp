package com.pedidos.android.persistence.ui.sale

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.db.entity.ProductComplementaryEntity
import com.pedidos.android.persistence.db.entity.ProductEntity
import com.pedidos.android.persistence.ui.BaseActivity
import com.pedidos.android.persistence.ui.search.SearchProductActivity
import com.pedidos.android.persistence.viewmodel.GarantiesProductViewModel
import kotlinx.android.synthetic.main.activity_complementary_product.btnProcess
import kotlinx.android.synthetic.main.activity_complementary_product.fltLoading
import kotlinx.android.synthetic.main.activity_complementary_product.textCodeProduct
import kotlinx.android.synthetic.main.activity_complementary_product.textDescriptionProduct
import kotlinx.android.synthetic.main.activity_complementary_product.textPriceProduct
import kotlinx.android.synthetic.main.activity_complementary_product.textSecuencial
import kotlinx.android.synthetic.main.activity_complementary_product.toolbar
import kotlinx.android.synthetic.main.activity_garanties_product.*

class GarantiesProductActivity : BaseActivity() {

    private lateinit var saleViewModel: GarantiesProductViewModel
    private val adapterExt = GarantieProductAdapter(mutableListOf()) { setCheckProductExt(it)}
    private val adapterDamage = GarantieProductAdapter(mutableListOf()) { setCheckProductDamage(it)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_garanties_product)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        // toolbar.title = "${getString(R.string.complementary_title)} ${getSession().tienda}"
        toolbar.title = "${getString(R.string.garanties_tittle)}"

        btnProcess.setOnClickListener { onBackPressed() }
        btnRegisterProducts.setOnClickListener { addProducts() }
        rvwProductsDamage.layoutManager = LinearLayoutManager(this)
        rvwProductsDamage.adapter = adapterDamage
        rvwProductsExt.layoutManager = LinearLayoutManager(this)
        rvwProductsExt.adapter = adapterExt

        //this init the viewModel
        val compFactory = GarantiesProductViewModel.Companion.Factory(application, getSettings().urlbase)
        saleViewModel = ViewModelProviders.of(this, compFactory).get(GarantiesProductViewModel::class.java)

        saleViewModel.complementDataExt.observe(this, Observer {
            showProgress(false)
            adapterExt.addItems(it!!)
        })

        saleViewModel.complementDataDamage.observe(this, Observer {
            showProgress(false)
            adapterDamage.addItems(it!!)
        })

        saleViewModel.emptyComplementDate.observe(this, Observer {
            onError(it!!)
        })
        saleViewModel.showProgress.observe(this, Observer {
            showProgress(it!!)
        })

        saleViewModel.message.observe(this, Observer {
            if (it != null) {
                printOnSnackBar(it)
            }
        })

        saleViewModel.changeStatusExt = {adapterExt.addItemsCheck(it)}
        saleViewModel.changeStatusDamage = {adapterDamage.addItemsCheck(it)}

        showProgress(true)
        val extras = intent.extras
        textDescriptionProduct.text = extras.getString(PROD_DESCRIP)
        textCodeProduct.text = extras.getString(PROD_ID)
        textSecuencial.text = extras.getInt(PROD_SEC).toString()
        textPriceProduct.text = extras.getString(PROD_COIN)+""+extras.getDouble(PROD_PRICE).toString()
        saleViewModel.loadComplement(extras.getString(PROD_ID), extras.getDouble(PROD_PRICE).toString())
    }

    private fun showProgress(show: Boolean) {
        fltLoading.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setCheckProductExt(position : Int) {
        saleViewModel.changeCheckToProductExt(position)
    }

    private fun setCheckProductDamage(position: Int) {
        saleViewModel.changeCheckToProductDamage(position) }

    private fun addProductDamage(item: ProductComplementaryEntity) {
        val productEntity = ProductEntity(
            codigoVenta = item.codigo,
            descripcion = item.descripcion,
            precio = item.precio,
            complementaryRowColor = item.rowColor,
            monedaSimbolo = item.monedaSimbolo,
            stimei = false,
            cantidad = 1,
            codigo = "")
        productEntity.secgaraexte = intent.extras.getInt(PROD_SEC)
        productEntity.codgaraexte = intent.extras.getString(PROD_ID)
        productEntity.complementaryRowColor = item.rowColor

        val intent = Intent()
        intent.putExtra(SearchProductActivity.PRODUCT_KEY, productEntity)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun addProducts() {
        var itemExt : ProductComplementaryEntity?  = saleViewModel.dataExtSelected
        var itemDamage : ProductComplementaryEntity? = saleViewModel.dataDamageSelected

        val intent = Intent()

        if(itemExt != null) {
            val productEntityExt = ProductEntity(
                codigoVenta = itemExt.codigo,
                descripcion = itemExt.descripcion,
                precio = itemExt.precio,
                complementaryRowColor = itemExt.rowColor,
                monedaSimbolo = itemExt.monedaSimbolo,
                stimei = false,
                cantidad = 1,
                codigo = "")

            productEntityExt.secgaraexte = this.intent.extras.getInt(PROD_SEC)
            productEntityExt.codgaraexte = this.intent.extras.getString(PROD_ID)
            productEntityExt.complementaryRowColor = itemExt.rowColor
            intent.putExtra(SearchProductActivity.PRODUCT_KEY_EXT, productEntityExt)

        }


        if(itemDamage != null) {
            val productEntityDamage = ProductEntity(
                codigoVenta = itemDamage.codigo,
                descripcion = itemDamage.descripcion,
                precio = itemDamage.precio,
                complementaryRowColor = itemDamage.rowColor,
                monedaSimbolo = itemDamage.monedaSimbolo,
                stimei = false,
                cantidad = 1,
                codigo = "")
            productEntityDamage.secgaraexte = this.intent.extras.getInt(PROD_SEC)
            productEntityDamage.codgaraexte = this.intent.extras.getString(PROD_ID)
            productEntityDamage.complementaryRowColor = itemDamage.rowColor
            intent.putExtra(SearchProductActivity.PRODUCT_KEY_DAMAGE, productEntityDamage)

        }

        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun onError(message: String) {
        Log.e(SaleActivity.TAG, message)

        AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
            .setTitle(R.string.app_name)
            .setMessage(message)
            .setPositiveButton(R.string.aceptar) { d, _ ->
                d.dismiss()
                setResult(Activity.RESULT_CANCELED, intent)
                finish()}
            .setCancelable(false)
            .create().show()
    }

    companion object {
        const val PROD_ID = "prod_id"
        const val COM_PROD_ID = "com_prod_id"
        const val PROD_DESCRIP = "prod_descrip"
        const val PROD_SEC  = "prod_sec"
        const val PROD_PRICE = "prod_price"
        const val PROD_COIN = "prod_coin"
    }
}