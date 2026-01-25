package com.dailybalance.app.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dailybalance.app.R
import java.util.Locale
import kotlinx.coroutines.delay
import kotlin.math.max

@Composable
fun HomeScreen(
    lastCigaretteTimestamp: Long?,
    todayCigarettesCount: Int,
    todayBeersCount: Int,
    onCigaretteClick: () -> Unit,
    onBeerClick: () -> Unit,
    onFoodClick: () -> Unit,
    onViewRecordsClick: () -> Unit,
    onDeleteAllClick: () -> Unit,
    onMoneyClick: () -> Unit,
    onViewExpensesClick: () -> Unit,
    onTodayCigarettesClick: () -> Unit,
    onTodayBeersClick: () -> Unit,
) {
    // Ticker estable: evita reiniciar/cancelar el bucle cada vez que cambia el timestamp.
    // Esto ayuda a que el banner no "desaparezca y reaparezca" en un frame intermedio.
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(30_000L)
        }
    }

    val elapsedMs: Long? by remember(lastCigaretteTimestamp, nowMs) {
        derivedStateOf {
            lastCigaretteTimestamp?.let { ts -> max(0L, nowMs - ts) }
        }
    }

    val bannerText: String? by remember(elapsedMs) {
        derivedStateOf { elapsedMs?.let(::formatElapsed) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Mantén el layout estable: no desmontar el bloque entero evita el salto visual.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
                .animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (bannerText != null && elapsedMs != null) {
                MotivationalSmokeFreeBanner(
                    elapsedLabel = bannerText!!,
                    elapsedMs = elapsedMs!!,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                )
            }

            // Los contadores no deberían desaparecer al pulsar (siempre visibles).
            TodayCountersRow(
                cigarettes = todayCigarettesCount,
                beers = todayBeersCount,
                modifier = Modifier.fillMaxWidth(),
                onCigarettesClick = onTodayCigarettesClick,
                onBeersClick = onTodayBeersClick,
            )
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

@Composable
private fun TodayCountersRow(
    cigarettes: Int,
    beers: Int,
    modifier: Modifier = Modifier,
    onCigarettesClick: () -> Unit,
    onBeersClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatChip(
            label = "Cigarettes today",
            value = cigarettes,
            modifier = Modifier.weight(1f),
            onClick = onCigarettesClick,
        )
        StatChip(
            label = "Beers today",
            value = beers,
            modifier = Modifier.weight(1f),
            onClick = onBeersClick,
        )
    }
}

@Composable
private fun StatChip(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            }
        ),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun MotivationalSmokeFreeBanner(
    elapsedLabel: String,
    elapsedMs: Long,
    modifier: Modifier = Modifier,
) {
    val thresholdMs = 90L * 60L * 1000L // 1h30m
    val isBelowThreshold = elapsedMs < thresholdMs

    val positiveBackground = MaterialTheme.colorScheme.primaryContainer
    val positiveContent = MaterialTheme.colorScheme.onPrimaryContainer

    // Rojo MÁS intenso.
    val warningBackground = MaterialTheme.colorScheme.error
    val warningContent = MaterialTheme.colorScheme.onError

    val backgroundColor = if (isBelowThreshold) warningBackground else positiveBackground
    val contentColor = if (isBelowThreshold) warningContent else positiveContent

    val elevation = if (isBelowThreshold) 14.dp else 10.dp
    val borderColor = if (isBelowThreshold) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.outlineVariant
    val borderWidth = if (isBelowThreshold) 2.dp else 1.dp

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .shadow(elevation = elevation, shape = MaterialTheme.shapes.large)
                .fillMaxWidth(),
            color = backgroundColor,
            contentColor = contentColor,
            shape = MaterialTheme.shapes.large,
            tonalElevation = 0.dp,
            border = BorderStroke(borderWidth, borderColor)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Smoke-free: $elapsedLabel",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isBelowThreshold) {
                        "First 90 minutes are the hardest. Stay strong."
                    } else {
                        "You're doing it. One decision at a time."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.92f)
                )
            }
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
        String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
    }
}
