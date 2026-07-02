package com.redlantern.restopulse.data.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhatsAppChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun isWhatsAppInstalled(): Boolean =
        isPackageInstalled("com.whatsapp") || isPackageInstalled("com.whatsapp.w4b")

    fun canOpenChat(normalizedNumber: String): Boolean {
        if (!isWhatsAppInstalled()) return false
        val uri = Uri.parse("https://wa.me/91$normalizedNumber")
        val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent.resolveActivity(context.packageManager) != null
    }

    fun openChat(normalizedNumber: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/91$normalizedNumber"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun isPackageInstalled(packageName: String): Boolean = runCatching {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    }.getOrDefault(false)
}
