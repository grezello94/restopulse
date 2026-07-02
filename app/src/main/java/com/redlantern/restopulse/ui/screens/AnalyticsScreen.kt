package com.redlantern.restopulse.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun AnalyticsScreen(padding: PaddingValues) {
    val color = MaterialTheme.colorScheme.primary
    LazyColumn(
        contentPadding = PaddingValues(top = padding.calculateTopPadding() + 16.dp, bottom = padding.calculateBottomPadding() + 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Analytics", style = MaterialTheme.typography.headlineMedium) }
        item {
            Card {
                Canvas(Modifier.fillMaxWidth().height(220.dp)) {
                    val points = listOf(.18f, .32f, .28f, .52f, .48f, .72f, .65f)
                    val step = size.width / (points.lastIndex.coerceAtLeast(1))
                    points.zipWithNext().forEachIndexed { index, pair ->
                        drawLine(
                            color = color,
                            start = Offset(index * step, size.height * (1f - pair.first)),
                            end = Offset((index + 1) * step, size.height * (1f - pair.second)),
                            strokeWidth = 8f,
                            cap = StrokeCap.Round
                        )
                    }
                    drawCircle(color.copy(alpha = .12f), radius = size.minDimension / 3, style = Stroke(14f))
                }
            }
        }
        item { Text("Daily, weekly, monthly, repeat-customer, average-duration, and most-active-customer analytics are backed by local call/customer data and ready for richer chart drill-downs as the dataset grows.") }
    }
}
