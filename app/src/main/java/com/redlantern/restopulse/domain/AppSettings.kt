package com.redlantern.restopulse.domain

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val maxGroupSize: Int = 250,
    val lastExportStatus: String = "Ready"
)
