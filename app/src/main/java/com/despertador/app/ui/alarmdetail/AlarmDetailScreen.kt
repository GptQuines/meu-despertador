package com.despertador.app.ui.alarmdetail

import android.app.TimePickerDialog
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmDetailScreen(
    alarmId: Long,
    onNavigateBack: () -> Unit,
    viewModel: AlarmDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val hour by viewModel.hour.collectAsState()
    val minute by viewModel.minute.collectAsState()
    val label by viewModel.label.collectAsState()
    val daysOfWeek by viewModel.daysOfWeek.collectAsState()
    val vibrate by viewModel.vibrate.collectAsState()
    val snoozeEnabled by viewModel.snoozeEnabled.collectAsState()
    val snoozeDuration by viewModel.snoozeDuration.collectAsState()
    val maxSnoozes by viewModel.maxSnoozes.collectAsState()
    val ringtoneUri by viewModel.ringtoneUri.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (alarmId > 0) "Editar alarme" else "Novo alarme"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Time display (click to change)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        TimePickerDialog(
                            context,
                            { _, h, m ->
                                viewModel.updateHour(h)
                                viewModel.updateMinute(m)
                            },
                            hour, minute, true
                        ).show()
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = String.format("%02d:%02d", hour, minute),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Label
            OutlinedTextField(
                value = label,
                onValueChange = { viewModel.updateLabel(it) },
                label = { Text("Nome do alarme") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Days of week
            Text(
                text = "Repetir",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val dayInfos = listOf(
                    "Dom" to Calendar.SUNDAY,
                    "Seg" to Calendar.MONDAY,
                    "Ter" to Calendar.TUESDAY,
                    "Qua" to Calendar.WEDNESDAY,
                    "Qui" to Calendar.THURSDAY,
                    "Sex" to Calendar.FRIDAY,
                    "S\u00e1b" to Calendar.SATURDAY
                )
                dayInfos.forEach { (name, day) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = name,
                            fontSize = 10.sp,
                            color = if (day in daysOfWeek)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Checkbox(
                            checked = day in daysOfWeek,
                            onCheckedChange = { viewModel.toggleDay(day) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ringtone picker
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Selecionar toque")
                            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ringtoneUri)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                        }
                        context.startActivity(intent)
                    },
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
                    Text(
                        text = "Toque",
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = viewModel.getRingtoneDisplayName(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vibrate
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.updateVibrate(!vibrate) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vibra\u00e7\u00e3o",
                    modifier = Modifier.weight(1f)
                )
                Checkbox(
                    checked = vibrate,
                    onCheckedChange = { viewModel.updateVibrate(it) }
                )
            }

            // Snooze enabled
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.updateSnoozeEnabled(!snoozeEnabled) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Soneca",
                    modifier = Modifier.weight(1f)
                )
                Checkbox(
                    checked = snoozeEnabled,
                    onCheckedChange = { viewModel.updateSnoozeEnabled(it) }
                )
            }

            if (snoozeEnabled) {
                Spacer(modifier = Modifier.height(8.dp))

                // Snooze duration
                Text(
                    text = "Dura\u00e7\u00e3o da soneca:  min",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5, 10, 15, 20, 30).forEach { d ->
                        Button(
                            onClick = { viewModel.updateSnoozeDuration(d) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (snoozeDuration == d)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (snoozeDuration == d)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("min", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Max snoozes
                Text(
                    text = "M\u00e1ximo de sonecas: ",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val options = listOf(1 to "1x", 2 to "2x", 3 to "3x", 5 to "5x", 10 to "10x", 99 to "Ilim.")
                    options.forEach { (count, label) ->
                        Button(
                            onClick = { viewModel.updateMaxSnoozes(count) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (maxSnoozes == count)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (maxSnoozes == count)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(label, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save button
            Button(
                onClick = {
                    viewModel.save {
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Salvar", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

