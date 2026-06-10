package com.car.play.android.app.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * A 270° automotive-style speed gauge. Set the current speed with [setSpeed]; the arc
 * animates smoothly and tick marks plus an accent progress arc give a premium cockpit feel.
 */
class SpeedGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val startAngle = 135f
    private val sweepAngle = 270f

    private var maxSpeed = 200f
    private var currentSpeed = 0f
    private var displayedSpeed = 0f

    private val density = resources.displayMetrics.density

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#2E3A52")
        strokeWidth = 14f * density
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 14f * density
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#33415C")
        strokeWidth = 2f * density
    }

    private val majorTickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#6B768C")
        strokeWidth = 3f * density
    }

    private val arcRect = RectF()
    private var animator: ValueAnimator? = null

    fun setMaxSpeed(value: Float) {
        maxSpeed = if (value <= 0f) 200f else value
        invalidate()
    }

    fun setSpeed(value: Float) {
        val clamped = value.coerceIn(0f, maxSpeed)
        if (clamped == currentSpeed) return
        currentSpeed = clamped
        animator?.cancel()
        animator = ValueAnimator.ofFloat(displayedSpeed, clamped).apply {
            duration = 600
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                displayedSpeed = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val stroke = 14f * density
        val pad = stroke / 2f + 6f * density
        val size = min(w, h).toFloat()
        val left = (w - size) / 2f + pad
        val top = (h - size) / 2f + pad
        arcRect.set(left, top, left + size - 2 * pad, top + size - 2 * pad)

        progressPaint.shader = SweepGradient(
            w / 2f, h / 2f,
            intArrayOf(
                Color.parseColor("#2D7DFF"),
                Color.parseColor("#00E5FF"),
                Color.parseColor("#2D7DFF")
            ),
            floatArrayOf(0f, 0.5f, 1f)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Track
        canvas.drawArc(arcRect, startAngle, sweepAngle, false, trackPaint)

        // Tick marks
        val cx = arcRect.centerX()
        val cy = arcRect.centerY()
        val outer = arcRect.width() / 2f + 2f * density
        val tickInner = outer - 9f * density
        val majorInner = outer - 16f * density
        val totalTicks = 40
        for (i in 0..totalTicks) {
            val angle = startAngle + sweepAngle * i / totalTicks
            val rad = Math.toRadians(angle.toDouble())
            val isMajor = i % 5 == 0
            val inner = if (isMajor) majorInner else tickInner
            val paint = if (isMajor) majorTickPaint else tickPaint
            canvas.drawLine(
                cx + (outer * cos(rad)).toFloat(),
                cy + (outer * sin(rad)).toFloat(),
                cx + (inner * cos(rad)).toFloat(),
                cy + (inner * sin(rad)).toFloat(),
                paint
            )
        }

        // Progress
        val fraction = (displayedSpeed / maxSpeed).coerceIn(0f, 1f)
        canvas.drawArc(arcRect, startAngle, sweepAngle * fraction, false, progressPaint)
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        super.onDetachedFromWindow()
    }
}
