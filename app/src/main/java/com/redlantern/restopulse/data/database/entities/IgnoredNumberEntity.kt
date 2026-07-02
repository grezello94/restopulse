package com.redlantern.restopulse.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ignored_numbers")
data class IgnoredNumberEntity(
    @PrimaryKey val normalizedNumber: String,
    val rawNumber: String,
    val ignoredAt: Long
)
