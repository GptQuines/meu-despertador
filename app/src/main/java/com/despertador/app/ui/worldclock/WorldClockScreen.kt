package com.despertador.app.ui.worldclock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldClockScreen() {
    val cities = remember { mutableStateListOf(
        "America/New_York" to "Nova York",
        "Europe/London" to "Londres",
        "Europe/Paris" to "Paris",
        "Asia/Tokyo" to "T\u00f3quio"
    ) }
    var showDialog by remember { mutableStateOf(false) }
    val allTimeZones = remember { TimeZone.getAvailableIDs().sorted() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rel\u00f3gio Mundial") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar cidade")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cities) { (tzId, cityName) ->
                WorldClockCard(tzId, cityName)
            }
        }
    }

    if (showDialog) {
        var selectedTz by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Adicionar cidade") },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(allTimeZones) { tz ->
                        val displayName = tz.replace("/", " / ")
                        TextButton(
                            onClick = {
                                selectedTz = tz
                                val shortName = tz.substringAfterLast("/").replace("_", " ")
                                cities.add(tz to shortName)
                                showDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(displayName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun WorldClockCard(tzId: String, cityName: String) {
    val tz = TimeZone.getTimeZone(tzId)
    val now = Date()
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    sdf.timeZone = tz
    val timeStr = sdf.format(now)
    val offsetMs = tz.getOffset(now.time)
    val offsetHours = offsetMs / 3600000
    val offsetMinutes = (Math.abs(offsetMs) % 3600000) / 60000
    val offsetStr = "UTC${if (offsetHours >= 0) "+" else ""}${offsetHours}${if (offsetMinutes > 0) ":" + String.format("%02d", offsetMinutes) else ""}"

    val sdfDate = SimpleDateFormat("dd/MM", Locale.getDefault())
    sdfDate.timeZone = tz
    val dateStr = sdfDate.format(now)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cityName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = offsetStr,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateStr,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = timeStr,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
