package com.redlantern.restopulse.utils

import java.text.DateFormat
import java.util.Date

fun Long.readable(): String = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(this))
