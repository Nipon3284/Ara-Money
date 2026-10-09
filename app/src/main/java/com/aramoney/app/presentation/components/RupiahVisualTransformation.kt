package com.aramoney.app.presentation.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.aramoney.app.util.CurrencyFormatter

/**
 * Menampilkan input digit mentah ("25000") sebagai format Rupiah ("25.000") saat diketik,
 * tanpa mengubah nilai state sebenarnya. Kursor selalu ditempatkan sesuai posisi digit.
 */
object RupiahVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) return TransformedText(text, OffsetMapping.Identity)
        val formatted = CurrencyFormatter.formatNumber(raw.toLongOrNull() ?: 0L)

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // Hitung posisi setelah [offset] digit pertama dalam string berformat
                var digits = 0
                formatted.forEachIndexed { index, c ->
                    if (digits == offset) return index
                    if (c.isDigit()) digits++
                }
                return formatted.length
            }

            override fun transformedToOriginal(offset: Int): Int =
                formatted.take(offset.coerceAtMost(formatted.length)).count { it.isDigit() }.coerceAtMost(raw.length)
        }
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}
