package com.example.model

const val APP_WATERMARK = "Software EletroFix Taller by sanchezluys@gmail.com"

data class WorkshopSettings(
    val workshopName: String = "ElectroFix Taller",
    val workshopPhone: String = "",
    val workshopAddress: String = "",
    val warrantyPolicy: String = "La garantía cubre exclusivamente la mano de obra técnica y los repuestos especificados en esta orden durante el período acordado. No cubre daños por humedad, sobrevoltaje eléctrico, golpes o intervención por terceros.",
    val logoUri: String? = null,
    val isDataLocked: Boolean = false,
    val currency: AppCurrency = AppCurrency.DEFAULT,
    val useThousandSeparator: Boolean = true,
    val thousandSeparatorChar: Char = ',',
    val isFirstLaunchCompleted: Boolean = false
) {
    fun formatMoney(amount: Double): String {
        return currency.format(
            amount = amount,
            useThousandSeparator = useThousandSeparator,
            thousandSeparatorChar = thousandSeparatorChar
        )
    }

    fun formatMoneyExact(amount: Double): String {
        return currency.formatExact(
            amount = amount,
            useThousandSeparator = useThousandSeparator,
            thousandSeparatorChar = thousandSeparatorChar
        )
    }
}
