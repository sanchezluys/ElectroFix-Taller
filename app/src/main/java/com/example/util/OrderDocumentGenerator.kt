package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Precision
import coil.size.Scale
import com.example.model.APP_WATERMARK
import com.example.model.RepairOrder
import com.example.model.RepairStatus
import com.example.model.WorkshopSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object OrderDocumentGenerator {

    /**
     * Generates a high-quality PDF Intake / Service Ticket document.
     */
    fun generateOrderPdf(
        context: Context,
        order: RepairOrder,
        settings: WorkshopSettings
    ): File? {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 standard width
            val pageHeight = 842 // A4 standard height
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 16f
                color = Color.rgb(11, 87, 208) // Tech blue
            }
            val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textSize = 10f
                color = Color.DKGRAY
            }
            val headerBoxPaint = Paint().apply {
                color = Color.rgb(240, 244, 248)
                style = Paint.Style.FILL
            }
            val borderPaint = Paint().apply {
                color = Color.rgb(220, 226, 235)
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 9.5f
                color = Color.rgb(27, 27, 31)
            }
            val boldTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 9.5f
                color = Color.rgb(27, 27, 31)
            }
            val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                textSize = 8.5f
                color = Color.rgb(120, 130, 145)
            }

            // Draw Top Header Banner
            var yOffset = 30f
            val margin = 32f
            val contentWidth = pageWidth - (margin * 2)

            // Draw Workshop Logo if available
            var logoDrawn = false
            settings.logoUri?.let { uriStr ->
                try {
                    val logoBitmap = loadBitmapFromUri(context, uriStr, 60, 60)
                    if (logoBitmap != null) {
                        canvas.drawBitmap(logoBitmap, margin, yOffset, paint)
                        logoDrawn = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val textStartX = if (logoDrawn) margin + 70f else margin

            // Workshop Header Details
            val shopName = settings.workshopName.ifBlank { "ELECTROFIX TALLER" }
            canvas.drawText(shopName, textStartX, yOffset + 16f, titlePaint)
            
            var infoY = yOffset + 30f
            if (settings.workshopAddress.isNotBlank()) {
                canvas.drawText("Dirección: ${settings.workshopAddress}", textStartX, infoY, subtitlePaint)
                infoY += 13f
            }
            if (settings.workshopPhone.isNotBlank()) {
                canvas.drawText("Teléfono / WhatsApp: ${settings.workshopPhone}", textStartX, infoY, subtitlePaint)
                infoY += 13f
            }

            // Ticket Badge on top right
            val ticketBoxWidth = 160f
            val ticketBoxHeight = 50f
            val ticketBoxX = pageWidth - margin - ticketBoxWidth
            canvas.drawRoundRect(
                RectF(ticketBoxX, yOffset, ticketBoxX + ticketBoxWidth, yOffset + ticketBoxHeight),
                8f, 8f, headerBoxPaint
            )
            canvas.drawRoundRect(
                RectF(ticketBoxX, yOffset, ticketBoxX + ticketBoxWidth, yOffset + ticketBoxHeight),
                8f, 8f, borderPaint
            )
            boldTextPaint.textSize = 12f
            canvas.drawText("ORDEN DE INGRESO", ticketBoxX + 12f, yOffset + 20f, boldTextPaint)
            boldTextPaint.textSize = 14f
            boldTextPaint.color = Color.rgb(11, 87, 208)
            canvas.drawText("N° ${order.displayOrderNumber}", ticketBoxX + 12f, yOffset + 40f, boldTextPaint)
            boldTextPaint.textSize = 9.5f
            boldTextPaint.color = Color.rgb(27, 27, 31)

            yOffset += 75f

            // Section 1: Customer & Date Info Box
            val box1Height = 62f
            canvas.drawRoundRect(
                RectF(margin, yOffset, margin + contentWidth, yOffset + box1Height),
                6f, 6f, headerBoxPaint
            )
            canvas.drawRoundRect(
                RectF(margin, yOffset, margin + contentWidth, yOffset + box1Height),
                6f, 6f, borderPaint
            )

            val col1X = margin + 12f
            val col2X = margin + (contentWidth / 2) + 12f

            canvas.drawText("CLIENTE:", col1X, yOffset + 18f, boldTextPaint)
            canvas.drawText(order.clientName, col1X + 55f, yOffset + 18f, textPaint)

            canvas.drawText("TELÉFONO:", col1X, yOffset + 35f, boldTextPaint)
            canvas.drawText(order.clientPhone, col1X + 58f, yOffset + 35f, textPaint)

            if (order.clientEmail.isNotBlank()) {
                canvas.drawText("EMAIL:", col1X, yOffset + 52f, boldTextPaint)
                canvas.drawText(order.clientEmail, col1X + 42f, yOffset + 52f, textPaint)
            }

            canvas.drawText("FECHA INGRESO:", col2X, yOffset + 18f, boldTextPaint)
            canvas.drawText(order.formattedEntryDate, col2X + 85f, yOffset + 18f, textPaint)

            canvas.drawText("ESTADO ACTUAL:", col2X, yOffset + 35f, boldTextPaint)
            canvas.drawText(order.status.label, col2X + 85f, yOffset + 35f, textPaint)

            yOffset += box1Height + 14f

            // Section 2: Equipment & Diagnosis Details
            boldTextPaint.textSize = 11f
            canvas.drawText("DATOS DEL EQUIPO Y SERVICIO", margin, yOffset, boldTextPaint)
            boldTextPaint.textSize = 9.5f
            yOffset += 8f

            val box2Height = 135f
            canvas.drawRoundRect(
                RectF(margin, yOffset, margin + contentWidth, yOffset + box2Height),
                6f, 6f, Color.WHITE.let { Paint().apply { color = it } }
            )
            canvas.drawRoundRect(
                RectF(margin, yOffset, margin + contentWidth, yOffset + box2Height),
                6f, 6f, borderPaint
            )

            var rowY = yOffset + 20f
            canvas.drawText("Tipo de Equipo:", col1X, rowY, boldTextPaint)
            canvas.drawText(order.deviceCategory.title, col1X + 85f, rowY, textPaint)

            canvas.drawText("Marca y Modelo:", col2X, rowY, boldTextPaint)
            canvas.drawText("${order.deviceBrand} ${order.deviceModel}", col2X + 85f, rowY, textPaint)

            rowY += 22f
            canvas.drawText("N° Serie / IMEI:", col1X, rowY, boldTextPaint)
            canvas.drawText(if (order.serialNumber.isNotBlank()) order.serialNumber else "No especificado", col1X + 85f, rowY, textPaint)

            canvas.drawText("Accesorios:", col2X, rowY, boldTextPaint)
            canvas.drawText(if (order.accessoriesIncluded.isNotBlank()) order.accessoriesIncluded else "Sin accesorios", col2X + 85f, rowY, textPaint)

            rowY += 24f
            canvas.drawLine(margin + 8f, rowY - 6f, margin + contentWidth - 8f, rowY - 6f, borderPaint)

            canvas.drawText("Falla Reportada:", col1X, rowY + 8f, boldTextPaint)
            val issueLines = wrapText(order.reportedIssue, 80)
            var issueY = rowY + 8f
            for (line in issueLines.take(2)) {
                canvas.drawText(line, col1X + 85f, issueY, textPaint)
                issueY += 13f
            }

            rowY += 36f
            if (order.technicalDiagnosis.isNotBlank()) {
                canvas.drawText("Diagnóstico:", col1X, rowY, boldTextPaint)
                val diagLines = wrapText(order.technicalDiagnosis, 80)
                var diagY = rowY
                for (line in diagLines.take(2)) {
                    canvas.drawText(line, col1X + 85f, diagY, textPaint)
                    diagY += 13f
                }
            }

            yOffset += box2Height + 14f

            // Section 3: Financial Summary
            boldTextPaint.textSize = 11f
            canvas.drawText("RESUMEN ECONÓMICO", margin, yOffset, boldTextPaint)
            boldTextPaint.textSize = 9.5f
            yOffset += 8f

            val box3Height = 48f
            canvas.drawRoundRect(
                RectF(margin, yOffset, margin + contentWidth, yOffset + box3Height),
                6f, 6f, headerBoxPaint
            )
            canvas.drawRoundRect(
                RectF(margin, yOffset, margin + contentWidth, yOffset + box3Height),
                6f, 6f, borderPaint
            )

            val finCol1 = margin + 12f
            val finCol2 = margin + (contentWidth * 0.35f)
            val finCol3 = margin + (contentWidth * 0.70f)

            canvas.drawText("Total Estimado:", finCol1, yOffset + 20f, boldTextPaint)
            boldTextPaint.textSize = 11f
            boldTextPaint.color = Color.rgb(11, 87, 208)
            canvas.drawText(order.getFormattedTotal(settings), finCol1, yOffset + 38f, boldTextPaint)
            boldTextPaint.textSize = 9.5f
            boldTextPaint.color = Color.rgb(27, 27, 31)

            canvas.drawText("Abono Inicial:", finCol2, yOffset + 20f, boldTextPaint)
            textPaint.color = Color.rgb(22, 101, 52)
            canvas.drawText(order.getFormattedDeposit(settings), finCol2, yOffset + 38f, textPaint)
            textPaint.color = Color.rgb(27, 27, 31)

            canvas.drawText("Saldo Pendiente:", finCol3, yOffset + 20f, boldTextPaint)
            boldTextPaint.textSize = 11f
            boldTextPaint.color = if (order.balanceDue > 0) Color.rgb(185, 28, 28) else Color.rgb(22, 101, 52)
            canvas.drawText(order.getFormattedBalanceDue(settings), finCol3, yOffset + 38f, boldTextPaint)
            boldTextPaint.textSize = 9.5f
            boldTextPaint.color = Color.rgb(27, 27, 31)

            yOffset += box3Height + 14f

            // Section 4: Order Photos (Up to 5 thumbnails if available)
            if (order.photos.isNotEmpty()) {
                boldTextPaint.textSize = 11f
                canvas.drawText("REGISTRO FOTOGRÁFICO DEL INGRESO (${order.photos.size})", margin, yOffset, boldTextPaint)
                boldTextPaint.textSize = 9.5f
                yOffset += 8f

                val photoBoxHeight = 85f
                val photoSize = 75f
                val spacing = 12f

                for ((idx, photoUri) in order.photos.take(5).withIndex()) {
                    val photoX = margin + (idx * (photoSize + spacing))
                    try {
                        val bitmap = loadBitmapFromUri(context, photoUri, photoSize.toInt(), photoSize.toInt())
                        if (bitmap != null) {
                            canvas.drawBitmap(bitmap, photoX, yOffset, paint)
                            canvas.drawRect(photoX, yOffset, photoX + photoSize, yOffset + photoSize, borderPaint)
                        } else {
                            canvas.drawRect(photoX, yOffset, photoX + photoSize, yOffset + photoSize, headerBoxPaint)
                            canvas.drawRect(photoX, yOffset, photoX + photoSize, yOffset + photoSize, borderPaint)
                            canvas.drawText("Foto ${idx + 1}", photoX + 15f, yOffset + 40f, subtitlePaint)
                        }
                    } catch (e: Exception) {
                        canvas.drawRect(photoX, yOffset, photoX + photoSize, yOffset + photoSize, headerBoxPaint)
                    }
                }
                yOffset += photoBoxHeight + 10f
            }

            // Section 5: Warranty & Terms Policy
            if (settings.warrantyPolicy.isNotBlank()) {
                boldTextPaint.textSize = 10f
                canvas.drawText("CONDICIONES DEL SERVICIO Y POLÍTICA DE GARANTÍA", margin, yOffset, boldTextPaint)
                boldTextPaint.textSize = 9.5f
                yOffset += 6f

                val policyLines = wrapText(settings.warrantyPolicy, 95)
                var polY = yOffset + 10f
                for (line in policyLines.take(3)) {
                    canvas.drawText(line, margin, polY, subtitlePaint)
                    polY += 11f
                }
                yOffset = polY + 12f
            }

            // Signatures row
            val sigLineY = pageHeight - 55f
            val sigWidth = 160f

            canvas.drawLine(margin, sigLineY, margin + sigWidth, sigLineY, borderPaint)
            canvas.drawText("Firma del Cliente", margin + 35f, sigLineY + 14f, subtitlePaint)

            canvas.drawLine(pageWidth - margin - sigWidth, sigLineY, pageWidth - margin, sigLineY, borderPaint)
            canvas.drawText("Técnico / Responsable", pageWidth - margin - sigWidth + 25f, sigLineY + 14f, subtitlePaint)

            // MANDATORY WATERMARK ON BOTTOM LEFT
            val watermarkY = pageHeight - 16f
            canvas.drawText(APP_WATERMARK, margin, watermarkY, watermarkPaint)

            pdfDocument.finishPage(page)

            // Save PDF File
            val dir = File(context.cacheDir, "documents")
            if (!dir.exists()) dir.mkdirs()
            val pdfFile = File(dir, "Ingreso_${order.displayOrderNumber}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()

            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Generates a graphical Image (PNG) receipt that can be easily shared on WhatsApp / Messaging.
     */
    fun generateOrderImage(
        context: Context,
        order: RepairOrder,
        settings: WorkshopSettings
    ): File? {
        try {
            val width = 900
            val height = 1250
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Background
            canvas.drawColor(Color.rgb(248, 249, 252))

            val margin = 40f
            val contentWidth = width - (margin * 2)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(220, 226, 235)
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 28f
                color = Color.rgb(11, 87, 208)
            }
            val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 18f
                color = Color.DKGRAY
            }
            val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 20f
                color = Color.rgb(27, 27, 31)
            }
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 20f
                color = Color.rgb(40, 43, 48)
            }
            val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                textSize = 16f
                color = Color.rgb(100, 115, 130)
            }

            // Main Card Container
            val cardRect = RectF(margin, margin, width - margin, height - margin)
            canvas.drawRoundRect(cardRect, 20f, 20f, cardPaint)
            canvas.drawRoundRect(cardRect, 20f, 20f, cardBorderPaint)

            var y = margin + 40f
            val innerMargin = margin + 30f

            // Logo & Header
            var logoDrawn = false
            settings.logoUri?.let { uriStr ->
                try {
                    val logoBitmap = loadBitmapFromUri(context, uriStr, 100, 100)
                    if (logoBitmap != null) {
                        canvas.drawBitmap(logoBitmap, innerMargin, y, paint)
                        logoDrawn = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val textX = if (logoDrawn) innerMargin + 120f else innerMargin
            canvas.drawText(settings.workshopName.ifBlank { "ELECTROFIX TALLER" }, textX, y + 32f, titlePaint)
            if (settings.workshopAddress.isNotBlank()) {
                canvas.drawText("📍 ${settings.workshopAddress}", textX, y + 62f, subtitlePaint)
            }
            if (settings.workshopPhone.isNotBlank()) {
                canvas.drawText("📞 ${settings.workshopPhone}", textX, y + 90f, subtitlePaint)
            }

            y += 120f

            // Ticket Banner
            val ticketBannerPaint = Paint().apply {
                color = Color.rgb(232, 240, 254)
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(
                RectF(innerMargin, y, width - innerMargin, y + 60f),
                12f, 12f, ticketBannerPaint
            )
            boldPaint.color = Color.rgb(11, 87, 208)
            boldPaint.textSize = 22f
            canvas.drawText("ORDEN DE SERVICIO TÉCNICO: ${order.displayOrderNumber}", innerMargin + 20f, y + 38f, boldPaint)
            boldPaint.color = Color.rgb(27, 27, 31)
            boldPaint.textSize = 20f

            y += 85f

            // Client & Date
            canvas.drawText("👤 Cliente: ${order.clientName}", innerMargin, y, textPaint)
            y += 32f
            canvas.drawText("📱 Teléfono: ${order.clientPhone}", innerMargin, y, textPaint)
            y += 32f
            canvas.drawText("📅 Fecha: ${order.formattedEntryDate}", innerMargin, y, textPaint)
            y += 32f
            canvas.drawText("🔄 Estado: ${order.status.label}", innerMargin, y, textPaint)

            y += 35f
            canvas.drawLine(innerMargin, y, width - innerMargin, y, cardBorderPaint)
            y += 30f

            // Equipment Details
            boldPaint.textSize = 22f
            canvas.drawText("DATOS DEL EQUIPO", innerMargin, y, boldPaint)
            boldPaint.textSize = 20f
            y += 32f

            canvas.drawText("💻 Dispositivo: ${order.deviceCategory.title}", innerMargin, y, textPaint)
            y += 30f
            canvas.drawText("🏷️ Marca/Modelo: ${order.deviceBrand} ${order.deviceModel}", innerMargin, y, textPaint)
            y += 30f
            if (order.serialNumber.isNotBlank()) {
                canvas.drawText("🔢 Serial/IMEI: ${order.serialNumber}", innerMargin, y, textPaint)
                y += 30f
            }
            if (order.accessoriesIncluded.isNotBlank()) {
                canvas.drawText("🔌 Accesorios: ${order.accessoriesIncluded}", innerMargin, y, textPaint)
                y += 30f
            }

            y += 10f
            canvas.drawText("⚠️ Falla Reportada:", innerMargin, y, boldPaint)
            y += 28f
            for (line in wrapText(order.reportedIssue, 55).take(2)) {
                canvas.drawText("   $line", innerMargin, y, textPaint)
                y += 28f
            }

            if (order.technicalDiagnosis.isNotBlank()) {
                y += 6f
                canvas.drawText("🔬 Diagnóstico:", innerMargin, y, boldPaint)
                y += 28f
                for (line in wrapText(order.technicalDiagnosis, 55).take(2)) {
                    canvas.drawText("   $line", innerMargin, y, textPaint)
                    y += 28f
                }
            }

            if (order.workPerformed.isNotBlank()) {
                y += 6f
                canvas.drawText("✅ Trabajo Realizado:", innerMargin, y, boldPaint)
                y += 28f
                for (line in wrapText(order.workPerformed, 55).take(2)) {
                    canvas.drawText("   $line", innerMargin, y, textPaint)
                    y += 28f
                }
            }

            y += 20f
            canvas.drawLine(innerMargin, y, width - innerMargin, y, cardBorderPaint)
            y += 30f

            // Budget Info
            boldPaint.textSize = 22f
            canvas.drawText("PRESUPUESTO", innerMargin, y, boldPaint)
            boldPaint.textSize = 20f
            y += 34f

            canvas.drawText("Total Estimado: ${order.getFormattedTotal(settings)}", innerMargin, y, boldPaint)
            y += 30f
            if (order.depositPaid > 0) {
                canvas.drawText("Abono Inicial: ${order.getFormattedDeposit(settings)}", innerMargin, y, textPaint)
                y += 30f
                canvas.drawText("Saldo Pendiente: ${order.getFormattedBalanceDue(settings)}", innerMargin, y, boldPaint)
                y += 30f
            }

            // Attached Photos if any
            if (order.photos.isNotEmpty()) {
                y += 10f
                canvas.drawText("📸 Fotos del Ingreso (${order.photos.size}):", innerMargin, y, boldPaint)
                y += 30f
                val photoSize = 100f
                val spacing = 16f
                for ((idx, photoUri) in order.photos.take(4).withIndex()) {
                    val px = innerMargin + (idx * (photoSize + spacing))
                    try {
                        val bmp = loadBitmapFromUri(context, photoUri, photoSize.toInt(), photoSize.toInt())
                        if (bmp != null) {
                            canvas.drawBitmap(bmp, px, y, paint)
                            canvas.drawRect(px, y, px + photoSize, y + photoSize, cardBorderPaint)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                y += photoSize + 20f
            }

            // WATERMARK ON BOTTOM LEFT OF IMAGE
            val watermarkY = height - margin - 25f
            canvas.drawText(APP_WATERMARK, innerMargin, watermarkY, watermarkPaint)

            // Save Image File
            val dir = File(context.cacheDir, "documents")
            if (!dir.exists()) dir.mkdirs()
            val imageFile = File(dir, "Comprobante_${order.displayOrderNumber}.png")
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, outputStream)
            outputStream.flush()
            outputStream.close()

            return imageFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Shares a generated file (PDF or Image) or plain text.
     */
    fun shareDocument(
        context: Context,
        file: File?,
        mimeType: String,
        fallbackText: String
    ) {
        try {
            if (file != null && file.exists()) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, fallbackText)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Compartir Comprobante"))
            } else {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, fallbackText)
                }
                context.startActivity(Intent.createChooser(intent, "Compartir Orden"))
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al compartir: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadBitmapFromUri(context: Context, uriString: String, targetWidth: Int, targetHeight: Int): Bitmap? {
        return try {
            val uri = Uri.parse(uriString)
            val requestData: Any = if (uri.scheme == null || uri.scheme == "file") {
                val path = uri.path ?: uriString
                File(path)
            } else {
                uri
            }

            val request = ImageRequest.Builder(context)
                .data(requestData)
                .size(targetWidth, targetHeight)
                .scale(Scale.FIT)
                .precision(Precision.INEXACT)
                .allowHardware(false)
                .build()

            val result = runBlocking(Dispatchers.IO) {
                context.imageLoader.execute(request)
            }

            if (result is SuccessResult) {
                (result.drawable as? BitmapDrawable)?.bitmap ?: result.drawable.toBitmap(targetWidth, targetHeight)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun wrapText(text: String, maxCharsPerLine: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            if (currentLine.length + word.length + 1 > maxCharsPerLine) {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder()
                }
            }
            if (currentLine.isNotEmpty()) currentLine.append(" ")
            currentLine.append(word)
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }
}
