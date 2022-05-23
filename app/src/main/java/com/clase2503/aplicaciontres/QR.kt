package com.clase2503.aplicaciontres

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.Result
import ezvcard.Ezvcard
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.lang.Exception
import java.net.MalformedURLException
import java.net.URL


class QR : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private val PERMISO_CAMARA = 1
    private val PERMISO_CONTACTOS = 1
    private val PERMISO_MENSAJES = 1
    private var scannerView: ZXingScannerView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scannerView = ZXingScannerView(this)
        setContentView(scannerView)



        if(checarPermiso()){

        }else{
            solicitarPermiso()
        }


        scannerView?.setResultHandler(this)
        scannerView?.startCamera()
    }

    private fun solicitarPermiso() {
        ActivityCompat.requestPermissions(this@QR, arrayOf(Manifest.permission.CAMERA),PERMISO_CAMARA)
    }

    private fun checarPermiso(): Boolean {
        return (ContextCompat.checkSelfPermission(this@QR, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    private fun solicitarPermisoTel() {
        ActivityCompat.requestPermissions(this@QR, arrayOf(Manifest.permission.READ_PHONE_NUMBERS),PERMISO_CAMARA)
    }

    private fun checarPermisoTel(): Boolean {
        return (ContextCompat.checkSelfPermission(this@QR, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED)
    }

    private fun solicitarPermisoSMS() {
        ActivityCompat.requestPermissions(this@QR, arrayOf(Manifest.permission.SEND_SMS),PERMISO_CAMARA)
    }

    private fun checarPermisoSMS(): Boolean {
        return (ContextCompat.checkSelfPermission(this@QR, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)
    }

    override fun handleResult(p0: Result?) {
        // Código QR
        val scanResult = p0?.text
        Log.d("QR_LEIDO",scanResult!!)
        val parametros = scanResult.split(":")

        val case = scanResult.split(":")[0]
        val cases = arrayListOf<String>("MATMSG","SMSTO","BEGIN","SMTP")
        Log.d("Caso",case!!)
        //val vcard = Ezvcard.parse(scanResult).first()

        //Log.d("Name", vcard.formattedName.value)

        try{
            val url = URL(scanResult)
            val i = Intent(Intent.ACTION_VIEW)
            i.setData(Uri.parse(scanResult))
            startActivity(i)
            finish()
        }catch(e: MalformedURLException){
            if(!cases.contains(case)){
                AlertDialog.Builder(this@QR)
                    .setTitle("Error")
                    .setMessage("El codigo QR no es validor para la aplicacion")
                    .setPositiveButton("Aceptar", DialogInterface.OnClickListener { dialogInterface, i ->
                        dialogInterface.dismiss()
                        finish()
                    })
                    .create()
                    .show()
            }
        }

        when(case){
            "MATMSG" -> {
                try{
                    val correo = parametros[2].split(';')[0]
                    val subject = parametros[3].split(';')[0]
                    val texto = parametros[4].split(':')
                    val i = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(correo))
                        putExtra(Intent.EXTRA_SUBJECT, subject)
                        putExtra(Intent.EXTRA_TEXT, parametros[4].split(';')[0])
                    }
                    startActivity(i)
                    finish()
                }catch(e: Exception){
                    Log.d("Error:", e.toString())
                }
            }
            "SMTP" -> {
                try{
                    val correo = parametros[1]
                    val subject = parametros[2]
                    val texto = parametros[3]
                    val i = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(correo))
                        putExtra(Intent.EXTRA_SUBJECT, subject)
                        putExtra(Intent.EXTRA_TEXT, texto)
                    }
                    startActivity(i)
                    finish()
                }catch(e: Exception){
                    Log.d("Error:", e.toString())
                }
            }
            "SMSTO" ->{
                try{
                    val uri = Uri.parse(parametros[0].lowercase() + ":" + parametros[1])
                    val i = Intent(Intent.ACTION_SENDTO,uri)
                    i.putExtra("sms_body",parametros[2])
                    startActivity(i)
                    finish()
                }catch(e: Exception){
                    Log.d("Error:", e.toString())
                }
            }
            "BEGIN" ->{
                try{
                    val vcard = Ezvcard.parse(scanResult).first()
                    val intent_c = Intent(ContactsContract.Intents.Insert.ACTION)
                    intent_c.setType(ContactsContract.RawContacts.CONTENT_TYPE)

                    intent_c.putExtra(ContactsContract.Intents.Insert.NAME, vcard.structuredName.given + ' ' + vcard.structuredName.family)
                    intent_c.putExtra(ContactsContract.Intents.Insert.EMAIL, vcard.emails[0].value)
                    for (e in vcard.telephoneNumbers){
                        if(!e.text.equals("")) {
                            intent_c.putExtra(ContactsContract.Intents.Insert.PHONE, e.text)
                            break
                        }
                    }
                    //intent_c.putExtra(ContactsContract.Intents.Insert.PHONE, vcard.telephoneNumbers)
                    startActivity(intent_c)
                    finish()
                }catch(e: Exception){
                    Log.d("Error", e.toString())
                }
            }

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
            PERMISO_CAMARA -> {
                if(grantResults.isNotEmpty()){
                    if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                        if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                            AlertDialog.Builder(this@QR)
                                .setTitle("Permiso Requerido")
                                .setMessage("Se necesita acceso a la camara para leer QR")
                                .setPositiveButton("Aceptar", DialogInterface.OnClickListener{dialogInterface, i ->
                                    requestPermissions(arrayOf(Manifest.permission.CAMERA), PERMISO_CAMARA)
                                })
                                .setNegativeButton("Cancelar", DialogInterface.OnClickListener { dialogInterface, i ->
                                    dialogInterface.dismiss()
                                    finish()
                                })
                                .create()
                                .show()
                        }else{
                            Toast.makeText(this@QR, "El permiso de la cámara no se ha concedido", Toast.LENGTH_LONG)
                            finish()
                        }
                    }
                }
            }
        }

    }

}