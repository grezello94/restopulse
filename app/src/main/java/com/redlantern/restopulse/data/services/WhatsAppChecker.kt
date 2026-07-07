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
    enum class WhatsAppApp(val packageName: String, val label: String) {
        PERSONAL("com.whatsapp", "WhatsApp"),
        BUSINESS("com.whatsapp.w4b", "Business WhatsApp")
    }

    data class ChatAvailability(
        val personal: Boolean,
        val business: Boolean
    ) {
        val any: Boolean get() = personal || business
    }

    fun isWhatsAppInstalled(): Boolean =
        isPackageInstalled("com.whatsapp") || isPackageInstalled("com.whatsapp.w4b")

    fun canOpenChat(normalizedNumber: String): Boolean {
        if (!isWhatsAppInstalled()) return false
        val uri = Uri.parse("https://wa.me/91$normalizedNumber")
        val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return intent.resolveActivity(context.packageManager) != null
    }

    fun chatAvailability(normalizedNumber: String): ChatAvailability =
        ChatAvailability(
            personal = canOpenChat(normalizedNumber, WhatsAppApp.PERSONAL),
            business = canOpenChat(normalizedNumber, WhatsAppApp.BUSINESS)
        )

    fun canOpenChat(normalizedNumber: String, app: WhatsAppApp): Boolean {
        if (!isPackageInstalled(app.packageName)) return false
        val intent = chatIntent(normalizedNumber).setPackage(app.packageName)
        return intent.resolveActivity(context.packageManager) != null
    }

    fun openChat(normalizedNumber: String) {
        context.startActivity(chatIntent(normalizedNumber))
    }

    fun openChat(normalizedNumber: String, app: WhatsAppApp) {
        context.startActivity(chatIntent(normalizedNumber).setPackage(app.packageName))
    }

    private fun chatIntent(normalizedNumber: String): Intent =
        Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/91$normalizedNumber"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    private fun isPackageInstalled(packageName: String): Boolean = runCatching {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    }.getOrDefault(false)
}
