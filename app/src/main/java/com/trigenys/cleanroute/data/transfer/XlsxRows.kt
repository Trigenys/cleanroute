package com.trigenys.cleanroute.data.transfer

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile

internal object XlsxRows {
    fun readFirstSheet(
        file: File,
        onRow: (rowNumber: Int, values: List<String>) -> Unit
    ) {
        ZipFile(file).use { zip ->
            val sharedStrings = zip.getEntry("xl/sharedStrings.xml")
                ?.let { entry -> zip.getInputStream(entry).use(::readSharedStrings) }
                .orEmpty()
            val sheetPath = resolveFirstSheetPath(zip)
            val sheetEntry = zip.getEntry(sheetPath)
                ?: error("La première feuille Excel est introuvable.")

            zip.getInputStream(sheetEntry).use { input ->
                readSheet(input, sharedStrings, onRow)
            }
        }
    }

    private fun resolveFirstSheetPath(zip: ZipFile): String {
        val workbookEntry = zip.getEntry("xl/workbook.xml")
            ?: return "xl/worksheets/sheet1.xml"
        val relationshipId = zip.getInputStream(workbookEntry).use { input ->
            val parser = Xml.newPullParser().apply {
                setInput(input, "UTF-8")
            }
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG && parser.name == "sheet") {
                    return@use parser.attributeByLocalName("id")
                }
                parser.next()
            }
            null
        } ?: return "xl/worksheets/sheet1.xml"

        val relsEntry = zip.getEntry("xl/_rels/workbook.xml.rels")
            ?: return "xl/worksheets/sheet1.xml"
        val target = zip.getInputStream(relsEntry).use { input ->
            val parser = Xml.newPullParser().apply {
                setInput(input, "UTF-8")
            }
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                if (
                    parser.eventType == XmlPullParser.START_TAG &&
                    parser.name == "Relationship" &&
                    parser.attributeByLocalName("Id") == relationshipId
                ) {
                    return@use parser.attributeByLocalName("Target")
                }
                parser.next()
            }
            null
        } ?: return "xl/worksheets/sheet1.xml"

        val normalized = target.replace('\\', '/').removePrefix("/")
        return if (normalized.startsWith("xl/")) normalized else "xl/$normalized"
    }

    private fun readSharedStrings(input: InputStream): List<String> {
        val values = mutableListOf<String>()
        val parser = Xml.newPullParser().apply {
            setInput(input, "UTF-8")
        }
        var inItem = false
        var current = StringBuilder()

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when {
                parser.eventType == XmlPullParser.START_TAG && parser.name == "si" -> {
                    inItem = true
                    current = StringBuilder()
                }
                parser.eventType == XmlPullParser.START_TAG && parser.name == "t" && inItem -> {
                    current.append(parser.nextText())
                }
                parser.eventType == XmlPullParser.END_TAG && parser.name == "si" -> {
                    values += current.toString()
                    inItem = false
                }
            }
            parser.next()
        }
        return values
    }

    private fun readSheet(
        input: InputStream,
        sharedStrings: List<String>,
        onRow: (rowNumber: Int, values: List<String>) -> Unit
    ) {
        val parser = Xml.newPullParser().apply {
            setInput(input, "UTF-8")
        }

        var rowNumber = 0
        var cells = mutableMapOf<Int, String>()
        var cellReference: String? = null
        var cellType: String? = null
        var rawValue: String? = null
        var inlineValue: String? = null

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when {
                parser.eventType == XmlPullParser.START_TAG && parser.name == "row" -> {
                    rowNumber = parser.attributeByLocalName("r")?.toIntOrNull()
                        ?: (rowNumber + 1)
                    cells = mutableMapOf()
                }
                parser.eventType == XmlPullParser.START_TAG && parser.name == "c" -> {
                    cellReference = parser.attributeByLocalName("r")
                    cellType = parser.attributeByLocalName("t")
                    rawValue = null
                    inlineValue = null
                }
                parser.eventType == XmlPullParser.START_TAG && parser.name == "v" -> {
                    rawValue = parser.nextText()
                }
                parser.eventType == XmlPullParser.START_TAG && parser.name == "t" -> {
                    inlineValue = inlineValue.orEmpty() + parser.nextText()
                }
                parser.eventType == XmlPullParser.END_TAG && parser.name == "c" -> {
                    val index = cellReference?.let(::columnIndexFromReference)
                        ?: cells.size
                    val value = when (cellType) {
                        "s" -> rawValue?.toIntOrNull()?.let(sharedStrings::getOrNull).orEmpty()
                        "inlineStr", "str" -> inlineValue ?: rawValue.orEmpty()
                        "b" -> if (rawValue == "1") "TRUE" else "FALSE"
                        else -> rawValue.orEmpty()
                    }
                    cells[index] = value
                }
                parser.eventType == XmlPullParser.END_TAG && parser.name == "row" -> {
                    if (cells.isNotEmpty()) {
                        val maxIndex = cells.keys.maxOrNull() ?: 0
                        val values = (0..maxIndex).map { cells[it].orEmpty() }
                        onRow(rowNumber, values)
                    }
                }
            }
            parser.next()
        }
    }

    private fun columnIndexFromReference(reference: String): Int {
        var result = 0
        reference.takeWhile(Char::isLetter).forEach { char ->
            result = result * 26 + (char.uppercaseChar() - 'A' + 1)
        }
        return (result - 1).coerceAtLeast(0)
    }

    private fun XmlPullParser.attributeByLocalName(name: String): String? {
        for (index in 0 until attributeCount) {
            if (getAttributeName(index) == name) {
                return getAttributeValue(index)
            }
        }
        return null
    }
}
