package com.dailybalance.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dailybalance.app.R

@Composable
fun HomeScreen(
    onCigaretteClick: () -> Unit,
    onBeerClick: () -> Unit,
    onFoodClick: () -> Unit,
    onViewRecordsClick: () -> Unit,
    onDeleteAllClick: () -> Unit,
    onMoneyClick: () -> Unit,
    onViewExpensesClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
            Button( onClick = onMoneyClick ) {
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
