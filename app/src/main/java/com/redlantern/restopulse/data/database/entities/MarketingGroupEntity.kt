package com.redlantern.restopulse.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "marketing_groups")
data class MarketingGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val maxSize: Int,
    val createdAt: Long,
    val updatedAt: Long
)
