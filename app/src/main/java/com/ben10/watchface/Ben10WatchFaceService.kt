package com.ben10.watchface

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.view.SurfaceHolder
import java.lang.ref.WeakReference
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val MSG_UPDATE_TIME = 0

class Ben10WatchFaceService : CanvasWatchFaceService() {

    override fun onCreateEngine(): Engine = Ben10Engine()

    private class UpdateHandler(ref: WeakReference<Ben10Engine>) :
        Handler(Looper.getMainLooper()) {
        private val engineRef = ref
        override fun handleMessage(msg: Message) {
            if (msg.what == MSG_UPDATE_TIME) engineRef.get()?.onTick()
        }
    }

    inner class Ben10Engine : CanvasWatchFaceService.Engine() {

        // ── Colours ────────────────────────────────────────────────────────────
        private val clrBlack        = Color.BLACK
        private val clrGreenBright  = Color.parseColor("#00FF41")
        private val clrGreenMid     = Color.parseColor("#00CC22")
        private val clrGreenDark    = Color.parseColor("#005500")
        private val clrGreenDeep    = Color.parseColor("#001A00")
        private val clrCasing       = Color.parseColor("#0E0E0E")
        private val clrGray         = Color.parseColor("#444444")

        // ── Paints ─────────────────────────────────────────────────────────────
        private val pBg             = Paint()   // black background
        private val pCasing         = Paint()   // device body
        private val pOuterRing      = Paint()   // outer green ring stroke
        private val pInnerRing      = Paint()   // inner ring stroke
        private val pGlowHalo       = Paint()   // soft glow around center
        private val pBtnDeep        = Paint()   // center button dark layer
        private val pBtnMid         = Paint()   // center button mid layer
        private val pBtnBright      = Paint()   // center button bright layer
        private val pBtnBorder      = Paint()   // center button border
        private val pSymbol         = Paint()   // hourglass fill (white)
        private val pSymbolBar      = Paint()   // hourglass top/bottom bars
        private val pDetailLine     = Paint()   // cardinal tick marks
        private val pDetailDot      = Paint()   // 45-degree dots
        private val pAmbientRing    = Paint()   // ambient ring stroke
        private val pAmbientBtn     = Paint()   // ambient center fill
        private val pAmbientSymbol  = Paint()   // ambient hourglass
        private val pTime           = Paint()   // digital clock

        // ── State ──────────────────────────────────────────────────────────────
        private var cx = 0f
        private var cy = 0f
        private var radius = 0f
        private var ambient = false
        private var scaledBitmap: Bitmap? = null
        private var scaledAodBitmap: Bitmap? = null
        private var ben10Typeface: Typeface? = null

        private val handler = UpdateHandler(WeakReference(this))

        // ── Lifecycle ──────────────────────────────────────────────────────────

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)
            setWatchFaceStyle(
                WatchFaceStyle.Builder(this@Ben10WatchFaceService)
                    .setAcceptsTapEvents(true)
                    .build()
            )
            ben10Typeface = Typeface.createFromAsset(assets, "fonts/BADABB__.TTF")
            initPaints()
        }

        private fun initPaints() {
            pBg.apply { color = clrBlack; isAntiAlias = true }

            pCasing.apply { color = clrCasing; style = Paint.Style.FILL; isAntiAlias = true }

            pOuterRing.apply {
                color = clrGreenDark; style = Paint.Style.STROKE
                strokeWidth = 5f; isAntiAlias = true
            }
            pInnerRing.apply {
                color = clrGreenDark; style = Paint.Style.STROKE
                strokeWidth = 2f; isAntiAlias = true; alpha = 160
            }

            pGlowHalo.apply {
                color = Color.argb(55, 0, 255, 65)
                style = Paint.Style.FILL; isAntiAlias = true
            }

            pBtnDeep.apply { color = clrGreenDeep; style = Paint.Style.FILL; isAntiAlias = true }
            pBtnMid.apply { color = clrGreenDark; style = Paint.Style.FILL; isAntiAlias = true }
            pBtnBright.apply { color = clrGreenMid; style = Paint.Style.FILL; isAntiAlias = true }

            pBtnBorder.apply {
                color = clrGreenBright; style = Paint.Style.STROKE
                strokeWidth = 4f; isAntiAlias = true
            }

            pSymbol.apply { color = Color.WHITE; style = Paint.Style.FILL; isAntiAlias = true }

            pSymbolBar.apply {
                color = Color.WHITE; style = Paint.Style.STROKE
                strokeWidth = 5f; strokeCap = Paint.Cap.ROUND; isAntiAlias = true
            }

            pDetailLine.apply {
                color = clrGreenDark; style = Paint.Style.STROKE
                strokeWidth = 4f; strokeCap = Paint.Cap.ROUND; isAntiAlias = true; alpha = 200
            }

            pDetailDot.apply {
                color = clrGreenDark; style = Paint.Style.FILL
                isAntiAlias = true; alpha = 160
            }

            pAmbientRing.apply {
                color = clrGray; style = Paint.Style.STROKE
                strokeWidth = 1f; isAntiAlias = true
            }
            pAmbientBtn.apply {
                color = Color.parseColor("#1A1A1A"); style = Paint.Style.FILL; isAntiAlias = true
            }
            pAmbientSymbol.apply {
                color = Color.LTGRAY; style = Paint.Style.FILL; isAntiAlias = true
            }
            pTime.apply {
                color = Color.parseColor("#FF0000")
                textSize = radius * 0.27f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                typeface = ben10Typeface
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
            super.onSurfaceChanged(holder, format, w, h)
            cx = w / 2f; cy = h / 2f; radius = min(w, h) / 2f
            val size = (min(w, h) * 1.1f).toInt()
            val raw = BitmapFactory.decodeResource(resources, R.drawable.omnitrix_bg)
            scaledBitmap?.recycle()
            scaledBitmap = Bitmap.createScaledBitmap(raw, size, size, true)
            raw.recycle()
            val aodSize = (min(w, h) * 1.1f).toInt()
            val rawAod = BitmapFactory.decodeResource(resources, R.drawable.omnitrix_aod)
            scaledAodBitmap?.recycle()
            scaledAodBitmap = Bitmap.createScaledBitmap(rawAod, aodSize, aodSize, true)
            rawAod.recycle()
            initPaints()
        }

        override fun onTimeTick() { super.onTimeTick(); invalidate() }

        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            super.onAmbientModeChanged(inAmbientMode)
            ambient = inAmbientMode
            updateTimer(); invalidate()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            updateTimer()
        }

        // ── Draw ───────────────────────────────────────────────────────────────

        override fun onDraw(canvas: Canvas, bounds: Rect) {
            if (radius == 0f) return
            canvas.drawCircle(cx, cy, radius, pBg)
            if (ambient) { drawAmbient(canvas); return }
            scaledBitmap?.let { bmp ->
                canvas.drawBitmap(bmp, cx - bmp.width / 2f, cy - bmp.height / 2f, null)
            } ?: drawOmnitrix(canvas)
            drawTime(canvas)
        }

        private fun drawOmnitrix(canvas: Canvas) {
            val r = radius

            // Device body
            canvas.drawCircle(cx, cy, r * 0.97f, pCasing)

            // Outer casing ring + inner ring
            canvas.drawCircle(cx, cy, r * 0.92f, pOuterRing)
            canvas.drawCircle(cx, cy, r * 0.76f, pInnerRing)

            // Dark inner area
            canvas.drawCircle(cx, cy, r * 0.75f, pBg)

            // Glow halo around center button
            canvas.drawCircle(cx, cy, r * 0.50f, pGlowHalo)

            // Center button — layered for depth
            val cr = r * 0.42f
            canvas.drawCircle(cx, cy, cr,          pBtnDeep)
            canvas.drawCircle(cx, cy, cr * 0.88f,  pBtnMid)
            canvas.drawCircle(cx, cy, cr * 0.72f,  pBtnBright)
            canvas.drawCircle(cx, cy, cr,          pBtnBorder)

            // Cardinal tick marks (12, 3, 6, 9)
            drawCardinalMarks(canvas, r)

            // Omnitrix hourglass
            drawHourglass(canvas, cr)
        }

        private fun drawCardinalMarks(canvas: Canvas, r: Float) {
            for (i in 0..3) {
                val a = (Math.PI / 2.0 * i - Math.PI / 2.0).toFloat()
                val outerR = r * 0.88f
                val innerR = r * 0.78f
                canvas.drawLine(
                    cx + outerR * cos(a), cy + outerR * sin(a),
                    cx + innerR * cos(a), cy + innerR * sin(a),
                    pDetailLine
                )
            }
            // Diagonal dots at 45°
            for (i in 0..3) {
                val a = (Math.PI / 4.0 + Math.PI / 2.0 * i - Math.PI / 2.0).toFloat()
                val dr = r * 0.85f
                canvas.drawCircle(cx + dr * cos(a), cy + dr * sin(a), 5f, pDetailDot)
            }
        }

        private fun drawHourglass(canvas: Canvas, cr: Float) {
            val hw = cr * 0.54f   // half-width at top/bottom
            val hh = cr * 0.60f  // half-height

            // Top triangle  ▽
            val top = Path().apply {
                moveTo(cx - hw, cy - hh)
                lineTo(cx + hw, cy - hh)
                lineTo(cx,      cy)
                close()
            }
            // Bottom triangle △
            val bot = Path().apply {
                moveTo(cx - hw, cy + hh)
                lineTo(cx + hw, cy + hh)
                lineTo(cx,      cy)
                close()
            }
            canvas.drawPath(top, pSymbol)
            canvas.drawPath(bot, pSymbol)

            // Horizontal bars at top and bottom of hourglass
            canvas.drawLine(cx - hw, cy - hh, cx + hw, cy - hh, pSymbolBar)
            canvas.drawLine(cx - hw, cy + hh, cx + hw, cy + hh, pSymbolBar)
        }

        private fun drawTime(canvas: Canvas) {
            val cal = java.util.Calendar.getInstance()
            val hour = cal.get(java.util.Calendar.HOUR)
            val min  = cal.get(java.util.Calendar.MINUTE)
            val time = "%02d:%02d".format(hour, min)
            canvas.drawText(time, cx, cy + radius * 0.78f, pTime)
        }

        private fun drawAmbient(canvas: Canvas) {
            scaledAodBitmap?.let { bmp ->
                canvas.drawBitmap(bmp, cx - bmp.width / 2f, cy - bmp.height / 2f, null)
            }
        }

        // ── Timer ──────────────────────────────────────────────────────────────

        fun onTick() {
            invalidate()
            if (shouldRun()) {
                val delay = 60000L - System.currentTimeMillis() % 60000L
                handler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delay)
            }
        }

        private fun updateTimer() {
            handler.removeMessages(MSG_UPDATE_TIME)
            if (shouldRun()) handler.sendEmptyMessage(MSG_UPDATE_TIME)
        }

        private fun shouldRun() = isVisible && !ambient
    }
}
