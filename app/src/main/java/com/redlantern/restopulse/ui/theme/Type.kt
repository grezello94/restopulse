package com.redlantern.restopulse.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    displaySmall = Typography().displaySmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
    headlineLarge = Typography().headlineLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp),
    headlineMedium = Typography().headlineMedium.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = Typography().titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = Typography().titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = Typography().labelLarge.copy(fontWeight = FontWeight.SemiBold)
)
