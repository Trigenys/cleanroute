package com.trigenys.cleanroute.data.transfer

import java.io.BufferedInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PushbackReader
import java.nio.charset.StandardCharsets

internal object CsvRows {
    fun read(
        input: InputStream,
        onRow: (rowNumber: Int, values: List<String>) -> Unit
    ) {
        val buffered = BufferedInputStream(input)
        val delimiter = detectDelimiter(buffered)
        val reader = PushbackReader(
            InputStreamReader(buffered, StandardCharsets.UTF_8),
            1
        )

        var rowNumber = 1
        val row = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false

        fun emitField() {
            row += field.toString()
            field.setLength(0)
        }

        fun emitRow() {
            emitField()
            if (row.any { it.isNotBlank() }) {
                onRow(rowNumber, row.toList())
            }
            row.clear()
            rowNumber += 1
        }

        while (true) {
            val code = reader.read()
            if (code == -1) break
            val char = code.toChar()

            if (inQuotes) {
                if (char == '"') {
                    val next = reader.read()
                    if (next == '"'.code) {
                        field.append('"')
                    } else {
                        inQuotes = false
                        if (next != -1) reader.unread(next)
                    }
                } else {
                    field.append(char)
                }
                continue
            }

            when (char) {
                '"' -> {
                    if (field.isEmpty()) {
                        inQuotes = true
                    } else {
                        field.append(char)
                    }
                }
                delimiter -> emitField()
                '\n' -> emitRow()
                '\r' -> Unit
                else -> field.append(char)
            }
        }

        if (field.isNotEmpty() || row.isNotEmpty()) {
            emitRow()
        }
    }

    private fun detectDelimiter(input: BufferedInputStream): Char {
        input.mark(4096)
        var commas = 0
        var semicolons = 0
        var inQuotes = false
        var count = 0

        while (count < 4096) {
            val code = input.read()
            if (code == -1) break
            val char = code.toChar()
            if (char == '\n' || char == '\r') break

            if (char == '"') {
                inQuotes = !inQuotes
            } else if (!inQuotes) {
                if (char == ',') commas += 1
                if (char == ';') semicolons += 1
            }
            count += 1
        }

        input.reset()
        return if (semicolons > commas) ';' else ','
    }
}
