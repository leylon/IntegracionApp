package com.pedidos.android.persistence.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.design.widget.Snackbar
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.pedidos.android.persistence.model.LoginResponse
import com.pedidos.android.persistence.ui.login.LoginActivity
import com.google.gson.Gson
import com.pedidos.android.persistence.BuildConfig
import com.pedidos.android.persistence.R
import com.pedidos.android.persistence.api.CoolboxApi
import com.pedidos.android.persistence.db.entity.SettingsEntity
import com.pedidos.android.persistence.model.Settings
import com.pedidos.android.persistence.ui.cancel.CancelActivity
import com.pedidos.android.persistence.ui.menu.MenuActivity
import com.pedidos.android.persistence.utils.BluetoothConnector
import com.pedidos.android.persistence.utils.Extensions
import kotlinx.android.synthetic.main.sales_activity.*
import kotlinx.android.synthetic.main.search_imei_dialog.view.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.Charset
import kotlin.math.min


@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {
    private var view: View? = null
    fun checkSession() {
        val sessionActive = getSession()
        //no quitar validacion de null
        if (sessionActive.usuario == null || sessionActive.usuario == "") {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    fun getSession(): LoginResponse {
        val preferences = getSharedPreferences(LoginActivity.NAMESPACE, Context.MODE_PRIVATE)
        return Gson().fromJson(preferences.getString(LoginActivity.SESSION_USER_NAME, "{}"), LoginResponse::class.java)
    }
    fun getActiveLog(): Int {
        val preferences = getSharedPreferences(MenuActivity.NAMESPACE, Context.MODE_PRIVATE)
        val settings = Gson().fromJson(preferences.getString(MenuActivity.SETTINGS, "{}"), SettingsEntity::class.java)
        return settings.isLog
    }

    fun cleanSession() {
        val preferences = getSharedPreferences(LoginActivity.NAMESPACE, Context.MODE_PRIVATE)
        preferences.edit().putString(
                LoginActivity.SESSION_USER_NAME, "{}").apply()
    }

    fun saveSetting(settings: Settings) {
        val preferences = getSharedPreferences(MenuActivity.NAMESPACE, Context.MODE_PRIVATE)

        preferences.edit().putString(
                MenuActivity.SETTINGS,
                Gson().toJson(settings)
        ).apply()
    }

    fun getSettings(): Settings {
        val preferences = getSharedPreferences(MenuActivity.NAMESPACE, Context.MODE_PRIVATE)
        val settings = Gson().fromJson(preferences.getString(MenuActivity.SETTINGS, "{}"), SettingsEntity::class.java)
        if (settings.urlbase == "") {
            val defaultApiUrl = if (BuildConfig.DEBUG) BasicApp.DEFAULT_BASE_URL_DEBUG else BasicApp.DEFAULT_BASE_URL
            settings.urlbase = defaultApiUrl
        }

        return settings
    }

    fun getRepository(): CoolboxApi {
        return CoolboxApi.create(getSettings().urlbase)
    }

    fun printOnSnackBar(content: String) {
        val view = (findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup
        Snackbar.make(view, content, Snackbar.LENGTH_INDEFINITE)
                .setDuration(2000)
                .setAction("Action", null).show()
    }
    fun printOnDialogMessaging(content: String) {
        /*val view = (findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup
        Snackbar.make(view, content, Snackbar.LENGTH_INDEFINITE)
            .setDuration(2000)
            .setAction("Action", null).show()*/
        AlertDialog.Builder(this, R.style.AppTheme_DIALOG)
            .setTitle(R.string.app_name)
            .setMessage(content)
            .setPositiveButton(R.string.aceptar) { d, _ -> d.dismiss() }
            .setCancelable(false)
            .create().show()
    }
    fun printOnSnackBarTop(content: String) {
        val view = (findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0) as ViewGroup
        Snackbar.make(view, content, Snackbar.LENGTH_LONG)
            .setDuration(2000)
            .setAction("Action", null).show()
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params

    }

    //todo: add optional lambda, replace uses
    fun confirmMessage(content: String) {
        AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(content)
                .setPositiveButton(R.string.aceptar) { d, _ -> d.dismiss() }
                .setCancelable(false)
                .create().show()

    }

    fun messageLog(message: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_message_log, null, false)
        view?.textTitle?.setText(message)
        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(true)
            .show()
        view?.tvwAccept?.setOnClickListener {
            dialog.dismiss()
        }

    }
    private fun setupPrinter(): BluetoothConnector.BluetoothSocketWrapper? {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val pairedDevices = bluetoothAdapter.bondedDevices

        if (pairedDevices == null || pairedDevices.size == 0) {
            printOnSnackBar(getString(R.string.no_devices_paired))
            return null
        }

        val settings = getSettings()
        if (settings.impresora.isEmpty()) {
            printOnSnackBar(getString(R.string.printer_not_configured))
        }

        val device = pairedDevices.first { it ->
            it.name == settings.impresora
        }

        if (device == null) {
            printOnSnackBar(getString(R.string.printer_not_found))
        }

        return BluetoothConnector(device, false, bluetoothAdapter, null).connect()
    }

    protected fun performPrinting(bytes: ByteArray): Boolean {
        try {
            val blueToothWrapper = this.setupPrinter()
            if (blueToothWrapper != null) {
               // val setMulti = byteArrayOf(0x1C.toByte(), 0x26.toByte())
                //val setUtf8  = getCodePageCommandSunmi(16)
                // --- Comandos para la impresora ---
                val initPrinter = byteArrayOf(0x1B, 0x40)
                val selectCodePage = byteArrayOf(0x1B, 0x74, 0x13) // PC858 (Euro)
                // NUEVO: Comando para establecer el interlineado por defecto (ESC 2)
                //val setDefaultLineSpacing = byteArrayOf(0x1B, 0x32)
                val setCompactLineSpacing = byteArrayOf(0x1B, 0x33, 15)
                // Codificar el texto completo una sola vez.
                val textData = String(bytes, Charset.forName("IBM850")).toByteArray()
                // --- Envío de datos a la impresora ---
                // 1. Enviar comandos de inicialización y configuración.
                blueToothWrapper.outputStream.write(initPrinter)
                blueToothWrapper.outputStream.write(selectCodePage)
                blueToothWrapper.outputStream.write(setCompactLineSpacing) // Aplicamos el interlineado estándar

                // 2. Enviar el texto en trozos (chunks) para evitar desbordamiento del búfer.
                val chunkSize = 512 // Tamaño del trozo en bytes. Puedes ajustar este valor.
                var offset = 0
                while (offset < textData.size) {
                    val size = min(chunkSize, textData.size - offset)
                    blueToothWrapper.outputStream.write(textData, offset, size)
                    // Pequeña pausa para que la impresora procese el trozo.
                    Thread.sleep(50)
                    offset += size
                }

                blueToothWrapper.outputStream.write(textData)
                Thread.sleep(2000)
                blueToothWrapper.outputStream.close()
                blueToothWrapper.close()
                return true
            } else {
                printOnSnackBar(getString(R.string.printer_error))
            }
        } catch (ex: Exception) {
            Log.e(CancelActivity.TAG, ex.message)
          //  printOnSnackBar("Error seleccionando la impresora: " + ex.message)
        }

        return false
    }

    protected fun performPrinting(documentoPrint: String): Boolean {
        if (documentoPrint == "") {
            Log.i(CancelActivity.TAG, "no existe valor en el documento")
            printOnSnackBar(getString(R.string.payment_no_receipt))
            return false
        }
        try {
            val blueToothWrapper = this.setupPrinter()
            if (blueToothWrapper != null) {
                println("Ley documentoPrint: " + documentoPrint)
               /* val initPrinter = byteArrayOf(0x1B, 0x40)
                blueToothWrapper.outputStream.write(initPrinter)
                val selectCodePage = byteArrayOf(0x1B, 0x74, 0x13) // 0x13 es 19 en hexadecimal
                blueToothWrapper.outputStream.write(selectCodePage)
               //val setMulti = byteArrayOf(0x1C.toByte(), 0x26.toByte())
                //val setUtf8  = getCodePageCommand(1)
                //val setSmallFont = byteArrayOf(0x1B.toByte(), 0x21.toByte(), 0x00.toByte())
                //blueToothWrapper.outputStream.write(setMulti)
                // blueToothWrapper.outputStream.write(setUtf8)
                /*blueToothWrapper.outputStream.write(setSmallFont)
                Thread.sleep(1500)*/
                blueToothWrapper.outputStream.write(documentoPrint.toByteArray(Charset.forName("IBM850")))
                Thread.sleep(1500)
                blueToothWrapper.outputStream.close()
                blueToothWrapper.inputStream.close()
                blueToothWrapper.close()*/
                return printSpanishText(blueToothWrapper.underlyingSocket, documentoPrint)
            } else {
                printOnSnackBar(getString(R.string.printer_error))
            }
        } catch (ex: Exception) {
            Log.e(CancelActivity.TAG,"ley: "+ ex.message)
           // printOnSnackBar(getString(R.string.printer_error) + ": " + ex.message)
        }

        return false
        //saveAndShareFile(Base64.decode(documentoPrint, Base64.DEFAULT), numeroDocumento)
    }
    /**
     * Envía datos de texto a una impresora Bluetooth, manejando caracteres especiales en español.
     *
     * @param socket El BluetoothSocket conectado a la impresora.
     * @param textToPrint El texto que se desea imprimir.
     * @return true si la impresión se envió correctamente, false en caso de error.
     */
    fun printSpanishText(socket: BluetoothSocket?, textToPrint: String): Boolean {
        if (socket == null) {
            println("Error: El socket Bluetooth es nulo.")
            return false
        }

        var outputStream: OutputStream? = null
        try {
            outputStream = socket.outputStream

            // --- Comandos para la impresora ---
            val initPrinter = byteArrayOf(0x1B, 0x40)
            val selectCodePage = byteArrayOf(0x1B, 0x74, 0x13) // PC858 (Euro)
            // NUEVO: Comando para establecer el interlineado por defecto (ESC 2)
            //val setDefaultLineSpacing = byteArrayOf(0x1B, 0x32)
            val setCompactLineSpacing = byteArrayOf(0x1B, 0x33, 15)
            // Codificar el texto completo una sola vez.
            val textData = textToPrint.toByteArray(Charset.forName("IBM850"))

            // --- Envío de datos a la impresora ---

            // 1. Enviar comandos de inicialización y configuración.
            outputStream.write(initPrinter)
            outputStream.write(selectCodePage)
            outputStream.write(setCompactLineSpacing) // Aplicamos el interlineado estándar

            // 2. Enviar el texto en trozos (chunks) para evitar desbordamiento del búfer.
            val chunkSize = 512 // Tamaño del trozo en bytes. Puedes ajustar este valor.
            var offset = 0
            while (offset < textData.size) {
                val size = min(chunkSize, textData.size - offset)
                outputStream.write(textData, offset, size)
                // Pequeña pausa para que la impresora procese el trozo.
                Thread.sleep(50)
                offset += size
            }

            // 3. Agregar saltos de línea al final y asegurar que todo se envíe.
            outputStream.write(byteArrayOf(0x0A, 0x0A, 0x0A, 0x0A))
            outputStream.flush()

            println("Datos enviados a la impresora correctamente.")

            // 4. Pausa final larga antes de cerrar para asegurar que la impresión se complete.
            Thread.sleep(2000) // Aumentamos un poco el tiempo final por si acaso.

            return true

        } catch (e: Exception) { // Capturamos Exception para incluir InterruptedException
            println("Error durante la impresión: ${e.message}")
            e.printStackTrace()
            return false
        } finally {
            // 5. Cerrar la conexión.
            try {
                outputStream?.close()
                socket.close()
                println("Socket de impresora cerrado.")
            } catch (e: IOException) {
                println("Error al cerrar el socket de la impresora: ${e.message}")
            }
        }
    }


    protected fun performPrintingQr(qrPrint: String): Boolean {
        if (qrPrint == "") {
            Log.i(CancelActivity.TAG, "no existe valor en el documento")
            printOnSnackBar(getString(R.string.payment_no_receipt))
            return false
        }
        try {

            val blueToothWrapper = this.setupPrinter()
            if (blueToothWrapper != null) {
                val qrByte = Base64.decode(qrPrint,Base64.DEFAULT)
                val qrBitmap = BitmapFactory.decodeByteArray(qrByte,0, qrByte.size)
                val documentPrint = Extensions().decodeBitmap(qrBitmap)

                blueToothWrapper.outputStream.write(byteArrayOf(0x1b, 'a'.toByte(), 0x01))
                blueToothWrapper.outputStream.write(documentPrint)
                Thread.sleep(1500)
                blueToothWrapper.outputStream.close()
                blueToothWrapper.inputStream.close()
                blueToothWrapper.close()
                return true
            } else {
                printOnSnackBar(getString(R.string.printer_error))
            }
        } catch (ex: Exception) {
            Log.e(CancelActivity.TAG, "ley : "+ex.message)

            //printOnSnackBar(getString(R.string.printer_error) + ": " + ex.message)
        }

        return false
        //saveAndShareFile(Base64.decode(documentoPrint, Base64.DEFAULT), numeroDocumento)
    }

    protected fun saveAndShareFile(bytes: ByteArray, fileName: String) {
        //delete all files
        val file = File.createTempFile(fileName, ".pdf", cacheDir)
        val fos = FileOutputStream(file, false)
        fos.write(bytes)
        fos.flush()
        fos.close()

        //shareFile(file)
        externalViewFile(file)
    }
    fun getCodePageCommand(page: Int): ByteArray {
        return byteArrayOf(0x1B, 0x74, page.toByte()) // ESC t <n>
    }
    fun getCodePageCommandSunmi(page: Int): ByteArray {
        return byteArrayOf(0x1C.toByte(), 0x43.toByte(), 0xFF.toByte()) // ESC t <n>
    }
    private fun shareFile(file: File) {
        val uri = FileProvider.getUriForFile(this, this.packageName + ".provider", file)
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
    }

    private fun externalViewFile(file: File) {
        val uri = FileProvider.getUriForFile(this, this.packageName + ".provider", file)
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            trimCache(this)
        } catch (e: Exception) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

    }

    private fun trimCache(context: Context) {
        try {
            val dir = context.cacheDir
            if (dir != null && dir.isDirectory) {
                deleteDir(dir)
            }
        } catch (e: Exception) {
            // TODO: handle exception
        }
    }

    private fun deleteDir(dir: File): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()

            for (child in children) {
                val success = deleteDir(File(dir, child))
                if (!success) {
                    return false
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete()
    }

}