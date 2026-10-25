package com.example.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.ReceiptData
import java.io.File
import java.io.FileOutputStream

object ReceiptShareHelper {

    fun generateReceiptBitmap(receipt: ReceiptData): Bitmap {
        val width = 720
        val lineCount = receipt.lines.size
        val hasNotes = !receipt.notes.isNullOrBlank()
        val calculatedHeight = 760 + (lineCount * 46) + (if (hasNotes) 80 else 0)

        val bitmap = Bitmap.createBitmap(width, calculatedHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. White Background
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 2. Decorative Top Blue Stripe (Electric Blue)
        paint.color = Color.parseColor("#2563EB")
        canvas.drawRect(0f, 0f, width.toFloat(), 14f, paint)

        var y = 60f

        // 3. Header Branding
        paint.color = Color.parseColor("#2563EB")
        paint.textSize = 34f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("CRISPFLOW", width / 2f, y, paint)

        y += 32f
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(receipt.subtitle, width / 2f, y, paint)

        y += 28f
        // Dashed Divider
        drawDashedLine(canvas, 40f, width - 40f, y)

        y += 40f
        // Receipt Title
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 26f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(receipt.title, width / 2f, y, paint)

        y += 30f
        // Badge & Receipt ID
        paint.textSize = 17f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.parseColor("#475569")
        canvas.drawText("No: ${receipt.receiptId} • ${receipt.badge}", width / 2f, y, paint)

        y += 26f
        // Timestamp
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 16f
        canvas.drawText(FormatUtils.formatDateTime(receipt.timestamp), width / 2f, y, paint)

        y += 24f
        drawDashedLine(canvas, 40f, width - 40f, y)

        // 4. Primary Amount Box (Light Blue / Clean Slate)
        y += 24f
        val boxHeight = 100f
        val boxRect = RectF(40f, y, width - 40f, y + boxHeight)
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRoundRect(boxRect, 18f, 18f, paint)

        paint.color = Color.parseColor("#64748B")
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(receipt.primaryLabel, width / 2f, y + 36f, paint)

        paint.textSize = 34f
        paint.color = if (receipt.isPositive) Color.parseColor("#16A34A") else Color.parseColor("#DC2626")
        canvas.drawText(receipt.primaryValue, width / 2f, y + 80f, paint)

        y += boxHeight + 24f
        drawDashedLine(canvas, 40f, width - 40f, y)

        // 5. Line Items (Key-Value)
        y += 36f
        val leftX = 48f
        val rightX = width - 48f

        for (line in receipt.lines) {
            paint.textAlign = Paint.Align.LEFT
            paint.textSize = 19f
            paint.typeface = if (line.isBold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = if (line.isBold) Color.parseColor("#0F172A") else Color.parseColor("#475569")
            canvas.drawText(line.label, leftX, y, paint)

            paint.textAlign = Paint.Align.RIGHT
            paint.typeface = if (line.isBold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = when {
                line.isHighlight -> Color.parseColor("#16A34A")
                line.isNegative -> Color.parseColor("#DC2626")
                line.isBold -> Color.parseColor("#0F172A")
                else -> Color.parseColor("#334155")
            }
            canvas.drawText(line.value, rightX, y, paint)

            y += 44f
        }

        // 6. Notes (if any)
        if (hasNotes) {
            y += 4f
            drawDashedLine(canvas, 40f, width - 40f, y)
            y += 32f

            paint.textAlign = Paint.Align.LEFT
            paint.textSize = 15f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = Color.parseColor("#64748B")
            canvas.drawText("CATATAN:", leftX, y, paint)

            y += 24f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            paint.color = Color.parseColor("#334155")
            paint.textSize = 17f
            canvas.drawText(receipt.notes ?: "", leftX, y, paint)
            y += 20f
        }

        y += 8f
        drawDashedLine(canvas, 40f, width - 40f, y)

        // 7. Simulated Thermal Barcode Graphic
        y += 34f
        drawSimulatedBarcode(canvas, width / 2f, y)

        y += 56f
        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(receipt.footerMessage, width / 2f, y, paint)

        // 8. Sawtooth Thermal Edge at Bottom
        val bottomY = calculatedHeight.toFloat()
        drawSawtoothEdge(canvas, width, bottomY)

        return bitmap
    }

    private fun drawDashedLine(canvas: Canvas, startX: Float, endX: Float, y: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(10f, 8f), 0f)
        }
        val path = Path().apply {
            moveTo(startX, y)
            lineTo(endX, y)
        }
        canvas.drawPath(path, paint)
    }

    private fun drawSimulatedBarcode(canvas: Canvas, centerX: Float, startY: Float) {
        val paint = Paint().apply {
            color = Color.parseColor("#1E293B")
            style = Paint.Style.FILL
        }

        // Draw 32 vertical bars with varying thicknesses
        val barPattern = intArrayOf(
            3, 1, 4, 2, 1, 3, 2, 4, 1, 2, 4, 3, 1, 2, 3, 1,
            4, 2, 1, 3, 2, 4, 1, 3, 2, 1, 4, 2, 3, 1, 2, 4
        )
        val barHeight = 40f
        var currentX = centerX - 190f

        for (w in barPattern) {
            val barWidth = (w * 2.2f)
            canvas.drawRect(currentX, startY, currentX + barWidth, startY + barHeight, paint)
            currentX += barWidth + 4.5f
        }
    }

    private fun drawSawtoothEdge(canvas: Canvas, width: Int, bottomY: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.FILL
        }
        val toothWidth = 24f
        val toothHeight = 12f
        val path = Path()

        var x = 0f
        path.moveTo(0f, bottomY)
        while (x < width) {
            path.lineTo(x + toothWidth / 2f, bottomY - toothHeight)
            path.lineTo(x + toothWidth, bottomY)
            x += toothWidth
        }
        path.lineTo(width.toFloat(), bottomY)
        path.close()
        canvas.drawPath(path, paint)
    }

    fun shareReceiptImage(context: Context, receipt: ReceiptData) {
        try {
            val bitmap = generateReceiptBitmap(receipt)
            val cacheReceiptsDir = File(context.cacheDir, "receipts")
            if (!cacheReceiptsDir.exists()) {
                cacheReceiptsDir.mkdirs()
            }

            val file = File(cacheReceiptsDir, "struk_${receipt.receiptId}.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Struk: ${receipt.title}")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "📄 ${receipt.title} (${receipt.receiptId})\n" +
                            "${receipt.primaryLabel}: ${receipt.primaryValue}\n" +
                            "Waktu: ${FormatUtils.formatDateTime(receipt.timestamp)}\n" +
                            "CrispFlow System"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Bagikan Struk Transaksi")
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membagikan struk: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
