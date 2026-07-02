package com.redlantern.restopulse.models

enum class CustomerFilter(val label: String) {
    ALL("All"),
    WHATSAPP("WhatsApp"),
    NON_WHATSAPP("Non WhatsApp"),
    VIP("VIP"),
    FAVORITES("Favorites"),
    TODAY("Today's Calls"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    MISSED("Missed"),
    INCOMING("Incoming"),
    OUTGOING("Outgoing")
}
