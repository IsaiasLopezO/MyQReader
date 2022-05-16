package com.example.myqreader

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Vibrator
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.net.URL

class QR : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private val permiso_camara = 1
    private var scannerView: ZXingScannerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scannerView = ZXingScannerView(this)
        setContentView(scannerView)

        if(checarPermiso()){
            // Se tiene permiso
        }else{
            solicitarPermiso()
        }

        scannerView?.setResultHandler(this)
        scannerView?.startCamera()
    }

    private fun solicitarPermiso() {
        ActivityCompat.requestPermissions(this@QR, arrayOf(Manifest.permission.CAMERA), permiso_camara)
    }

    private fun checarPermiso(): Boolean {
        return(ContextCompat.checkSelfPermission(this@QR, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    override fun handleResult(p0: Result?) {//Aqui va lo que lea del codigo QR

        val scanResult = p0?.text
        val vibratorService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibratorService.vibrate(100)

        if (scanResult!!.substring(startIndex = 0, endIndex = 4) == "http"){
            Log.d("QR_LEIDO", scanResult!!)
            Log.d("QR_LEIDO", "URL leido")

            val url = URL(scanResult)
            val i = Intent(Intent.ACTION_VIEW)
            i.setData(Uri.parse(scanResult))
            startActivity(i)
            finish()

        }else if (scanResult!!.substring(startIndex = 0, endIndex = 5) == "SMSTO" || scanResult!!.substring(startIndex = 0, endIndex = 5) == "smsTO"){
            var index_inicio_numero = scanResult!!.indexOf("TO:")
            var index_final_numero = scanResult!!.indexOf(":", index_inicio_numero+3)
            var numero = ""
            var mensaje = ""

            for(i in index_inicio_numero+3 until index_final_numero){
                numero += scanResult[i]
            }

            for(i in index_final_numero+1 until scanResult.length){
                mensaje += scanResult[i]
            }

            Log.d("QR_LEIDO", scanResult!!)
            Log.d("QR_LEIDO", "SMS leido")
            Log.d("QR_LEIDO", numero)
            Log.d("QR_LEIDO", mensaje)

            var i = Intent(Intent.ACTION_VIEW, Uri.fromParts("sms",numero,null))
            i.putExtra("sms_body",mensaje)
            startActivity(i)
            finish()

        }else if (scanResult!!.substring(startIndex = 0, endIndex = 6) == "MATMSG" || scanResult!!.substring(startIndex = 0, endIndex = 6) == "mailto" ){
            var index_inicio_direccion = scanResult!!.indexOf("TO:")
            var index_final_direccion = scanResult!!.indexOf(";", index_inicio_direccion+3)

            var index_inicio_asunto = scanResult!!.indexOf("SUB:", index_inicio_direccion+3)
            var index_final_asunto = scanResult!!.indexOf(";", index_inicio_asunto+4)

            var index_inicio_cuerpo = scanResult!!.indexOf("BODY:",index_inicio_asunto+4)
            var index_final_cuerpo = scanResult!!.indexOf(";", index_inicio_cuerpo+5)

            var direccion_correo = ""
            var asunto = ""
            var cuerpo_correo = ""

            for(i in index_inicio_direccion+3 until index_final_direccion){
                direccion_correo += scanResult[i]
            }

            for(i in index_inicio_asunto+4 until index_final_asunto){
                asunto += scanResult[i]
            }

            for(i in index_inicio_cuerpo+5 until index_final_cuerpo){
                cuerpo_correo += scanResult[i]
            }

            var i = Intent(Intent.ACTION_SENDTO)
            var emails = arrayOf(direccion_correo)
            i.setType("*/*")
            i.setData(Uri.parse("mailto:"))
            i.putExtra(Intent.EXTRA_EMAIL, emails)
            i.putExtra(Intent.EXTRA_SUBJECT, asunto)
            i.putExtra(Intent.EXTRA_TEXT, cuerpo_correo)
            startActivity(i)
            finish()

            Log.d("QR_LEIDO", scanResult!!)
            Log.d("QR_LEIDO", "eMail leido")
            Log.d("QR_LEIDO", "Direccion: $direccion_correo")
            Log.d("QR_LEIDO", "Asunto: $asunto")
            Log.d("QR_LEIDO", "Cuerpo: $cuerpo_correo")

        }else if (scanResult!!.substring(startIndex = 0, endIndex = 11) == "BEGIN:VCARD"){
            var index_inicio_VERSION = scanResult.indexOf("VERSION:")
            var index_inicio_N = scanResult.indexOf("N:", index_inicio_VERSION+8)
            var index_puntoycoma = scanResult.indexOf(";", index_inicio_N+2)
            var index_inicio_FN = scanResult.indexOf("FN:", index_inicio_N+2)
            var index_inicio_ORG = scanResult.indexOf("ORG:",index_inicio_FN+3)
            var index_inicio_TITLE = scanResult.indexOf("TITLE:", index_inicio_ORG+4)
            var index_inicio_ADR = scanResult.indexOf("ADR:", index_inicio_TITLE+6)
            var index_inicio_TEL_WORK = scanResult.indexOf("TEL;WORK;VOICE:", index_inicio_ADR+4)
            var index_inicio_TEL_CELL = scanResult.indexOf("TEL;CELL:", index_inicio_TEL_WORK+15)
            var index_inicio_TEL_FAX = scanResult.indexOf("TEL;FAX:", index_inicio_TEL_CELL+9)
            var index_inicio_EMAIL_WORK_INTERNET = scanResult.indexOf("EMAIL;WORK;INTERNET:", index_inicio_TEL_FAX+8)
            var index_inicio_URL = scanResult.indexOf("URL:", index_inicio_EMAIL_WORK_INTERNET+20)
            var index_fin_vCard = scanResult.indexOf("END:VCARD")

            var VERSION = ""
            var N = ""
            var FN = ""
            var ORG = ""
            var TITLE = ""
            var ADR = ""
            var TEL_WORK_VOICE = ""
            var TEL_CELL = ""
            var TEL_FAX = ""
            var EMAIL_WORK_INTERNET = ""
            var URL = ""

            for(i in index_inicio_VERSION+8 until index_inicio_N-1){
                VERSION += scanResult[i]
            }

            for(i in index_inicio_N+2 until index_puntoycoma){
                N += scanResult[i]
            }
            N += " "
            for(i in index_puntoycoma+1 until index_inicio_FN-1){
                N += scanResult[i]
            }

            for(i in index_inicio_FN+3 until index_inicio_ORG-1){
                FN += scanResult[i]
            }

            for(i in index_inicio_ORG+4 until index_inicio_TITLE-1){
                ORG += scanResult[i]
            }

            for(i in index_inicio_TITLE+6 until index_inicio_ADR-1){
                TITLE += scanResult[i]
            }

            for(i in index_inicio_ADR+4 until index_inicio_TEL_WORK-1){
                ADR += scanResult[i]
            }

            for(i in index_inicio_TEL_WORK+15 until index_inicio_TEL_CELL-1){
                TEL_WORK_VOICE += scanResult[i]
            }

            for(i in index_inicio_TEL_CELL+9 until index_inicio_TEL_FAX-1){
                TEL_CELL += scanResult[i]
            }

            for(i in index_inicio_TEL_FAX+8 until index_inicio_EMAIL_WORK_INTERNET-1){
                TEL_FAX += scanResult[i]
            }

            for(i in index_inicio_EMAIL_WORK_INTERNET+20 until index_inicio_URL-1){
                EMAIL_WORK_INTERNET += scanResult[i]
            }

            for(i in index_inicio_URL+4 until index_fin_vCard-1){
                URL += scanResult[i]
            }

            Log.d("QR_LEIDO", scanResult!!)
            Log.d("QR_LEIDO", "VCard leida")
            Log.d("QR_LEIDO", "Version: $VERSION")
            Log.d("QR_LEIDO", "N: $N")
            Log.d("QR_LEIDO", "FN: $FN")
            Log.d("QR_LEIDO", "ORG: $ORG")
            Log.d("QR_LEIDO", "TITLE: $TITLE")
            Log.d("QR_LEIDO", "ADR: $ADR")
            Log.d("QR_LEIDO", "TEL_WORK_VOICE: $TEL_WORK_VOICE")
            Log.d("QR_LEIDO", "TEL_CELL: $TEL_CELL")
            Log.d("QR_LEIDO", "TEL_FAX: $TEL_FAX")
            Log.d("QR_LEIDO", "EMAIL_WORK_INTERNET: $EMAIL_WORK_INTERNET")
            Log.d("QR_LEIDO", "URL: $URL")

            ORG = "FI UNAM"
            TITLE = "Maestro"
            TEL_WORK_VOICE = "12345"
            TEL_FAX = "98745"
            URL = "www.isaias.com"

            val i = Intent(Intent.ACTION_INSERT).apply {
                type = ContactsContract.Contacts.CONTENT_TYPE
                putExtra(ContactsContract.Intents.Insert.NAME, N)
                putExtra(ContactsContract.Intents.Insert.PHONETIC_NAME,FN)
                putExtra(ContactsContract.Intents.Insert.COMPANY, ORG)
                putExtra(ContactsContract.Intents.Insert.JOB_TITLE, TITLE)
                putExtra(ContactsContract.Intents.Insert.POSTAL_TYPE, ADR)
                putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE, TEL_WORK_VOICE)
                putExtra(ContactsContract.Intents.Insert.PHONE, TEL_CELL)
                putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, TEL_FAX)
                putExtra(ContactsContract.Intents.Insert.EMAIL, EMAIL_WORK_INTERNET)
                putExtra(ContactsContract.Intents.Insert.DATA, URL)
            }
            startActivity(i)
            finish()

        }else{
            AlertDialog.Builder(this@QR)
                .setTitle(getString(R.string.Error))
                .setMessage(getString(R.string.Codigo_no_reconocido))
                .setPositiveButton(getString(R.string.Aceptar), DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                    finish()
                })
                .create()
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        if(checarPermiso()){
            if(scannerView == null){
                scannerView = ZXingScannerView(this)
                setContentView(scannerView)
            }
            scannerView?.setResultHandler(this)
            scannerView?.startCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scannerView?.stopCamera()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            permiso_camara -> {
                if(grantResults.isNotEmpty()){
                    if(grantResults[0]!= PackageManager.PERMISSION_GRANTED){
                        if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                            AlertDialog.Builder(this@QR)
                                .setTitle(getString(R.string.permiso_necesario))
                                .setMessage(getString(R.string.Se_necesita_camara))
                                .setPositiveButton(getString(R.string.Aceptar), DialogInterface.OnClickListener { dialogInterface, i ->
                                    requestPermissions(arrayOf(Manifest.permission.CAMERA), permiso_camara)
                                })
                                .setNegativeButton(getString(R.string.Denegar), DialogInterface.OnClickListener { dialogInterface, i ->
                                  dialogInterface.dismiss()
                                  finish()
                                })
                                .create()
                                .show()
                        }else{
                            Toast.makeText(this@QR, getString(R.string.Permiso_camara_no_se_ha_concedido), Toast.LENGTH_LONG).show()
                            finish()
                        }
                    }
                }
            }
        }
    }
}