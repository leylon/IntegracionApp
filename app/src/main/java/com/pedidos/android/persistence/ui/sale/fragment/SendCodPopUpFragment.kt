package com.pedidos.android.persistence.ui.sale.fragment

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.InputType
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.model.guide.DataResponse
import com.pedidos.android.persistence.model.sale.VentaProductoResponse
import com.pedidos.android.persistence.ui.guide.fragment.CityPopUpFragment

import com.pedidos.android.persistence.ui.guide.fragment.OnClickListenerCustom


class SendCodPopUpFragment : DialogFragment, OnClickListenerCustom<String> {

    constructor()
    var optionQuestion: String = ""
    lateinit var progress_send: ProgressBar
    lateinit var imgSendMail: ImageView
    lateinit var boton_aprobar_cod: TextView
    lateinit var texto_dialogo_email: TextView
    lateinit var edit_barcod_auth: EditText
    lateinit var dataList: VentaProductoResponse
    lateinit var edit_cod_auth: EditText
    lateinit var boton_aprobar_fotocheck: TextView
    lateinit var imgCamCod: ImageView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_send_cod, container)
        dialog.setCancelable(false)
        optionQuestion = arguments?.getString("OptionQuestion").toString()
        dataList = arguments?.get("DataList") as VentaProductoResponse
        progress_send = view.findViewById(R.id.progress_send)
        edit_cod_auth = view.findViewById(R.id.edit_cod_auth)
        boton_aprobar_cod = view.findViewById(R.id.boton_aprobar_cod)
        texto_dialogo_email = view.findViewById(R.id.texto_dialogo_email)
        edit_barcod_auth = view.findViewById(R.id.edit_barcod_auth)
        boton_aprobar_fotocheck = view.findViewById(R.id.boton_aprobar_fotocheck)
        imgCamCod = view.findViewById(R.id.imgCamCod)
        imgSendMail = view.findViewById(R.id.imgSendMail)
        texto_dialogo_email.text = dataList.correo
        //edit_cod_auth.setText(dataList.codigoautorizacion)
        edit_cod_auth.setText("")
        edit_barcod_auth.setText("")
        edit_barcod_auth.isEnabled = true
        if (!dataList.bloqueafotocheck.equals("NO")){
            edit_barcod_auth.apply {
                inputType = InputType.TYPE_NULL
                requestFocus()
            }
        }

        imgSendMail.setOnClickListener {
            progress_send.visibility = View.VISIBLE
            val activity = activity as newDialoglistenerSendCod
            activity.closeDialogSendCod("SEND_MAIL",optionQuestion)
            //this.dismiss()
        }
        imgCamCod.setOnClickListener {

            val activity = activity as newDialoglistenerSendCod
            activity.closeDialogSendCod("CAM_COD",optionQuestion)
            //this.dismiss()
        }
        boton_aprobar_cod.setOnClickListener {
            if(edit_cod_auth.text.toString().isNotEmpty()){
            val activity = activity as newDialoglistenerSendCod
            activity.closeDialogSendCod("VALID_COD",edit_cod_auth.text.toString())
            //this.dismiss()
             }
        }
        edit_barcod_auth.setOnKeyListener { v, keyCode, event ->
            edit_barcod_auth.isEnabled = true
            if (event?.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                println("ley: keyyyyy")
                if (!edit_barcod_auth.text.isNullOrBlank()) {
                    val activity = activity as newDialoglistenerSendCod
                    activity.closeDialogSendCod("VALID_COD", edit_cod_auth.text.toString())
                }
                edit_barcod_auth.setText("")
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }


        boton_aprobar_fotocheck.setOnClickListener {
            if(edit_barcod_auth.text.toString().isNotEmpty()){
                val activity = activity as newDialoglistenerSendCod
                activity.closeDialogSendCod("VALID_BAR_COD",edit_barcod_auth.text.toString())
                //this.dismiss()
            }
        }
        return view
    }

    override fun onClickDataListener(objectData: String) {
        val activity = activity as newDialoglistenerSendCod

        if (objectData != "NO"){
            activity.closeDialogSendCod(objectData,optionQuestion)
            this.dismiss()
        }
    }

    interface newDialoglistenerSendCod {
        fun closeDialogSendCod(data : String, optionData : String)
    }
}