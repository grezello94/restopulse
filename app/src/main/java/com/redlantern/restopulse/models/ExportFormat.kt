package com.redlantern.restopulse.models

enum class ExportFormat(val extension: String, val mimeType: String) {
    CSV("csv", "text/csv"),
    EXCEL("xls", "application/vnd.ms-excel"),
    VCF("vcf", "text/vcard"),
    PDF("pdf", "application/pdf")
}
