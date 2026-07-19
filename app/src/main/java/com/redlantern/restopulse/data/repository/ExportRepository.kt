package com.redlantern.restopulse.data.repository

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.redlantern.restopulse.data.database.entities.CustomerEntity
import com.redlantern.restopulse.data.preferences.SettingsRepository
import com.redlantern.restopulse.models.ExportFormat
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class ExportRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val customers: CustomerRepository,
    private val settings: SettingsRepository
) {
    suspend fun export(format: ExportFormat, namePrefix: String = ""): Intent {
        val prefix = namePrefix.trim()
        val list = customers.observeCustomers().first()
            .filter { prefix.isBlank() || it.name.trimStart().startsWith(prefix, ignoreCase = true) }
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val suffix = prefix
            .replace(Regex("[^A-Za-z0-9_-]+"), "-")
            .trim('-')
            .take(40)
            .takeIf { it.isNotBlank() }
            ?.let { "-$it" }
            .orEmpty()
        val file = File(dir, "red-lantern-customers$suffix.${format.extension}")
        when (format) {
            ExportFormat.CSV -> file.writeText(list.toCsv(), Charsets.UTF_8)
            ExportFormat.EXCEL -> file.writeText(list.toExcelXml(), Charsets.UTF_8)
            ExportFormat.VCF -> file.writeText(list.toVcf(), Charsets.UTF_8)
            ExportFormat.PDF -> writePdf(file, list)
        }
        settings.setExportStatus("Exported ${list.size} contacts as ${format.extension.uppercase()}")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        return Intent(Intent.ACTION_SEND)
            .setType(format.mimeType)
            .putExtra(Intent.EXTRA_STREAM, uri)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    /** SpreadsheetML is a real Excel-readable workbook without a heavyweight XLS library. */
    private fun List<CustomerEntity>.toExcelXml(): String {
        val headers = listOf("Name", "Phone", "Normalized", "Tag", "Notes", "WhatsApp", "VIP", "Favorite", "Total Calls", "Address", "Location")
        fun cell(value: Any?): String =
            "<Cell><Data ss:Type=\"String\">${value.toString().xmlEscape()}</Data></Cell>"

        return buildString {
            appendLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
            appendLine("<?mso-application progid=\"Excel.Sheet\"?>")
            appendLine("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">")
            appendLine("<Worksheet ss:Name=\"Contacts\"><Table>")
            appendLine("<Row>${headers.joinToString("") { cell(it) }}</Row>")
            this@toExcelXml.forEach { customer ->
                val values = listOf(customer.name, customer.phoneNumber, customer.normalizedNumber, customer.customerTag, customer.notes, customer.whatsappAvailable, customer.vip, customer.favorite, customer.totalCalls, customer.address, customer.location)
                appendLine("<Row>${values.joinToString("") { cell(it) }}</Row>")
            }
            appendLine("</Table></Worksheet></Workbook>")
        }
    }

    private fun String.xmlEscape(): String = this
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    private fun List<CustomerEntity>.toCsv(): String = buildString {
        appendLine("Name,Phone,Normalized,Tag,Notes,WhatsApp,VIP,Favorite,Total Calls,Address,Location")
        this@toCsv.forEach { customer ->
            appendLine(listOf(customer.name, customer.phoneNumber, customer.normalizedNumber, customer.customerTag, customer.notes, customer.whatsappAvailable, customer.vip, customer.favorite, customer.totalCalls, customer.address, customer.location).joinToString(",") { value ->
                "\"${value.toString().replace("\"", "\"\"")}\""
            })
        }
    }

    private fun List<CustomerEntity>.toVcf(): String = buildString {
        this@toVcf.forEach { customer ->
            appendLine("BEGIN:VCARD")
            appendLine("VERSION:3.0")
            appendLine("FN:${customer.name.ifBlank { customer.phoneNumber }}")
            appendLine("TEL;TYPE=CELL:${customer.phoneNumber}")
            if (customer.address.isNotBlank()) appendLine("ADR;TYPE=WORK:;;${customer.address}")
            appendLine("NOTE:${customer.notes}")
            appendLine("END:VCARD")
        }
    }

    private fun writePdf(file: File, list: List<CustomerEntity>) {
        val document = PdfDocument()
        val paint = Paint().apply { textSize = 12f }
        var pageNumber = 1
        var y = 40f
        var page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
        page.canvas.drawText("Red Lantern Customer Report", 40f, y, paint)
        y += 28f
        list.forEach { customer ->
            if (y > 800f) {
                document.finishPage(page)
                pageNumber += 1
                y = 40f
                page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
            }
            page.canvas.drawText("${customer.name.ifBlank { "Customer" }}  ${customer.phoneNumber}  ${customer.customerTag}", 40f, y, paint)
            y += 20f
        }
        document.finishPage(page)
        file.outputStream().use(document::writeTo)
        document.close()
    }
}
