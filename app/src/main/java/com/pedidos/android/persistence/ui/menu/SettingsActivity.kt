package com.pedidos.android.persistence.ui.menu

import android.app.Activity
import android.content.Intent
import android.os.Build

import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.db.entity.SettingsEntity

import kotlinx.android.synthetic.main.settings_activity.*
import java.util.*

class SettingsActivity : AppCompatActivity() {
    companion object {

        const val SETTINGS_KEY = "settings_key"
    }

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
        val settingsEntity: SettingsEntity = intent.getParcelableExtra(SettingsActivity.SETTINGS_KEY)
        setData(settingsEntity)
    }

    private fun saveChanges() {
        val settings = SettingsEntity()
        settings.urlbase = edwUrlBase.text.toString()
        settings.impresora = edwImpresora.text.toString()
        //settings.logoUrl = edwImageUrl.text.toString()

        val intent = Intent().apply {
            putExtra(SETTINGS_KEY, settings)
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun setData(settingsEntity: SettingsEntity) {
        edwUrlBase.text = Editable.Factory.getInstance().newEditable(settingsEntity.urlbase)
        edwImpresora.text = Editable.Factory.getInstance().newEditable(settingsEntity.impresora)
        //edwImageUrl.text = Editable.Factory.getInstance().newEditable(settingsEntity.logoUrl)
    }
}
