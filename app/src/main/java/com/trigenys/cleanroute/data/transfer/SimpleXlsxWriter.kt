package com.trigenys.cleanroute.data.transfer

import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal object SimpleXlsxWriter {
    fun write(
        output: OutputStream,
        sheets: List<SpreadsheetSheet>
    ) {
        require(sheets.isNotEmpty()) { "At least one sheet is required" }
        ZipOutputStream(output.buffered()).use { zip ->
            zip.putText("[Content_Types].xml", contentTypes(sheets.size))
            zip.putText("_rels/.rels", rootRelationships())
            zip.putText("xl/workbook.xml", workbookXml(sheets))
            zip.putText("xl/_rels/workbook.xml.rels", workbookRelationships(sheets.size))

            sheets.forEachIndexed { index, sheet ->
                zip.putEntry("xl/worksheets/sheet${index + 1}.xml") { sink ->
                    sink.write(
                        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""" +
                            """<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>"""
                    )
                    var rowNumber = 1
                    sheet.rows.forEach { row ->
                        sink.write("""<row r="$rowNumber">""")
                        row.forEachIndexed { column, cell ->
                            val reference = columnName(column) + rowNumber
                            when (cell) {
                                is SpreadsheetCell.Text -> {
                                    sink.write(
                                        """<c r="$reference" t="inlineStr"><is><t xml:space="preserve">${escape(cell.value)}</t></is></c>"""
                                    )
                                }
                                is SpreadsheetCell.Number -> {
                                    sink.write(
                                        """<c r="$reference"><v>${cell.value}</v></c>"""
                                    )
                                }
                                SpreadsheetCell.Blank -> Unit
                            }
                        }
                        sink.write("</row>")
                        rowNumber += 1
                    }
                    sink.write("</sheetData></worksheet>")
                }
            }
        }
    }

    private fun contentTypes(sheetCount: Int): String = buildString {
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        append("""<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">""")
        append("""<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>""")
        append("""<Default Extension="xml" ContentType="application/xml"/>""")
        append("""<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>""")
        repeat(sheetCount) { index ->
            append(
                """<Override PartName="/xl/worksheets/sheet${index + 1}.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>"""
            )
        }
        append("</Types>")
    }

    private fun rootRelationships(): String =
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""" +
            """<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""" +
            """<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>""" +
            """</Relationships>"""

    private fun workbookXml(sheets: List<SpreadsheetSheet>): String = buildString {
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        append(
            """<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" """ +
                """xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets>"""
        )
        sheets.forEachIndexed { index, sheet ->
            append(
                """<sheet name="${escapeAttribute(sheet.name.take(31))}" sheetId="${index + 1}" r:id="rId${index + 1}"/>"""
            )
        }
        append("</sheets></workbook>")
    }

    private fun workbookRelationships(sheetCount: Int): String = buildString {
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        append("""<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""")
        repeat(sheetCount) { index ->
            append(
                """<Relationship Id="rId${index + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet${index + 1}.xml"/>"""
            )
        }
        append("</Relationships>")
    }

    private fun ZipOutputStream.putText(path: String, text: String) {
        putEntry(path) { sink -> sink.write(text) }
    }

    private inline fun ZipOutputStream.putEntry(
        path: String,
        block: (TextSink) -> Unit
    ) {
        putNextEntry(ZipEntry(path))
        block(TextSink(this))
        closeEntry()
    }

    private class TextSink(
        private val output: OutputStream
    ) {
        fun write(value: String) {
            output.write(value.toByteArray(StandardCharsets.UTF_8))
        }
    }

    private fun columnName(index: Int): String {
        var value = index + 1
        val result = StringBuilder()
        while (value > 0) {
            val remainder = (value - 1) % 26
            result.append(('A'.code + remainder).toChar())
            value = (value - 1) / 26
        }
        return result.reverse().toString()
    }

    private fun escape(value: String): String =
        value
            .filter { it == '\t' || it == '\n' || it == '\r' || it.code >= 0x20 }
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    private fun escapeAttribute(value: String): String =
        escape(value)
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
}
