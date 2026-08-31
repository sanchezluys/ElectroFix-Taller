package com.example.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class AppCurrency(
    val code: String,
    val label: String,
    val symbol: String,
    val description: String,
    val exampleAmount: Double = 150.0
) {
    USD(
        code = "USD",
        label = "Dólar",
        symbol = "$",
        description = "Dólar estadounidense ($ USD)",
        exampleAmount = 150.0
    ),
    COP(
        code = "COP",
        label = "Pesos colombianos",
        symbol = "$",
        description = "Pesos colombianos ($ COP)",
        exampleAmount = 150000.0
    ),
    PEN(
        code = "PEN",
        label = "Soles",
        symbol = "S/",
        description = "Soles peruanos (S/ PEN)",
        exampleAmount = 150.0
    );

    fun format(
        amount: Double,
        useThousandSeparator: Boolean = true,
        thousandSeparatorChar: Char = if (this == COP) '.' else ','
    ): String {
        val decimalChar = if (thousandSeparatorChar == '.') ',' else '.'

        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = thousandSeparatorChar
            decimalSeparator = decimalChar
        }

        val pattern = when {
            !useThousandSeparator -> {
                if (this == COP || amount % 1.0 == 0.0) "#0" else "#0.00"
            }
            this == COP -> {
                "#,##0"
            }
            amount % 1.0 == 0.0 -> {
                "#,##0"
            }
            else -> {
                "#,##0.00"
            }
        }

        val formattedNum = DecimalFormat(pattern, symbols).format(amount)

        return when (this) {
            USD -> "$ $formattedNum"
            COP -> "$ $formattedNum COP"
            PEN -> "S/ $formattedNum"
        }
    }

    fun formatExact(
        amount: Double,
        useThousandSeparator: Boolean = true,
        thousandSeparatorChar: Char = if (this == COP) '.' else ','
    ): String {
        val decimalChar = if (thousandSeparatorChar == '.') ',' else '.'
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = thousandSeparatorChar
            decimalSeparator = decimalChar
        }
        val pattern = if (useThousandSeparator) "#,##0.00" else "#0.00"
        val formattedNum = DecimalFormat(pattern, symbols).format(amount)
        return when (this) {
            USD -> "$ $formattedNum"
            COP -> "$ $formattedNum COP"
            PEN -> "S/ $formattedNum"
        }
    }

    companion object {
        val DEFAULT = USD

        fun fromCode(code: String?): AppCurrency {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: DEFAULT
        }
    }
}
