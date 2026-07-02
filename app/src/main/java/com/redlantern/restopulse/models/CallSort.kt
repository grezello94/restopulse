package com.redlantern.restopulse.models

enum class CallSort(val label: String) {
    NEWEST("Newest"),
    OLDEST("Oldest"),
    DURATION("Duration"),
    INCOMING("Incoming"),
    OUTGOING("Outgoing"),
    MISSED("Missed")
}
