package com.example.esp32project

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun HomeScreen(
    bluetoothManager: Esp32BluetoothManager,
    hasPermission: Boolean,
    onPermissionRequest: () -> Unit
) {

    var isConnected by remember {
        mutableStateOf(false)
    }

    var statusText by remember {
        mutableStateOf("Ready to Connect")
    }


    var masterLight by remember {
        mutableStateOf(false)
    }

    var greenLight by remember {
        mutableStateOf(false)
    }

    var yellowLight by remember {
        mutableStateOf(false)
    }


    val context = LocalContext.current



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(
            modifier = Modifier.height(50.dp)
        )

        Text(
            text = "💡 Smart Light",
            fontSize = 32.sp
        )


        Spacer(
            modifier = Modifier.height(16.dp)
        )



        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {

            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null
                )


                Spacer(
                    modifier = Modifier.width(12.dp)
                )


                Text(
                    text = statusText,
                    fontSize = 17.sp
                )

            }
        }



        Spacer(
            modifier = Modifier.height(20.dp)
        )



        if (!hasPermission) {

            Button(
                onClick = onPermissionRequest
            ) {
                Text("Allow Bluetooth")
            }

        } else {


            Button(
                onClick = {

                    statusText = "Connecting..."

                    bluetoothManager.connect { success, msg ->

                        isConnected = success
                        statusText = msg

                        if (!success) {

                            Toast.makeText(
                                context,
                                msg,
                                Toast.LENGTH_LONG
                            ).show()

                        }

                    }

                },
                enabled = !isConnected
            ) {

                Text(
                    if (isConnected)
                        "Connected"
                    else
                        "Connect ESP32"
                )
            }

        }



        Spacer(
            modifier = Modifier.height(25.dp)
        )



        LightCard(
            title = "All Lights",
            checked = masterLight,
            enabled = isConnected,
            onChanged = {

                masterLight = it

                if (it) {

                    bluetoothManager.sendCommand("ALL_ON")

                    greenLight = true
                    yellowLight = true

                } else {

                    bluetoothManager.sendCommand("ALL_OFF")

                    greenLight = false
                    yellowLight = false
                }

            }
        )



        Spacer(
            modifier = Modifier.height(12.dp)
        )



        LightCard(
            title = "🟢 Green Light",
            checked = greenLight,
            enabled = isConnected,
            onChanged = {

                greenLight = it

                bluetoothManager.sendCommand(
                    if (it)
                        "GREEN_ON"
                    else
                        "GREEN_OFF"
                )

            }
        )



        Spacer(
            modifier = Modifier.height(12.dp)
        )



        LightCard(
            title = "🟡 Yellow Light",
            checked = yellowLight,
            enabled = isConnected,
            onChanged = {

                yellowLight = it

                bluetoothManager.sendCommand(
                    if (it)
                        "YELLOW_ON"
                    else
                        "YELLOW_OFF"
                )

            }
        )



        Spacer(
            modifier = Modifier.height(25.dp)
        )



        Text(
            text = "Modes",
            fontSize = 22.sp
        )


        Spacer(
            modifier = Modifier.height(10.dp)
        )



        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {


            ModeButton(
                text = "☀️ Day",
                enabled = isConnected
            ) {

                bluetoothManager.sendCommand("DAY")

                greenLight = true
                yellowLight = false
            }



            ModeButton(
                text = "🌙 Night",
                enabled = isConnected
            ) {

                bluetoothManager.sendCommand("NIGHT")

                greenLight = false
                yellowLight = true
            }

        }



        Spacer(
            modifier = Modifier.height(10.dp)
        )



        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {


            ModeButton(
                text = "🎉 Party",
                enabled = isConnected
            ) {

                bluetoothManager.sendCommand("PARTY")

            }



            ModeButton(
                text = "⛔ Stop",
                enabled = isConnected
            ) {

                bluetoothManager.sendCommand("STOP")

                greenLight = false
                yellowLight = false
                masterLight = false
            }

        }

    }

}





@Composable
fun LightCard(
    title: String,
    checked: Boolean,
    enabled: Boolean,
    onChanged: (Boolean) -> Unit
) {


    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {


        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {


            Text(
                text = title,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f)
            )


            Switch(
                checked = checked,
                onCheckedChange = onChanged,
                enabled = enabled
            )

        }

    }

}





@Composable
fun ModeButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(15.dp)
    ) {

        Text(
            text = text
        )

    }

}