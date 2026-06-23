package l192.aakarsh.pocketops.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import l192.aakarsh.pocketops.R
import java.io.File
import java.io.FileOutputStream

object ShareUtils {

    fun shareQrCode(
        context: Context,
        qrBitmap: Bitmap,
        name: String,
        upiId: String,
        amount: String,
        showUpiId: Boolean
    ) {
        val imageFile = generateShareableImage(context, qrBitmap, name, upiId, amount, showUpiId) ?: return
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share QR Code"))
    }

    private fun generateShareableImage(
        context: Context,
        qrBitmap: Bitmap,
        name: String,
        upiId: String,
        amount: String,
        showUpiId: Boolean
    ): File? {
        try {
            val width = 800
            val height = 1000
            val bitmap = createBitmap(width, height)
            val canvas = Canvas(bitmap)

            // Background
            canvas.drawColor("#f8f9fb".toColorInt())

            val textPaint = Paint().apply {
                color = Color.BLACK
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }

            // Define QR Code geometry (perfectly centered vertically)
            val qrSize = 520
            val qrLeft = (width - qrSize) / 2
            val qrTop = (height - qrSize) / 2
            val qrDestRect = Rect(qrLeft, qrTop, qrLeft + qrSize, qrTop + qrSize)
            canvas.drawBitmap(qrBitmap, null, qrDestRect, null)

            // Draw Payee Name above (if it exists)
            if (name.isNotBlank()) {
                textPaint.textSize = 36f
                textPaint.isFakeBoldText = false
                textPaint.color = "#4c566a".toColorInt()
                canvas.drawText(name, width / 2f, qrTop - 110f, textPaint)
            }

            // Draw "Scan to Pay" or "₹Amount" exactly above the QR code
            if (amount.isNotBlank()) {
                textPaint.textSize = 75f
                textPaint.isFakeBoldText = true
                textPaint.color = "#1a1a1a".toColorInt()
                canvas.drawText("₹$amount", width / 2f, qrTop - 35f, textPaint)
            } else {
                textPaint.textSize = 55f
                textPaint.isFakeBoldText = true
                textPaint.color = "#4c566a".toColorInt()
                canvas.drawText("Scan to Pay", width / 2f, qrTop - 35f, textPaint)
            }

            // Draw raw UPI ID directly below the QR code
            if (showUpiId) {
                textPaint.textSize = 32f
                textPaint.isFakeBoldText = false
                textPaint.color = "#7b88a1".toColorInt()
                canvas.drawText(upiId, width / 2f, qrTop + qrSize + 60f, textPaint)
            }

            // Draw footer centered at the bottom: [small logo] PocketOps
            val logoDrawable = context.getDrawable(R.drawable.ic_pocketops_tile)?.mutate()
            if (logoDrawable != null) {
                logoDrawable.setTint("#4c566a".toColorInt())
                val footerText = "PocketOps"
                val footerPaint = Paint().apply {
                    color = "#4c566a".toColorInt()
                    textSize = 34f
                    isAntiAlias = true
                    isFakeBoldText = true
                }
                val textWidth = footerPaint.measureText(footerText)
                val iconSize = 34
                val gap = 12
                val totalFooterWidth = iconSize + gap + textWidth
                val footerLeft = (width - totalFooterWidth) / 2f
                val footerY = height - 70f

                logoDrawable.setBounds(
                    footerLeft.toInt(),
                    (footerY - iconSize).toInt(),
                    (footerLeft + iconSize).toInt(),
                    footerY.toInt()
                )
                logoDrawable.draw(canvas)

                // Align baseline to middle of the icon
                canvas.drawText(footerText, footerLeft + iconSize + gap, footerY - 2f, footerPaint)
            }

            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, "share_qr.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
