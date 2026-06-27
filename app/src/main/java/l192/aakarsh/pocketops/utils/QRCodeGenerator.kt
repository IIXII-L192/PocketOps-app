package l192.aakarsh.pocketops.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import l192.aakarsh.pocketops.R

object QRCodeGenerator {
    @Throws(WriterException::class)
    fun generateQRCode(context: Context, text: String, width: Int, height: Int): Bitmap {
        val hints = HashMap<EncodeHintType, Any>().apply {
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
            put(EncodeHintType.MARGIN, 1)
        }
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(
            text,
            BarcodeFormat.QR_CODE,
            width,
            height,
            hints
        )
        val matrixWidth = bitMatrix.width
        val matrixHeight = bitMatrix.height

        val bitmap = Bitmap.createBitmap(matrixWidth, matrixHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Background
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }

        val dotSize = matrixWidth.toFloat() / bitMatrix.width.toFloat()

        for (y in 0 until matrixHeight) {
            for (x in 0 until matrixWidth) {
                if (bitMatrix[x, y]) {
                    val left = x * dotSize
                    val top = y * dotSize
                    val right = left + dotSize
                    val bottom = top + dotSize

                    // Draw solid square modules so QR lines are perfectly solid black
                    canvas.drawRect(left, top, right, bottom, paint)
                }
            }
        }

        // Draw the pocketops tile icon in the center
        val logoDrawable = ContextCompat.getDrawable(context, R.drawable.ic_pocketops_tile)
        if (logoDrawable != null) {
            val logoSize = (width * 0.20).toInt() // Logo occupies 20% of QR width
            val logoLeft = (width - logoSize) / 2
            val logoTop = (height - logoSize) / 2
            val logoRight = logoLeft + logoSize
            val logoBottom = logoTop + logoSize

            // Background circle/card for logo to clear QR code elements under it
            val borderSize = (logoSize * 0.15).toInt()
            val cardRect = RectF(
                (logoLeft - borderSize).toFloat(),
                (logoTop - borderSize).toFloat(),
                (logoRight + borderSize).toFloat(),
                (logoBottom + borderSize).toFloat()
            )
            val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(cardRect, borderSize.toFloat() * 1.5f, borderSize.toFloat() * 1.5f, cardPaint)

            // Dynamic blue background for the icon container to make it pop beautifully
            val innerCardRect = RectF(
                logoLeft.toFloat(),
                logoTop.toFloat(),
                logoRight.toFloat(),
                logoBottom.toFloat()
            )
            val innerCardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#5170FF") // Requested blue color
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(innerCardRect, borderSize.toFloat(), borderSize.toFloat(), innerCardPaint)

            // Draw logo inside
            val padding = (logoSize * 0.18).toInt()
            logoDrawable.setBounds(
                logoLeft + padding,
                logoTop + padding,
                logoRight - padding,
                logoBottom - padding
            )
            logoDrawable.setTint(Color.WHITE)
            logoDrawable.draw(canvas)
        }

        return bitmap
    }
}
