package com.clase2503.aplicaciontres

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private val PERMISO_CONTACTOS = 1
    private val PERMISO_MENSAJES = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(!checarPermisoSMS()){
            solicitarPermisoSMS()
        }

        if(!checarPermisoTel()){
            solicitarPermisoTel()
        }
    }

    fun click(view: View) {
        startActivity(Intent(this, QR::class.java))

    }



    private fun solicitarPermisoTel() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_NUMBERS),PERMISO_CONTACTOS)
    }

    private fun checarPermisoTel(): Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED)
    }

    private fun solicitarPermisoSMS() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS),PERMISO_MENSAJES)
    }

    private fun checarPermisoSMS(): Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)
    }

}