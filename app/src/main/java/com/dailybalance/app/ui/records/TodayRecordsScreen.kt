package com.dailybalance.app.ui.records

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailybalance.app.data.ActionRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TodayRecordsScreen(
    type: String,
    records: List<ActionRecord>,
    onBackClick: () -> Unit,
    onDeleteTodayClick: () -> Unit,
    onDeleteRecordConfirm: (ActionRecord) -> Unit,
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    var recordToDelete by remember { mutableStateOf<ActionRecord?>(null) }

    if (recordToDelete != null) {
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("Confirmar borrado") },
            text = { Text("¿Quieres borrar este registro?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteRecordConfirm(recordToDelete!!)
                        recordToDelete = null
                    }
                ) { Text("Borrar") }
            },
            dismissButton = {
                Button(onClick = { recordToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    val (title, emptyText) = when (type) {
        "cigarette" -> "Cigarros de hoy" to "No hay cigarros registrados hoy."
        "beer" -> "Cervezas de hoy" to "No hay cervezas registradas hoy."
        else -> "Registros de hoy" to "No hay registros hoy."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = title, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(12.dp))

        if (records.isEmpty()) {
            Text(text = emptyText, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(records, key = { it.id }) { record ->
                val formattedTime = dateFormat.format(Date(record.timestamp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = formattedTime, fontSize = 16.sp, modifier = Modifier.weight(1f))

                    IconButton(onClick = { recordToDelete = record }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Borrar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBackClick) { Text("Volver") }
            Button(
                onClick = onDeleteTodayClick,
                enabled = records.isNotEmpty()
            ) { Text("Borrar hoy") }
        }
    }
}
