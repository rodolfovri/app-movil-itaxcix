package com.rodolfo.itaxcix

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar si ya tiene el permiso
        val hasCallPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        if (hasCallPermission) {
            // Ya tiene permiso, inicializar directamente
            initializeApp()
        } else {
            // Solicitar permiso antes de continuar
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    // Launcher para solicitar permiso de llamada
    private val callPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Independientemente del resultado, continuar con la app
        initializeApp()
    }

    private fun initializeApp() {
        setContent {
            App()
        }
    }
}