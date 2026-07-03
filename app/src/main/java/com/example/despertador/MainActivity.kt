package com.example.despertador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

class MainActivity : ComponentActivity() {

    private lateinit var alarmScheduler: AlarmScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alarmScheduler = AlarmScheduler(this)

        setContent {
            MaterialTheme {
                AlarmScreen(alarmScheduler)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(alarmScheduler: AlarmScheduler) {
    var alarms by remember { mutableStateOf(listOf<AlarmData>()) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Despertador") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar alarme")
            }
        }
    ) { padding ->
        if (alarms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum alarme definido",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        onToggle = { enabled ->
                            val updated = alarms.map {
                                if (it.id == alarm.id) it.copy(isEnabled = enabled) else it
                            }
                            alarms = updated
                            val changed = alarm.copy(isEnabled = enabled)
                            if (enabled) alarmScheduler.schedule(changed)
                            else alarmScheduler.cancel(changed)
                        },
                        onDelete = {
                            alarmScheduler.cancel(alarm)
                            alarms = alarms.filter { it.id != alarm.id }
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AddAlarmDialog(
            onDismiss = { showDialog = false },
            onConfirm = { alarm ->
                alarms = alarms + alarm
                alarmScheduler.schedule(alarm)
                showDialog = false
            }
        )
    }
}

@Composable
fun AlarmCard(
    alarm: AlarmData,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val timeText = String.format("%02d:%02d", alarm.hour, alarm.minute)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timeText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                if (alarm.label.isNotEmpty()) {
                    Text(
                        text = alarm.label,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (alarm.daysOfWeek.isNotEmpty()) {
                    Text(
                        text = alarm.daysOfWeek.joinToString(", ") { dayText(it) },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = onToggle
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmDialog(
    onDismiss: () -> Unit,
    onConfirm: (AlarmData) -> Unit
) {
    var hour by remember { mutableIntStateOf(8) }
    var minute by remember { mutableIntStateOf(0) }
    var label by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(setOf<Int>()) }

    val timePickerState = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Alarme") },
        text = {
            Column {
                TimePicker(state = timePickerState)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Rótulo") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Repetir:", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(
                        "D" to Calendar.MONDAY,
                        "S" to Calendar.TUESDAY,
                        "T" to Calendar.WEDNESDAY,
                        "Q" to Calendar.THURSDAY,
                        "Q" to Calendar.FRIDAY,
                        "S" to Calendar.SATURDAY,
                        "D" to Calendar.SUNDAY
                    ).forEach { (abbr, day) ->
                        FilterChip(
                            selected = day in selectedDays,
                            onClick = {
                                selectedDays = if (day in selectedDays)
                                    selectedDays - day
                                else
                                    selectedDays + day
                            },
                            label = { Text(abbr) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val alarm = AlarmData(
                        hour = timePickerState.hour,
                        minute = timePickerState.minute,
                        label = label,
                        daysOfWeek = selectedDays
                    )
                    onConfirm(alarm)
                }
            ) { Text("Confirmar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

private fun dayText(day: Int): String = when (day) {
    Calendar.MONDAY -> "Seg"
    Calendar.TUESDAY -> "Ter"
    Calendar.WEDNESDAY -> "Qua"
    Calendar.THURSDAY -> "Qui"
    Calendar.FRIDAY -> "Sex"
    Calendar.SATURDAY -> "Sab"
    Calendar.SUNDAY -> "Dom"
    else -> ""
}
