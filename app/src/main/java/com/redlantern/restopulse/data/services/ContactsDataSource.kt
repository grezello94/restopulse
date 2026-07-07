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
    // Legacy contacts may contain parentheses around the sequence, trailing last
    // four digits, or a short-lived prefix format. Every recognized variant is
    // rewritten to: RL Customer 153 [7584] / RL Frq Customer 153 [7584].
    private val generatedName = Regex(
        "^RL\\s+(?:\\d{4}\\s+)?Customer\\s+\\(?(\\d+)\\)?(?:\\s+\\[\\d{4}])?$",
        RegexOption.IGNORE_CASE
    )
    private val generatedFrequentName = Regex(
        "^RL\\s+(?:\\d{4}\\s+)?Frq\\s+Customer\\s+\\(?(\\d+)\\)?(?:\\s+\\[\\d{4}])?$",
        RegexOption.IGNORE_CASE
    )

    private fun generatedCustomerName(sequence: Int, normalizedNumber: String): String =
        "RL Customer $sequence [${normalizedNumber.takeLast(4)}]"

    private fun generatedFrequentCustomerName(sequence: Int, normalizedNumber: String): String =
        "RL Frq Customer $sequence [${normalizedNumber.takeLast(4)}]"

    /** Adds the phone suffix to every existing generated RL contact name. */
    fun backfillGeneratedContactNames(): Map<String, String> {
        val pendingByRawContact = linkedMapOf<Long, Pair<String, String>>()
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            ),
            null,
            null,
            null
        )?.use { cursor ->
            val numberIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val rawIdIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID)
            val nameIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val normalized = normalizer.normalize(cursor.getString(numberIndex))
                if (normalized.length < 4) continue
                val currentName = cursor.getString(nameIndex).orEmpty().trim()
                val frequentSequence = generatedFrequentName.matchEntire(currentName)
                    ?.groupValues?.getOrNull(1)?.toIntOrNull()
                val customerSequence = generatedName.matchEntire(currentName)
                    ?.groupValues?.getOrNull(1)?.toIntOrNull()
                val correctedName = when {
                    frequentSequence != null -> generatedFrequentCustomerName(frequentSequence, normalized)
                    customerSequence != null -> generatedCustomerName(customerSequence, normalized)
                    else -> null
                }
                if (correctedName != null && correctedName != currentName) {
                    pendingByRawContact.putIfAbsent(cursor.getLong(rawIdIndex), normalized to correctedName)
                }
            }
        }

        val renamed = linkedMapOf<String, String>()
        pendingByRawContact.forEach { (rawContactId, assignment) ->
            val (normalized, correctedName) = assignment
            runCatching {
                val operation = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                        "${ContactsContract.Data.RAW_CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                        arrayOf(rawContactId.toString(), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    )
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, correctedName)
                    .build()
                val result = context.contentResolver.applyBatch(ContactsContract.AUTHORITY, arrayListOf(operation))
                if (result.firstOrNull()?.count?.let { it > 0 } == true) renamed[normalized] = correctedName
            }
        }
        return renamed
    }

    fun contactExists(rawNumber: String): Boolean {
        val target = normalizer.normalize(rawNumber)
        if (target.isBlank()) return false
        return existingNormalizedNumbers().contains(target)
    }

    fun existingNormalizedNumbers(): Set<String> {
        val numbers = linkedSetOf<String>()
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
                normalizer.normalize(cursor.getString(numberIndex))
                    .takeIf(String::isNotBlank)
                    ?.let(numbers::add)
            }
        }
        return numbers
    }

    /**
     * Renames every raw contact containing [rawNumber]. A normalized-number lookup
     * keeps differently formatted copies of the same number from being processed
     * as separate customers.
     */
    fun renameExistingContact(rawNumber: String, newName: String): Boolean {
        val target = normalizer.normalize(rawNumber)
        if (target.isBlank() || newName.isBlank()) return false

        val rawContactIds = linkedSetOf<Long>()
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID
            ),
            null,
            null,
            null
        )?.use { cursor ->
            val numberIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val rawIdIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID)
            while (cursor.moveToNext()) {
                if (normalizer.normalize(cursor.getString(numberIndex)) == target) {
                    rawContactIds += cursor.getLong(rawIdIndex)
                }
            }
        }
        if (rawContactIds.isEmpty()) return false

        var renamedAny = false
        rawContactIds.forEach { rawContactId ->
            // One read-only synced raw contact must not prevent a writable copy of
            // the same normalized number from being renamed.
            runCatching {
                val operation = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                        "${ContactsContract.Data.RAW_CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                        arrayOf(
                            rawContactId.toString(),
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                        )
                    )
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, newName)
                    .build()
                val result = context.contentResolver.applyBatch(
                    ContactsContract.AUTHORITY,
                    arrayListOf(operation)
                )
                if (result.firstOrNull()?.count?.let { it > 0 } == true) renamedAny = true
            }
        }
        return renamedAny
    }

    /**
     * Adds a phone contact only when its normalized number is not already present.
     *
     * Synchronization makes the existence check and insert atomic for every import
     * and manual-save path in this app process.
     */
    @Synchronized
    fun addContactIfAbsent(name: String, number: String): Boolean {
        if (normalizer.normalize(number).isBlank() || contactExists(number)) return false
        addContact(name, number)
        return true
    }

    @Synchronized
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

    /** Returns an existing RL frequent sequence name for this number, if present. */
    fun frequentCustomerName(rawNumber: String): String? {
        val target = normalizer.normalize(rawNumber)
        if (target.isBlank()) return null
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            ),
            null,
            null,
            null
        )?.use { cursor ->
            val numberIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val nameIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            while (cursor.moveToNext()) {
                if (normalizer.normalize(cursor.getString(numberIndex)) == target) {
                    val name = cursor.getString(nameIndex).orEmpty().trim()
                    if (generatedFrequentName.matches(name)) return name
                }
            }
        }
        return null
    }

    /** Returns the lowest unused positive RL frequent-customer sequence number. */
    fun nextAvailableFrequentCustomerNumber(): Int {
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
                generatedFrequentName.matchEntire(cursor.getString(nameIndex).orEmpty().trim())
                    ?.groupValues?.getOrNull(1)?.toIntOrNull()?.let(used::add)
            }
        }
        var candidate = 1
        while (candidate in used) candidate++
        return candidate
    }

    /**
     * Renames an ordered, deduplicated list of frequent numbers using one contact
     * scan. Returns normalized number -> assigned name for successful updates.
     */
    fun renameFrequentContacts(rawNumbers: List<String>): Map<String, String> {
        val targets = rawNumbers.map(normalizer::normalize).filter(String::isNotBlank).distinct()
        if (targets.isEmpty()) return emptyMap()
        val targetSet = targets.toHashSet()
        val rawIdsByNumber = mutableMapOf<String, MutableSet<Long>>()
        val existingNameByNumber = mutableMapOf<String, String>()
        val usedSequenceNumbers = mutableSetOf<Int>()

        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            ),
            null,
            null,
            null
        )?.use { cursor ->
            val numberIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val rawIdIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID)
            val nameIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val normalized = normalizer.normalize(cursor.getString(numberIndex))
                val name = cursor.getString(nameIndex).orEmpty().trim()
                generatedFrequentName.matchEntire(name)?.groupValues?.getOrNull(1)
                    ?.toIntOrNull()?.let(usedSequenceNumbers::add)
                if (normalized in targetSet) {
                    rawIdsByNumber.getOrPut(normalized, ::linkedSetOf).add(cursor.getLong(rawIdIndex))
                    if (generatedFrequentName.matches(name)) existingNameByNumber[normalized] = name
                }
            }
        }

        var nextSequence = 1
        fun allocateName(normalizedNumber: String): String {
            while (nextSequence in usedSequenceNumbers) nextSequence++
            val value = nextSequence++
            usedSequenceNumbers += value
            return generatedFrequentCustomerName(value, normalizedNumber)
        }

        val renamed = linkedMapOf<String, String>()
        targets.forEach { normalized ->
            val existingSequence = existingNameByNumber[normalized]
                ?.let(generatedFrequentName::matchEntire)
                ?.groupValues?.getOrNull(1)?.toIntOrNull()
            val name = existingSequence
                ?.let { generatedFrequentCustomerName(it, normalized) }
                ?: allocateName(normalized)
            var updated = false
            rawIdsByNumber[normalized].orEmpty().forEach { rawContactId ->
                runCatching {
                    val operation = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(
                            "${ContactsContract.Data.RAW_CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                            arrayOf(rawContactId.toString(), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        )
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                        .build()
                    val result = context.contentResolver.applyBatch(ContactsContract.AUTHORITY, arrayListOf(operation))
                    if (result.firstOrNull()?.count?.let { it > 0 } == true) updated = true
                }
            }
            if (updated) renamed[normalized] = name
        }
        return renamed
    }
}
