package com.redlantern.restopulse.data.database.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["normalizedNumber"], unique = true),
        Index(value = ["name"]),
        Index(value = ["customerTag"]),
        Index(value = ["lastCallDate"])
    ]
)
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val phoneNumber: String,
    val normalizedNumber: String,
    val dateAdded: Long,
    val lastCallDate: Long = 0,
    val lastCallDuration: Long = 0,
    val incomingCount: Int = 0,
    val outgoingCount: Int = 0,
    val missedCount: Int = 0,
    val totalCalls: Int = 0,
    val whatsappAvailable: Boolean = false,
    val favorite: Boolean = false,
    val vip: Boolean = false,
    val customerTag: String = "",
    val notes: String = "",
    val photo: String? = null,
    val birthday: Long? = null,
    val anniversary: Long? = null,
    val address: String = "",
    val location: String = "",
    val groupId: Long? = null
) : Parcelable
