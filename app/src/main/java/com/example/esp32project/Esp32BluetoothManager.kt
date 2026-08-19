package com.example.esp32project

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

class Esp32BluetoothManager(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var connectedSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    val isConnected: Boolean get() = connectedSocket?.isConnected == true

    private val DEVICE_NAME = "ESP32_Smart_Light"
    private val UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val mainHandler = Handler(Looper.getMainLooper())

    private fun hasPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun connect(onResult: (Boolean, String) -> Unit) {
        if (bluetoothAdapter == null) {
            onResult(false, "Bluetooth is not supported on this device")
            return
        }

        if (!bluetoothAdapter!!.isEnabled) {
            onResult(false, "Bluetooth is turned off")
            return
        }

        if (!hasPermission()) {
            onResult(false, "BLUETOOTH_CONNECT permission not granted")
            return
        }

        scope.launch {
            try {
                val device = bluetoothAdapter?.bondedDevices?.firstOrNull {
                    it.name == DEVICE_NAME
                }

                if (device == null) {
                    withContext(Dispatchers.Main) {
                        onResult(false, "Device $DEVICE_NAME not found.\n Please pair it first")
                    }
                    return@launch
                }

                val socket = device.createRfcommSocketToServiceRecord(UUID_SPP)

                if (!hasPermission()) {
                    withContext(Dispatchers.Main) {
                        onResult(false, "Bluetooth permission not granted")
                    }
                    return@launch
                }

                socket.connect()

                connectedSocket = socket
                outputStream = socket.outputStream

                withContext(Dispatchers.Main) {
                    onResult(true, "Connection Successful")
                }

            } catch (e: SecurityException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onResult(false, "Permission Error: ${e.message}")
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onResult(false, "Connection failed. Please restart the device")
                }
            } catch (e: CancellationException) {
                println("Connection cancelled")
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onResult(false, "Unknown Error: ${e.message}")
                }
            }
        }
    }

    fun sendCommand(cmd: String, onError: () -> Unit = {}) {
        if (!hasPermission() || !isConnected) return

        Thread {
            try {
                outputStream?.write("$cmd\n".toByteArray())
                outputStream?.flush()

            } catch (e: Exception) {
                e.printStackTrace()
                mainHandler.post {
                    disconnect()
                    onError()
                }
            }
        }.start()
    }

    fun disconnect() {
        try {
            outputStream?.close()
            connectedSocket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        connectedSocket = null
        outputStream = null
    }
}