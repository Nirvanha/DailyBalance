package com.dailybalance.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dailybalance.app.R
import kotlinx.coroutines.delay
import kotlin.math.max

@Composable
fun HomeScreen(
    lastCigaretteTimestamp: Long?,
    onCigaretteClick: () -> Unit,
    onBeerClick: () -> Unit,
    onFoodClick: () -> Unit,
    onViewRecordsClick: () -> Unit,
    onDeleteAllClick: () -> Unit,
    onMoneyClick: () -> Unit,
    onViewExpensesClick: () -> Unit
) {
    var bannerText by remember(lastCigaretteTimestamp) { mutableStateOf<String?>(null) }

    LaunchedEffect(lastCigaretteTimestamp) {
        if (lastCigaretteTimestamp == null) {
            bannerText = null
            return@LaunchedEffect
        }

        fun compute(): String {
            val now = System.currentTimeMillis()
            val elapsedMs = max(0L, now - lastCigaretteTimestamp)
            return "Last cigarette ${formatElapsed(elapsedMs)}"
        }

        bannerText = compute()
        while (true) {
            delay(30_000L)
            bannerText = compute()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (bannerText != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = bannerText!!,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onCigaretteClick) {
                Image(
                    painter = painterResource(id = R.drawable.ic_cigarette),
                    contentDescription = "Cigarette",
                    modifier = Modifier.size(32.dp)
                )
            }
            Button(onClick = onBeerClick) {
                Image(
                    painter = painterResource(id = R.drawable.ic_beer),
                    contentDescription = "Beer",
                    modifier = Modifier.size(32.dp)
                )
            }
            Button(onClick = onFoodClick) {
                Image(
                    painter = painterResource(id = R.drawable.ic_food),
                    contentDescription = "Food",
                    modifier = Modifier.size(32.dp)
                )
            }
            Button(onClick = onMoneyClick) {
                Image(
                    painter = painterResource(id = R.drawable.ic_money),
                    contentDescription = "Money",
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Button(
            onClick = onViewExpensesClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text("Ver gastos")
        }
        Button(
            onClick = onViewRecordsClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver registros")
        }

        Button(
            onClick = onDeleteAllClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Borrar registros")
        }
    }
}

private fun formatElapsed(elapsedMs: Long): String {
    val totalMinutes = elapsedMs / 60_000L
    val days = totalMinutes / (60L * 24L)
    return if (days >= 1L) {
        val hours = (totalMinutes / 60L) % 24L
        "${days}d ${hours}h"
    } else {
        val hours = totalMinutes / 60L
        val minutes = totalMinutes % 60L
        String.format("%02d:%02d", hours, minutes)
    }
}
