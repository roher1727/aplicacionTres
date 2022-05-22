package com.clase2503.aplicaciontres

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.Result
import ezvcard.Ezvcard
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.net.MalformedURLException
import java.net.URL


class QR : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private val PERMISO_CAMARA = 1
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

    override fun handleResult(p0: Result?) {
        // Código QR
        val scanResult = p0?.text
        Log.d("QR_LEIDO",scanResult!!)

        //val vcard = Ezvcard.parse(scanResult).first()

        //Log.d("Name", vcard.formattedName.value)

        try{
            val url = URL(scanResult)
            val i = Intent(Intent.ACTION_VIEW)
            i.setData(Uri.parse(scanResult))
            startActivity(i)
            finish()
        }catch(e: MalformedURLException){
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