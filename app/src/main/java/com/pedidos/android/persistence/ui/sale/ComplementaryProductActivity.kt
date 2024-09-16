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
import com.pedidos.android.persistence.viewmodel.ComplementaryProductViewModel
import kotlinx.android.synthetic.main.activity_complementary_product.*
import kotlinx.android.synthetic.main.activity_complementary_product.btnProcess
import kotlinx.android.synthetic.main.activity_complementary_product.btnRegisterProducts
import kotlinx.android.synthetic.main.activity_complementary_product.fltLoading
import kotlinx.android.synthetic.main.activity_complementary_product.textCodeProduct
import kotlinx.android.synthetic.main.activity_complementary_product.textDescriptionProduct
import kotlinx.android.synthetic.main.activity_complementary_product.textPriceProduct
import kotlinx.android.synthetic.main.activity_complementary_product.textSecuencial
import kotlinx.android.synthetic.main.activity_complementary_product.toolbar
import kotlinx.android.synthetic.main.activity_garanties_product.*

class ComplementaryProductActivity : BaseActivity() {

    private lateinit var saleViewModel: ComplementaryProductViewModel
    private val adapter = ComplementaryProductAdapter(mutableListOf()) {
        setCheckProductComp(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complementary_product)

        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        // toolbar.title = "${getString(R.string.complementary_title)} ${getSession().tienda}"
        toolbar.title = "${getString(R.string.complementary_products_tittle)}"

        btnProcess.setOnClickListener { onBackPressed() }
        btnRegisterProducts.setOnClickListener { addProducts() }

        rvwProducts.layoutManager = LinearLayoutManager(this)
        rvwProducts.adapter = adapter

        //this init the viewModel
        val compFactory = ComplementaryProductViewModel.Companion.Factory(application, getSettings().urlbase)
        saleViewModel = ViewModelProviders.of(this, compFactory).get(ComplementaryProductViewModel::class.java)

        saleViewModel.complementData.observe(this, Observer {
            showProgress(false)
            adapter.addItems(it!!)
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

        saleViewModel.changeStatusComp = {adapter.addItemsCheck(it)}
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

    private fun addProducts() {
        var listProductsSelected = saleViewModel.getAllSelectedProducts()
        val intent = Intent()

        listProductsSelected.forEachIndexed { index, item ->
            val productEntity = ProductEntity(
                codigoVenta = item.codigo,
                descripcion = item.descripcion,
                precio = item.precio,
                complementaryRowColor = item.rowColor,
                monedaSimbolo = item.monedaSimbolo,
                stimei = false,
                cantidad = 1,
                codigo = "")
            productEntity.secgaraexte = this.intent.extras.getInt(PROD_SEC)
            productEntity.codgaraexte = this.intent.extras.getString(PROD_ID)
            productEntity.complementaryRowColor = item.rowColor
            intent.putExtra(SearchProductActivity.PRODUCT_KEY+index, productEntity)
        }

        intent.putExtra(SearchProductActivity.SIZE_PRODUCTS,listProductsSelected.size)
        setResult(Activity.RESULT_OK, intent)
        finish()

    }
    private fun addProduct(item: ProductComplementaryEntity) {
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

    private fun setCheckProductComp(position : Int) {
        saleViewModel.changeCheckToProductComp(position)
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