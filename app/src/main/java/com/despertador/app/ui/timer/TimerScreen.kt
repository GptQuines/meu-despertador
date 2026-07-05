package com.despertador.app.ui.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen() {
    var hours by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    var seconds by remember { mutableStateOf("") }
    var remainingMillis by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Temporizador") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isRunning && remainingMillis == 0L) {
                // Input mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedTextField(
                        value = hours,
                        onValueChange = { if (it.length <= 2) hours = it.filter { c -> c.isDigit() } },
                        label = { Text("H") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(80.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(":", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(top = 8.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = minutes,
                        onValueChange = { if (it.length <= 2) minutes = it.filter { c -> c.isDigit() } },
                        label = { Text("M") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(80.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(":", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(top = 8.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = seconds,
                        onValueChange = { if (it.length <= 2) seconds = it.filter { c -> c.isDigit() } },
                        label = { Text("S") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        val h = hours.toIntOrNull() ?: 0
                        val m = minutes.toIntOrNull() ?: 0
                        val s = seconds.toIntOrNull() ?: 0
                        if (h > 0 || m > 0 || s > 0) {
                            remainingMillis = (h * 3600L + m * 60L + s) * 1000L
                            isRunning = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Iniciar", fontSize = 18.sp)
                }
            } else {
                // Countdown mode
                Text(
                    text = formatTimer(remainingMillis),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = if (isFinished) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (isFinished) {
                    Text(
                        text = "Tempo esgotado!",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    if (isRunning) {
                        Button(
                            onClick = { isRunning = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("Pausar")
                        }
                    } else {
                        Button(
                            onClick = { isRunning = true },
                            enabled = remainingMillis > 0
                        ) {
                            Text("Continuar")
                        }
                    }

                    Button(
                        onClick = {
                            isRunning = false
                            isFinished = false
                            remainingMillis = 0L
                            hours = ""
                            minutes = ""
                            seconds = ""
                        }
                    ) {
                        Text("Resetar")
                    }
                }
            }
        }
    }

    // Countdown logic
    if (isRunning && remainingMillis > 0) {
        androidx.compose.runtime.LaunchedEffect(remainingMillis) {
            delay(100L)
            remainingMillis -= 100L
            if (remainingMillis <= 0L) {
                remainingMillis = 0L
                isRunning = false
                isFinished = true
            }
        }
    }
}

private fun formatTimer(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
