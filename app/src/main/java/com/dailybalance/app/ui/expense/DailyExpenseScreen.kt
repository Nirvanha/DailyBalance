package com.dailybalance.app.ui.expense

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import java.util.Locale
import androidx.compose.material3.MenuAnchorType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyExpenseScreen(
    amountText: String,
    category: String,
    origin: String,
    isAmountValid: Boolean,
    showExpenseError: Boolean,
    onAmountTextChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onOriginChange: (String) -> Unit,
    onRegisterExpenseClick: () -> Unit,
    onBackClick: () -> Unit,
    categoryOptions: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Registrar gasto diario", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = amountText,
            onValueChange = onAmountTextChange,
            label = { Text("Cantidad") },
            isError = !isAmountValid,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        if (!isAmountValid) {
            Text(
                text = "Introduce una cantidad válida mayor que 0",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        var categoryExpanded by remember { mutableStateOf(false) }

        // Normalizamos opciones (sin duplicados, y con 1ª letra en mayúscula)
        val normalizedCategoryOptions = remember(categoryOptions) {
            categoryOptions
                .map { it.trim().replaceFirstChar { c -> c.titlecase(Locale.getDefault()) } }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
        }

        // Filtrado en tiempo real por lo que el usuario escriba
        val filteredCategoryOptions = remember(category, normalizedCategoryOptions) {
            val query = category.trim()
            if (query.isBlank()) {
                normalizedCategoryOptions
            } else {
                normalizedCategoryOptions.filter { it.contains(query, ignoreCase = true) }
            }
        }

        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { shouldExpand -> categoryExpanded = shouldExpand },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = { input ->
                    val trimmed = input.trimStart() // deja escribir pero evita espacios al inicio
                    onCategoryChange(trimmed)
                    // Mientras escribes, mantenemos abierto para ver sugerencias (pero no tocamos el foco)
                    categoryExpanded = true
                },
                label = { Text("Categoría") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryEditable).fillMaxWidth(),
                singleLine = true
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                filteredCategoryOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onCategoryChange(option)
                            categoryExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        val originOptions = listOf("Nomina", "NoCuenta", "Credito", "Eci", "Efectivo")
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = origin,
                onValueChange = {}, // No editable manual
                readOnly = true,
                label = { Text("Origen") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                originOptions.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            onOriginChange(selectionOption)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                // Antes de registrar, forzamos formato: primera letra en mayúscula
                val normalized = category.trim().replaceFirstChar { c -> c.titlecase(Locale.getDefault()) }
                if (normalized != category) {
                    onCategoryChange(normalized)
                }
                onRegisterExpenseClick()
            },
            enabled = isAmountValid && category.isNotBlank() && origin.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar gasto")
        }
        if (showExpenseError) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Por favor, completa todos los campos.",
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onBackClick, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }
}
