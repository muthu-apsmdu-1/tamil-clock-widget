package com.example.tamilclock

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.os.BatteryManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

object ClockFaceRenderer {

    private val TEAL_NEON = Color.parseColor("#00F5D4")
    private val NEON_GREEN = Color.parseColor("#39FF14")

    // -------------------------------------------------------------
    // 1. ANALOG STYLE B DIAL (AMOLED Black + Radial White Numerals + 60 Ticks)
    // -------------------------------------------------------------
    fun createAmoledWhiteDialBitmap(sizePx: Int = 280): Bitmap {
        val safeSize = sizePx.coerceAtLeast(1)
        val bmp = Bitmap.createBitmap(safeSize, safeSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val cx = safeSize / 2f
        val cy = safeSize / 2f
        val radius = safeSize * 0.48f

        // Pure AMOLED Black Circle
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, radius, bgPaint)

        // Subtle outer border ring
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#33FFFFFF")
            style = Paint.Style.STROKE
            strokeWidth = safeSize * 0.008f
        }
        canvas.drawCircle(cx, cy, radius - ringPaint.strokeWidth, ringPaint)

        // 60 Perimeter Tick Marks
        val majorTickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            strokeWidth = safeSize * 0.011f
            strokeCap = Paint.Cap.ROUND
        }
        val minorTickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#888888")
            strokeWidth = safeSize * 0.006f
            strokeCap = Paint.Cap.ROUND
        }

        for (i in 0 until 60) {
            val angleRad = Math.toRadians((i * 6.0) - 90.0)
            val isMajor = i % 5 == 0
            val outerR = radius * 0.94f
            val innerR = if (isMajor) radius * 0.85f else radius * 0.89f
            canvas.drawLine(
                cx + (outerR * cos(angleRad)).toFloat(),
                cy + (outerR * sin(angleRad)).toFloat(),
                cx + (innerR * cos(angleRad)).toFloat(),
                cy + (innerR * sin(angleRad)).toFloat(),
                if (isMajor) majorTickPaint else minorTickPaint
            )
        }

        // Tall Condensed Radial Numerals (1..12)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = safeSize * 0.125f
            typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            textScaleX = 0.78f
        }
        val numRadius = radius * 0.68f
        val fontMetrics = textPaint.fontMetrics
        val textOffsetY = -(fontMetrics.ascent + fontMetrics.descent) / 2f

        for (hour in 1..12) {
            val deg = hour * 30f
            val rad = Math.toRadians(deg - 90.0)
            val nx = cx + (numRadius * cos(rad)).toFloat()
            val ny = cy + (numRadius * sin(rad)).toFloat()

            canvas.save()
            val tiltDeg = if (hour in 4..8) deg - 180f else deg
            canvas.rotate(tiltDeg, nx, ny)
            canvas.drawText(hour.toString(), nx, ny + textOffsetY, textPaint)
            canvas.restore()
        }

        return bmp
    }

    // -------------------------------------------------------------
    // 2. ANALOG STYLE C DIAL (AMOLED Black + Teal Triangle Bullets + Date/Day/Battery)
    // -------------------------------------------------------------
    fun createAmoledHybridDialBitmap(
        context: Context,
        cal: Calendar = Calendar.getInstance(),
        sizePx: Int = 280
    ): Bitmap {
        val safeSize = sizePx.coerceAtLeast(1)
        val bmp = Bitmap.createBitmap(safeSize, safeSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val cx = safeSize / 2f
        val cy = safeSize / 2f
        val radius = safeSize * 0.48f

        // Pure AMOLED Black Circle
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, radius, bgPaint)

        // Teal Perimeter Triangle Bullets (every 5 mins) + Small Dots (single mins)
        val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEAL_NEON
            style = Paint.Style.FILL
        }

        for (i in 0 until 60) {
            val angleDeg = (i * 6f) - 90f
            val rad = Math.toRadians(angleDeg.toDouble())
            if (i % 5 == 0) {
                val tipR = radius * 0.85f
                val baseR = radius * 0.94f
                val halfSpreadRad = Math.toRadians(2.4)

                val path = Path().apply {
                    moveTo(cx + (tipR * cos(rad)).toFloat(), cy + (tipR * sin(rad)).toFloat())
                    lineTo(
                        cx + (baseR * cos(rad - halfSpreadRad)).toFloat(),
                        cy + (baseR * sin(rad - halfSpreadRad)).toFloat()
                    )
                    lineTo(
                        cx + (baseR * cos(rad + halfSpreadRad)).toFloat(),
                        cy + (baseR * sin(rad + halfSpreadRad)).toFloat()
                    )
                    close()
                }
                canvas.drawPath(path, markerPaint)
            } else {
                val dotR = radius * 0.91f
                canvas.drawCircle(
                    cx + (dotR * cos(rad)).toFloat(),
                    cy + (dotR * sin(rad)).toFloat(),
                    safeSize * 0.0055f,
                    markerPaint
                )
            }
        }

        // Upright Rounded Teal Numerals (1..12)
        val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEAL_NEON
            textSize = safeSize * 0.088f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val numRadius = radius * 0.72f
        val fm = numPaint.fontMetrics
        val numOffsetY = -(fm.ascent + fm.descent) / 2f

        for (hour in 1..12) {
            val rad = Math.toRadians((hour * 30.0) - 90.0)
            val nx = cx + (numRadius * cos(rad)).toFloat()
            val ny = cy + (numRadius * sin(rad)).toFloat()
            canvas.drawText(hour.toString(), nx, ny + numOffsetY, numPaint)
        }

        // Embedded Complications
        val compPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEAL_NEON
            textSize = safeSize * 0.068f
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        // Right (3 o'clock area): Month & Date
        val monthDateStr = SimpleDateFormat("MMM dd", Locale.ENGLISH)
            .format(cal.time)
            .uppercase(Locale.ENGLISH)
        canvas.drawText(monthDateStr, cx + (radius * 0.36f), cy + (safeSize * 0.022f), compPaint)

        // Bottom (above 6): Weekday
        val weekdayStr = SimpleDateFormat("EEE", Locale.ENGLISH)
            .format(cal.time)
            .uppercase(Locale.ENGLISH)
        canvas.drawText(weekdayStr, cx, cy + (radius * 0.43f), compPaint)

        // Left (9 o'clock area): Battery Icon + Percentage
        val batteryPct = getBatteryPercentage(context)
        drawBatteryComplication(canvas, cx - (radius * 0.39f), cy, safeSize, batteryPct)

        return bmp
    }

    private fun drawBatteryComplication(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        sizePx: Int,
        batteryPct: Int
    ) {
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEAL_NEON
            style = Paint.Style.STROKE
            strokeWidth = sizePx * 0.006f
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEAL_NEON
            style = Paint.Style.FILL
        }

        val bw = sizePx * 0.11f
        val bh = sizePx * 0.052f
        val left = centerX - bw / 2f
        val top = centerY - bh - (sizePx * 0.005f)
        val rect = RectF(left, top, left + bw, top + bh)
        canvas.drawRoundRect(rect, sizePx * 0.01f, sizePx * 0.01f, strokePaint)

        // Battery Cap
        canvas.drawRect(
            left + bw,
            top + bh * 0.28f,
            left + bw + (sizePx * 0.012f),
            top + bh * 0.72f,
            fillPaint
        )

        // 5 internal bars proportional to battery %
        val bars = ((batteryPct + 10) / 20).coerceIn(1, 5)
        val pad = sizePx * 0.01f
        val innerW = bw - (pad * 2)
        val barW = (innerW / 5f) * 0.7f
        val step = innerW / 5f
        for (b in 0 until bars) {
            val bx = left + pad + (b * step)
            canvas.drawRect(bx, top + pad, bx + barW, top + bh - pad, fillPaint)
        }

        val pctPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = TEAL_NEON
            textSize = sizePx * 0.044f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("$batteryPct%", centerX, centerY + (sizePx * 0.038f), pctPaint)
    }

    private fun getBatteryPercentage(context: Context): Int {
        return try {
            val intent = context.applicationContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt() else 100
        } catch (_: Exception) {
            100
        }
    }

    // -------------------------------------------------------------
    // 3. SETTINGS THUMBNAIL PREVIEW BITMAPS (FOR SELECTION BUTTONS)
    // -------------------------------------------------------------
    fun createSettingsButtonThumbnail(
        context: Context,
        styleId: Int,
        sizePx: Int = 120
    ): Bitmap {
        val safeSize = sizePx.coerceAtLeast(1)
        val bmp = Bitmap.createBitmap(safeSize, safeSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val cx = safeSize / 2f
        val cy = safeSize / 2f

        when (styleId) {
            ThemeManager.CLOCK_DIGITAL_CLASSIC -> {
                val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#33FFFFFF") }
                canvas.drawRoundRect(RectF(6f, 24f, safeSize - 6f, safeSize - 24f), 16f, 16f, bg)
                val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = safeSize * 0.25f
                    typeface = Typeface.DEFAULT_BOLD
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("10:15", cx, cy + (safeSize * 0.08f), tp)
            }

            ThemeManager.CLOCK_DIGITAL_AMOLED_NEON -> {
                val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK }
                canvas.drawRoundRect(RectF(6f, 24f, safeSize - 6f, safeSize - 24f), 16f, 16f, bg)
                val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = NEON_GREEN
                    style = Paint.Style.STROKE
                    strokeWidth = 2.5f
                }
                canvas.drawRoundRect(RectF(6f, 24f, safeSize - 6f, safeSize - 24f), 16f, 16f, stroke)
                val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = NEON_GREEN
                    textSize = safeSize * 0.25f
                    typeface = Typeface.MONOSPACE
                    isFakeBoldText = true
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("10:15", cx, cy + (safeSize * 0.08f), tp)
            }

            ThemeManager.CLOCK_ANALOG_CLASSIC -> {
                val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                }
                canvas.drawCircle(cx, cy, safeSize * 0.42f, ring)
                drawSampleHands(canvas, cx, cy, safeSize * 0.42f, includeSecond = false)
            }

            ThemeManager.CLOCK_ANALOG_AMOLED_WHITE -> {
                val dial = createAmoledWhiteDialBitmap(safeSize)
                try {
                    canvas.drawBitmap(dial, 0f, 0f, null)
                } finally {
                    dial.recycle()
                }
                drawSampleHands(canvas, cx, cy, safeSize * 0.42f, includeSecond = true)
            }

            ThemeManager.CLOCK_ANALOG_AMOLED_HYBRID -> {
                val dial = createAmoledHybridDialBitmap(context, Calendar.getInstance(), safeSize)
                try {
                    canvas.drawBitmap(dial, 0f, 0f, null)
                } finally {
                    dial.recycle()
                }
                drawSampleHands(canvas, cx, cy, safeSize * 0.42f, includeSecond = true)
            }
        }
        return bmp
    }

    private fun drawSampleHands(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        r: Float,
        includeSecond: Boolean
    ) {
        val handPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            strokeWidth = r * 0.085f
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(cx, cy, cx - r * 0.38f, cy - r * 0.25f, handPaint)
        canvas.drawLine(cx, cy, cx + r * 0.54f, cy - r * 0.30f, handPaint)

        if (includeSecond) {
            val secPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FF1744")
                strokeWidth = r * 0.04f
                strokeCap = Paint.Cap.ROUND
            }
            canvas.drawLine(cx, cy + r * 0.14f, cx - r * 0.38f, cy + r * 0.46f, secPaint)
            canvas.drawCircle(cx, cy, r * 0.07f, secPaint)
        }
    }
}
