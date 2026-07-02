package com.redlantern.restopulse.utils

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhoneNumberNormalizer @Inject constructor() {
    fun normalize(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        var digits = raw.filter(Char::isDigit)
        while (digits.startsWith("00") && digits.length > 10) digits = digits.drop(2)
        if (digits.length > 10 && digits.startsWith("91")) digits = digits.drop(2)
        while (digits.length > 10 && digits.startsWith("0")) digits = digits.drop(1)
        if (digits.length > 10) digits = digits.takeLast(10)
        return digits
    }

    fun isValidMobile(raw: String?): Boolean = normalize(raw).length >= 10
}
