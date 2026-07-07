package com.redlantern.restopulse.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.redlantern.restopulse.ui.components.ContentCard
import com.redlantern.restopulse.ui.components.ScreenHeader
import com.redlantern.restopulse.ui.components.SectionTitle
import com.redlantern.restopulse.ui.components.StatusPill
import com.redlantern.restopulse.ui.components.StatusTone
import com.redlantern.restopulse.ui.components.screenPadding

@Composable
fun AnalyticsScreen(padding: PaddingValues) {
    val color = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    LazyColumn(
        contentPadding = screenPadding(padding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenHeader("Analytics", "Readable trend snapshots for calls and customer activity") }
        item { SectionTitle("Engagement Trend") }
        item {
            ContentCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatusPill("Local data", tone = StatusTone.Positive)
                    Canvas(Modifier.fillMaxWidth().height(220.dp)) {
                        val points = listOf(.18f, .32f, .28f, .52f, .48f, .72f, .65f)
                        val step = size.width / (points.lastIndex.coerceAtLeast(1))
                        drawLine(
                            color = secondary.copy(alpha = .18f),
                            start = Offset(0f, size.height * .72f),
                            end = Offset(size.width, size.height * .72f),
                            strokeWidth = 3f
                        )
                        points.zipWithNext().forEachIndexed { index, pair ->
                            drawLine(
                                color = color,
                                start = Offset(index * step, size.height * (1f - pair.first)),
                                end = Offset((index + 1) * step, size.height * (1f - pair.second)),
                                strokeWidth = 8f,
                                cap = StrokeCap.Round
                            )
                        }
                        points.forEachIndexed { index, value ->
                            drawCircle(
                                color = secondary,
                                radius = 7f,
                                center = Offset(index * step, size.height * (1f - value))
                            )
                        }
                        drawCircle(color.copy(alpha = .10f), radius = size.minDimension / 3, style = Stroke(14f))
                    }
                    Text("Repeat callers and recent activity are trending upward.", style = MaterialTheme.typography.titleMedium)
                    Text("Richer chart drill-downs can plug into this card as the dataset grows.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
