package com.pedidos.android.persistence.ui.sale.fragment

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.model.guide.DataResponse
import com.pedidos.android.persistence.model.sale.VentaProductoResponse
import com.pedidos.android.persistence.ui.guide.fragment.CityPopUpFragment

import com.pedidos.android.persistence.ui.guide.fragment.OnClickListenerCustom


class QuestionPopUpFragment : DialogFragment, OnClickListenerCustom<VentaProductoResponse> {

    constructor()
    var optionQuestion: String = ""
    lateinit var boton_aceptar: Button
    lateinit var texto_dialogo: TextView
    lateinit var ventaProductoResponse: VentaProductoResponse
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_confirm, container)
        optionQuestion = arguments?.getString("OptionQuestion").toString()
        ventaProductoResponse =  arguments?.get("DataList") as VentaProductoResponse
        boton_aceptar  = view.findViewById(R.id.boton_aceptar)
        texto_dialogo = view.findViewById(R.id.texto_dialogo)

        texto_dialogo.text = ventaProductoResponse.mensaje
        boton_aceptar.setOnClickListener {
            val activity = activity as QuestionPopUpFragment.newDialoglistenerQuestion
                activity.closeDialogQuestion("SI",ventaProductoResponse)
                this.dismiss()

        }

        return view
    }

    override fun onClickDataListener(data: VentaProductoResponse) {
        val activity = activity as newDialoglistenerQuestion

        if (optionQuestion != "NO"){
            activity.closeDialogQuestion(optionQuestion,data)
            this.dismiss()
        }
    }

    interface newDialoglistenerQuestion {
        fun closeDialogQuestion(data : String, optionData : VentaProductoResponse)
    }
}