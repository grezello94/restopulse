package com.redlantern.restopulse.core.logging

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashReporter @Inject constructor() {
    fun setUser(numberHash: String) {
        runCatching { Firebase.crashlytics.setUserId(numberHash) }
    }

    fun record(error: Throwable) {
        runCatching { Firebase.crashlytics.recordException(error) }
    }
}
