package com.redlantern.restopulse.utils

import java.text.DateFormat
import java.util.Date

private val readableDateTimeFormat = ThreadLocal.withInitial {
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
}

fun Long.readable(): String = readableDateTimeFormat.get()!!.format(Date(this))
