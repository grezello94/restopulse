package com.redlantern.restopulse.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.redlantern.restopulse.models.CallType

@Entity(
    tableName = "call_history",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("customerId"),
        Index("normalizedNumber"),
        Index("callDate"),
        Index(value = ["normalizedNumber", "callDate", "callType"], unique = true)
    ]
)
data class CallHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long?,
    val phoneNumber: String,
    val normalizedNumber: String,
    val callerName: String,
    val callType: CallType,
    val callDate: Long,
    val durationSeconds: Long,
    val savedToCustomer: Boolean
)
