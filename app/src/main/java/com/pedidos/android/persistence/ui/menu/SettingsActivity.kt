package com.pedidos.android.persistence.ui.menu

import android.app.Activity
import android.content.Intent
import android.os.Build

import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.gson.Gson
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.db.entity.SettingsEntity

import kotlinx.android.synthetic.main.settings_activity.*
import java.util.*

class SettingsActivity : AppCompatActivity() {
    companion object {

        const val SETTINGS_KEY = "settings_key"
    }
    var pageSize = "80mm"
    var typePrint = "SUNMI"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        setSupportActionBar(toolbar)

        bntwSaveChanges.setOnClickListener { saveChanges() }
        val androidID: String =
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        val uuID: String = UUID.randomUUID().toString()
        tvwAndroid_UUID.text = uuID
        tvwAndroid_ID.text = androidID
        val spinner: Spinner = findViewById(R.id.spnSizeImpresora)
        val spinnerTypePrint: Spinner = findViewById(R.id.spnTypeImpresora)
        val sizes = arrayOf("80mm", "58mm","48mm")
        val typePrints = arrayOf("SUNMI", "HIOPOS","GENERIC","POSD")
        val adapter = ArrayAdapter(
            this, // Contexto
            android.R.layout.simple_spinner_item, // Layout por defecto
            sizes // Datos
        )
        val adapterTypePrint = ArrayAdapter(
            this, // Contexto
            android.R.layout.simple_spinner_item, // Layout por defecto
            typePrints // Datos
        )

        spinner.adapter = adapter
        spinnerTypePrint.adapter = adapterTypePrint
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedSize = sizes[position]
                pageSize = selectedSize
                // Aquí puedes hacer algo con la selección
                val settings = SettingsEntity()
                settings.urlbase = edwUrlBase.text.toString()
                settings.impresora = edwImpresora.text.toString()
                settings.pageSize = selectedSize
                settings.typePrint = typePrint
                val intent = Intent().apply {
                    putExtra(SETTINGS_KEY, settings)
                }
                setResult(Activity.RESULT_OK, intent)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Acción cuando no se selecciona nada
            }
        }
        spinnerTypePrint.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = typePrints[position]
                typePrint = selectedType
                // Aquí puedes hacer algo con la selección
                val settings = SettingsEntity()
                settings.urlbase = edwUrlBase.text.toString()
                settings.impresora = edwImpresora.text.toString()
                settings.pageSize = pageSize
                settings.typePrint = selectedType
                val intent = Intent().apply {
                    putExtra(SETTINGS_KEY, settings)
                }
                setResult(Activity.RESULT_OK, intent)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Acción cuando no se selecciona nada
            }
        }
        val settingsEntity: SettingsEntity = intent.getParcelableExtra(SettingsActivity.SETTINGS_KEY)
        println("Settings to open: ${Gson().toJson(settingsEntity)}") // Asegúrate de importar Gson
        setData(settingsEntity)
    }

    private fun saveChanges() {
        val settings = SettingsEntity()
        settings.urlbase = edwUrlBase.text.toString()
        settings.impresora = edwImpresora.text.toString()
        settings.pageSize = pageSize
        settings.isLog = if (chxActiveLog.isChecked) 1 else 0
        settings.typePrint = typePrint
        //settings.logoUrl = edwImageUrl.text.toString()
        println("Settings to save: ${Gson().toJson(settings)}") // Asegúrate de importar Gson
        val intent = Intent().apply {
            putExtra(SETTINGS_KEY, settings)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun setData(settingsEntity: SettingsEntity) {
        edwUrlBase.text = Editable.Factory.getInstance().newEditable(settingsEntity.urlbase)
        edwImpresora.text = Editable.Factory.getInstance().newEditable(settingsEntity.impresora)
        setSpinnerValue(spnSizeImpresora, settingsEntity.pageSize)
        setSpinnerValueTypePrint(spnTypeImpresora, settingsEntity.typePrint)
        //edwImageUrl.text = Editable.Factory.getInstance().newEditable(settingsEntity.logoUrl)
    }
    private fun setDataTypePrint(settingsEntity: SettingsEntity) {
        edwUrlBase.text = Editable.Factory.getInstance().newEditable(settingsEntity.urlbase)
        edwImpresora.text = Editable.Factory.getInstance().newEditable(settingsEntity.impresora)
        setSpinnerValue(spnSizeImpresora, settingsEntity.pageSize)
        setSpinnerValueTypePrint(spnTypeImpresora, settingsEntity.typePrint)
        //edwImageUrl.text = Editable.Factory.getInstance().newEditable(settingsEntity.logoUrl)
    }
    private fun setSpinnerValue(spinner: Spinner, value: String) {
        val adapter = spinner.adapter
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i) == value) {
                spinner.setSelection(i)
                break
            }
        }
    }
    private fun setSpinnerValueTypePrint(adapterTypePrint: Spinner, value: String) {
        val adapter = adapterTypePrint.adapter
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i) == value) {
                adapterTypePrint.setSelection(i)
                break
            }
        }
    }
}
