package com.dailybalance.app.ui.records

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailybalance.app.data.DailyExpense
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp


@Composable
fun ExpenseRecordsScreen(
    expenseRecords: List<DailyExpense>,
    onBackClick: () -> Unit,
    onExportClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()) }
    var sortColumn by remember { mutableStateOf("date") }
    var sortAscending by remember { mutableStateOf(false) }

    val sortedRecords = remember(expenseRecords, sortColumn, sortAscending) {
        expenseRecords.sortedWith(compareBy<DailyExpense> {
            when (sortColumn) {
                "amount" -> it.amount
                "category" -> it.category
                "date" -> it.date
                "origin" -> it.origin
                "note" -> it.note ?: ""
                else -> it.date
            }
        }.let { comparator -> if (sortAscending) comparator else comparator.reversed() })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(androidx.compose.ui.graphics.Color.LightGray),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Gastos", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableHeader(
                text = "Cantidad",
                sorted = sortColumn == "amount",
                ascending = sortAscending,
                onClick = {
                    if (sortColumn == "amount") sortAscending = !sortAscending else {
                        sortColumn = "amount"; sortAscending = true
                    }
                }
            )
            TableHeader(
                text = "Categoría",
                sorted = sortColumn == "category",
                ascending = sortAscending,
                onClick = {
                    if (sortColumn == "category") sortAscending = !sortAscending else {
                        sortColumn = "category"; sortAscending = true
                    }
                }
            )
            TableHeader(
                text = "Fecha",
                sorted = sortColumn == "date",
                ascending = sortAscending,
                onClick = {
                    if (sortColumn == "date") sortAscending = !sortAscending else {
                        sortColumn = "date"; sortAscending = true
                    }
                }
            )
            TableHeader(
                text = "Origen",
                sorted = sortColumn == "origin",
                ascending = sortAscending,
                onClick = {
                    if (sortColumn == "origin") sortAscending = !sortAscending else {
                        sortColumn = "origin"; sortAscending = true
                    }
                }
            )
            TableHeader(
                text = "Nota",
                sorted = sortColumn == "note",
                ascending = sortAscending,
                onClick = {
                    if (sortColumn == "note") sortAscending = !sortAscending else {
                        sortColumn = "note"; sortAscending = true
                    }
                }
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )  {
            items(sortedRecords) { expense ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "${expense.amount} €", fontSize = 16.sp, modifier = Modifier.width(100.dp))
                    Text(text = expense.category, fontSize = 16.sp, modifier = Modifier.width(120.dp))
                    Text(text = dateFormat.format(Date(expense.date)), fontSize = 16.sp, modifier = Modifier.width(160.dp))
                    Text(text = expense.origin ?: "", fontSize = 16.sp, modifier = Modifier.width(120.dp))
                    Text(text = expense.note ?: "", fontSize = 16.sp, modifier = Modifier.width(160.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBackClick) {
                Text("Volver")
            }
            Button(onClick = onExportClick) {
                Text("Exportar gastos")
            }
        }
    }
}

@Composable
private fun TableHeader(text: String, sorted: Boolean, ascending: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clickable { onClick() }
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text,
            fontSize = 16.sp
        )
        if (sorted) {
            Icon(
                imageVector = if (ascending) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (ascending) "Ascendente" else "Descendente",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}