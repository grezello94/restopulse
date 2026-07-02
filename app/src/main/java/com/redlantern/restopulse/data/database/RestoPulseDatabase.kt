package com.redlantern.restopulse.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.redlantern.restopulse.data.database.dao.CallHistoryDao
import com.redlantern.restopulse.data.database.dao.CustomerDao
import com.redlantern.restopulse.data.database.dao.MarketingGroupDao
import com.redlantern.restopulse.data.database.entities.CallHistoryEntity
import com.redlantern.restopulse.data.database.entities.CustomerEntity
import com.redlantern.restopulse.data.database.entities.DuplicateEventEntity
import com.redlantern.restopulse.data.database.entities.IgnoredNumberEntity
import com.redlantern.restopulse.data.database.entities.MarketingGroupEntity

@Database(
    entities = [
        CustomerEntity::class,
        CallHistoryEntity::class,
        MarketingGroupEntity::class,
        IgnoredNumberEntity::class,
        DuplicateEventEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class RestoPulseDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun callHistoryDao(): CallHistoryDao
    abstract fun marketingGroupDao(): MarketingGroupDao
}
