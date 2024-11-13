package com.pedidos.android.persistence.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.databinding.ClientPopupFragmentBinding
import com.pedidos.android.persistence.db.entity.ClientEntity
import com.pedidos.android.persistence.model.Client
import com.pedidos.android.persistence.model.LoginResponse
import com.pedidos.android.persistence.model.TipoDocumento
import com.pedidos.android.persistence.viewmodel.ClientViewModel
import kotlinx.android.synthetic.main.client_popup_fragment.*
import java.util.LinkedHashMap


class ClientPopUpFragment : DialogFragment() {

    private lateinit var loginInfo: LoginResponse
    private lateinit var onSelectClient: (client: Client) -> Unit
    private lateinit var mbinding: ClientPopupFragmentBinding
    private lateinit var listTipoDocumento: LinkedHashMap<Int, String>
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mbinding = DataBindingUtil.inflate(inflater, R.layout.client_popup_fragment, container, false)

        return mbinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val factory = ClientViewModel.Companion.Factory(activity!!.application,
                arguments!!.getString(KEY_URLBase))

        val clientViewModel = ViewModelProviders.of(this, factory)
                .get(ClientViewModel::class.java)

        mbinding.viewModel = clientViewModel
        mbinding.callback = object : ClientClickCallback {
            override fun onRegisterClick() {
                val clientEntity = collectClientInformation()
                clientViewModel.registerClient(clientEntity)
            }
        }

        imgScan.setOnClickListener { scanProduct() }
        imgSearch.setOnClickListener { findClientByCode() }
        btnReniecSunat.setOnClickListener { findInReniecSunat() }

        subscribeToModel(clientViewModel)

        //spinner
        val spinner = tipodocumentoSpinner as Spinner
        spinner.onItemSelectedListener = onSpinnerSelectItem
        spinner.adapter = arrayAdapter()

        if (arguments!!.getString(KEY_CLIENT_ID).trim() != "") {
            clientViewModel.findClient(arguments!!.getString(KEY_CLIENT_ID), arguments!!.getInt(KEY_CLIENT_DOCUMENT_TYPE))
        }

        //resize popup to full
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT

        dialog.window.attributes = lp
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            val result = IntentIntegrator.parseActivityResult(IntentIntegrator.REQUEST_CODE, resultCode, data)
            if (result != null) {
                when (requestCode) {
                    115 -> {
                        if (result.contents == null) {
                            AlertDialog.Builder(context!!)
                                    .setTitle(R.string.app_name)
                                    .setMessage("Lectura cancelada")
                                    .setPositiveButton(R.string.aceptar) { d, _ -> d.dismiss() }
                                    .setCancelable(false)
                                    .create().show()
                        } else {
                            codigo.setText(result.contents)
                            mbinding.viewModel!!.findClient(result.contents, listTipoDocumento.keys.elementAt(tipodocumentoSpinner.selectedItemPosition))
                        }
                    }
                    else -> {
                        super.onActivityResult(requestCode, resultCode, data)
                    }
                }
            }
        }
    }

    private fun findInReniecSunat() {
        mbinding.viewModel?.findClientInRenienSunat(codigo.text.toString(), listTipoDocumento.keys.elementAt(tipodocumentoSpinner.selectedItemPosition))
    }

    private fun findClientByCode() {
        mbinding.viewModel?.findClient(codigo.text.toString(), listTipoDocumento.keys.elementAt(tipodocumentoSpinner.selectedItemPosition))
    }

    private fun scanProduct() {
        val integrator = FragmentIntentIntegrator(this)

        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.setPrompt("Escanear Documento")
        integrator.setOrientationLocked(false)
        integrator.setBeepEnabled(true)
        integrator.setBarcodeImageEnabled(true)
        integrator.setRequestCode(115)
        integrator.initiateScan()
    }

    private fun collectClientInformation(): ClientEntity {
        val newEntity = ClientEntity()
       // newEntity.identityDocumentType = TipoDocumento.getValByPosition(tipodocumentoSpinner.selectedItemPosition)
        newEntity.identityDocumentType = listTipoDocumento.keys.elementAt(tipodocumentoSpinner.selectedItemPosition)
        newEntity.documentNumber = codigo.text.toString()
        newEntity.fullName = nombres.text.toString()
        newEntity.address = direccion.text.toString()
        newEntity.email = correo.text.toString()
        newEntity.userName = loginInfo.usuario
        newEntity.storeName = loginInfo.tienda
        newEntity.phone = phone.text.toString()
        return newEntity
    }


    private fun subscribeToModel(model: ClientViewModel) {
        model.getObservableClient().observe(this, Observer {
            model.client.set(it)
            //tipodocumentoSpinner.setSelection(TipoDocumento.getPositionByVal(it!!.identityDocumentType))
            tipodocumentoSpinner.setSelection(listTipoDocumento.keys.toList().indexOf(it!!.identityDocumentType))
        })
        model.message.observe(this, Observer {
            Toast.makeText(this.context, it, Toast.LENGTH_LONG).show()
        })

        model.saved.observe(this, Observer {
            onSelectClient(model.getObservableClient().value!!)
            dismiss()
        })
        model.findResultSuccess.observe(this, Observer {
            Log.i(TAG, "findResultSuccess: " + it.toString())
            nombres.isEnabled = it!!
            nombres.isFocusable = it
            nombres.isFocusableInTouchMode = it

            direccion.isEnabled = it
            direccion.isFocusable = it
            direccion.isFocusableInTouchMode = it

            correo.isEnabled = it
            correo.isFocusable = it
            correo.isFocusableInTouchMode = it

            phone.isEnabled = it
            phone.isFocusable = it
            phone.isFocusableInTouchMode = it
        })
    }

    private val onSpinnerSelectItem = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(p0: AdapterView<*>?) {
            Log.i(TAG, "tipo de documento no seleccionado")
        }

        override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, p3: Long) {
            val item = adapterView!!.getItemAtPosition(position)
            //val tipoDocumento = TipoDocumento.getValByPosition(position)
            val tipoDocumento = listTipoDocumento.keys.elementAt(position)

            Log.i(TAG, "tipoDocumento: $tipoDocumento")
            Log.i(TAG, "item seleccionado: $item")
            if(tipoDocumento == 0){
                mbinding.viewModel!!.findResultSuccess.postValue(true)
            }

            mbinding.viewModel!!.cleanAndSetTipoDocumento(tipoDocumento)
        }
    }

    private fun arrayAdapter(): ArrayAdapter<String> {
        val dataAdapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, listTipoDocumento.values.toList())
        dataAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        return dataAdapter
    }
    
    companion object {
        val TAG = ClientPopUpFragment::class.java.simpleName!!
        const val KEY_CLIENT_ID = "client_id"
        const val KEY_CLIENT_DOCUMENT_TYPE = "client_document_type"
        const val KEY_URLBase = "url_base"
        fun createFragment(clientId: String, clientDocumentType: Int, urlBase: String, loginInfo: LoginResponse, onSelectClient: (client: Client) -> Unit,mapTipoDocumento: LinkedHashMap<Int,String>): ClientPopUpFragment {
            val fragment = ClientPopUpFragment()
            fragment.onSelectClient = onSelectClient
            fragment.loginInfo = loginInfo
            println("mapTipoDocumento: $mapTipoDocumento")
            fragment.listTipoDocumento = mapTipoDocumento
            val args = Bundle()
            args.putString(KEY_CLIENT_ID, clientId)
            args.putInt(KEY_CLIENT_DOCUMENT_TYPE, clientDocumentType)
            args.putString(KEY_URLBase, urlBase)
            fragment.arguments = args
            return fragment
        }
    }
}
