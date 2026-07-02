package com.redlantern.restopulse.data.database

import androidx.room.TypeConverter
import com.redlantern.restopulse.models.CallType

class Converters {
    @TypeConverter fun toCallType(value: String): CallType = CallType.valueOf(value)
    @TypeConverter fun fromCallType(type: CallType): String = type.name
}
