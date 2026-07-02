package com.redlantern.restopulse.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "duplicate_events")
data class DuplicateEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val normalizedNumber: String,
    val detectedAt: Long,
    val reason: String
)
