package com.dngwjy.fleettracker.ui.screens.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dngwjy.fleettracker.data.model.Alert
import com.dngwjy.fleettracker.data.model.AlertType

@Composable
fun AlertList(modifier: Modifier = Modifier, alerts: List<Alert>) {
    Column(modifier = modifier.fillMaxWidth()) {
        alerts.forEach { alert ->
            AlertItem(alert = alert)
        }
    }
}

@Composable
fun AlertItem(alert: Alert) {
    val colors = when (alert.type) {
        AlertType.SPEED -> Color.Red to Color.White
        AlertType.DOOR -> Color.Red to Color.White
        AlertType.ENGINE -> Color.Yellow to Color.Black
    }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(0.9f),
        colors = CardDefaults.cardColors(
            containerColor = colors.first,
            contentColor = colors.second
        )
    ) {
        Text(
            text = alert.message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium
        )
    }
}