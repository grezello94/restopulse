package com.redlantern.restopulse.data.services

import android.content.ContentProviderOperation
import android.content.Context
import android.provider.ContactsContract
import com.redlantern.restopulse.utils.PhoneNumberNormalizer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactsDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val normalizer: PhoneNumberNormalizer
) {
    private val generatedName = Regex("^RL Customer (\\d+)$", RegexOption.IGNORE_CASE)

    fun contactExists(rawNumber: String): Boolean {
        val target = normalizer.normalize(rawNumber)
        if (target.isBlank()) return false
        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val numberIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (cursor.moveToNext()) {
                if (normalizer.normalize(cursor.getString(numberIndex)) == target) return true
            }
        }
        return false
    }

    fun addContact(name: String, number: String) {
        val operations = arrayListOf<ContentProviderOperation>()
        val rawContactId = operations.size
        operations += ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
            .build()
        operations += ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name.ifBlank { number })
            .build()
        operations += ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
            .build()
        context.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
    }

    /** Returns the lowest available positive number in the RL Customer name sequence. */
    fun nextAvailableCustomerNumber(): Int {
        val used = mutableSetOf<Int>()
        context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY),
            null,
            null,
            null
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            while (cursor.moveToNext()) {
                generatedName.matchEntire(cursor.getString(nameIndex).orEmpty().trim())
                    ?.groupValues?.getOrNull(1)?.toIntOrNull()?.let(used::add)
            }
        }
        var candidate = 1
        while (candidate in used) candidate++
        return candidate
    }
}
