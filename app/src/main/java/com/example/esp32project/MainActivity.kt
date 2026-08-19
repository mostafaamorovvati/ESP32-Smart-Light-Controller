package com.example.esp32project

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.esp32project.ui.theme.ESP32ProjectTheme

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothManager: Esp32BluetoothManager

    private var hasBluetoothPermission by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasBluetoothPermission = permissions.values.all { it }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        bluetoothManager = Esp32BluetoothManager(this)

        checkAndRequestPermissions()

        setContent {
            ESP32ProjectTheme {
                HomeScreen(
                    bluetoothManager = bluetoothManager,
                    hasPermission = hasBluetoothPermission,
                    onPermissionRequest = { requestPermissions() }
                )
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val connectGranted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED

            val scanGranted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED

            hasBluetoothPermission = connectGranted && scanGranted

            if (!hasBluetoothPermission) {
                requestPermissions()
            }
        } else {
            hasBluetoothPermission = true
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                )
            )
        }
    }

    override fun onDestroy() {
        bluetoothManager.disconnect()
        super.onDestroy()
    }
}