package com.redlantern.restopulse.data.services

import android.content.Context
import android.provider.CallLog
import com.redlantern.restopulse.data.database.entities.CallHistoryEntity
import com.redlantern.restopulse.models.CallType
import com.redlantern.restopulse.utils.PhoneNumberNormalizer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallLogDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val normalizer: PhoneNumberNormalizer
) {
    fun latestCall(): CallHistoryEntity? = read(limit = 1).firstOrNull()

    fun read(limit: Int = 300): List<CallHistoryEntity> {
        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION
        )
        val results = mutableListOf<CallHistoryEntity>()
        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null,
            null,
            "${CallLog.Calls.DATE} DESC LIMIT $limit"
        )?.use { cursor ->
            val numberIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
            val nameIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)
            val typeIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)
            val dateIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)
            val durationIndex = cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)
            while (cursor.moveToNext()) {
                val number = cursor.getString(numberIndex).orEmpty()
                val normalized = normalizer.normalize(number)
                if (normalized.isBlank()) continue
                results += CallHistoryEntity(
                    customerId = null,
                    phoneNumber = number,
                    normalizedNumber = normalized,
                    callerName = cursor.getString(nameIndex).orEmpty(),
                    callType = cursor.getInt(typeIndex).toCallType(),
                    callDate = cursor.getLong(dateIndex),
                    durationSeconds = cursor.getLong(durationIndex),
                    savedToCustomer = false
                )
            }
        }
        return results
    }

    private fun Int.toCallType(): CallType = when (this) {
        CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
        CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
        CallLog.Calls.MISSED_TYPE -> CallType.MISSED
        CallLog.Calls.REJECTED_TYPE -> CallType.REJECTED
        CallLog.Calls.BLOCKED_TYPE -> CallType.BLOCKED
        else -> CallType.UNKNOWN
    }
}
