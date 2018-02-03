package com.devrapid.musicdiskplayer

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator

/**
 * Circular seek bar.
 *
 * @author  jieyi
 * @since   7/17/17
 */
class CircularSeekBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0):
    View(context, attrs, defStyleAttr) {
    companion object {
        private const val MAX_VALUE = 100f
        private const val DEFAULT_START_DEGREE = 135f
        private const val DEFAULT_SWEEP_DEGREE = 265f
        private const val WIDTH_OF_PROGRESS = 13f
        private const val BUTTON_RADIUS = 25f
        private const val INNER_PADDING = 25f
    }

    //region Variables of setting
    var progress = .0
        set (value) {
            field = value * rate
            val rawValue = (field / rate).toInt()

            if (this@CircularSeekBar.isTouchButton) {
                this@CircularSeekBar.remainedTime = (this@CircularSeekBar.totalTime - rawValue * this@CircularSeekBar.totalTime / 100).toLong()
            }
            // When change the value, it will invoke callback function.
            onProgressChanged?.invoke(rawValue, remainedTime.toInt())
            invalidate()
        }
    var progressColor = 0xFFFF7F50.toInt()
        set(value) {
            field = value
            playedProgressPaint.color = field
            postInv()
        }
    var unprogressColor = 0xFFA9A9A9.toInt()
        set(value) {
            field = value
            unplayProgressPaint.color = field
            postInv()
        }
    var progressWidth = WIDTH_OF_PROGRESS
        set(value) {
            field = value
            playedProgressPaint.strokeWidth = field
            postInv()
        }
    var startDegree = DEFAULT_START_DEGREE
        set(value) {
            field = value
            postInv()
        }
    // The degree swept, that is total degree.
    var sweepDegree = DEFAULT_SWEEP_DEGREE
        set(value) {
            field = value
            postInv()
        }
    var unpressBtnColor = 0xFF8A2BE2.toInt()
        set(value) {
            field = value
            controllerBtnPaint.color = field
            postInv()
        }
    var pressBtnColor = 0xFF00008B.toInt()
        set(value) {
            field = value
            controllerBtnPaint.color = field
            postInv()
        }
    var btnRadius = BUTTON_RADIUS
        set(value) {
            field = value
            postInv()
        }
    var onProgressChanged: ((progress: Int, remainedTime: Int) -> Unit)? = null
    var onProgressFinished: (() -> Unit)? = null
    // Check the press position is inside of the control circle button.
    var isTouchButton = false
        private set
    var totalTime = 0
    //endregion

    //region Private variables
    private val unplayProgressPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = progressColor
            strokeCap = Paint.Cap.ROUND
            strokeWidth = progressWidth
            style = Paint.Style.STROKE
        }
    }
    private val playedProgressPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = unprogressColor
            strokeCap = Paint.Cap.ROUND
            strokeWidth = progressWidth
            style = Paint.Style.STROKE
        }
    }
    private val controllerBtnPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = unpressBtnColor
        }
    }
    private val pm by lazy {
        PathMeasure().apply {
            setPath(Path().apply {
                addArc(RectF(INNER_PADDING + paddingStart,
                    INNER_PADDING + paddingTop,
                    width.toFloat() - paddingEnd - INNER_PADDING,
                    height.toFloat() - paddingBottom - INNER_PADDING),
                    startDegree,
                    sweepDegree)
            }, false)
        }
    }
    private val rectF by lazy {
        RectF(INNER_PADDING + paddingStart,
            INNER_PADDING + paddingTop,
            width.toFloat() - paddingEnd - INNER_PADDING,
            height.toFloat() - paddingBottom - INNER_PADDING)
    }
    private var rate = sweepDegree / MAX_VALUE
        set(value) {
            field = sweepDegree / MAX_VALUE
        }
    private var preX = 0f
    private var preY = 0f
    private var isVolumeUp = false
    private var pos = floatArrayOf(0f, 0f)
    private var isInit = true
    private var isAnimationRunning = false
    private var remainedTime = 0L
    private val postInv = { if (isInit) invalidate() }
    private var animatorPlay = ValueAnimator.ofFloat(0f, MAX_VALUE)
    //endregion

    init {
        context.obtainStyledAttributes(attrs, R.styleable.CircularSeekBar, defStyleAttr, 0).apply {
            startDegree = getFloat(R.styleable.CircularSeekBar_start_degree, startDegree)
            sweepDegree = getFloat(R.styleable.CircularSeekBar_sweep_degree, sweepDegree)
            progressWidth = getFloat(R.styleable.CircularSeekBar_progress_width, progressWidth)
            progress = getInteger(R.styleable.CircularSeekBar_progress, progress.toInt()).toDouble()
            btnRadius = getFloat(R.styleable.CircularSeekBar_controller_radius, btnRadius)
            progressColor = getColor(R.styleable.CircularSeekBar_progress_color, progressColor)
            unprogressColor = getColor(R.styleable.CircularSeekBar_unprogress_color, unprogressColor)
            pressBtnColor = getColor(R.styleable.CircularSeekBar_unpress_controller_color, pressBtnColor)
            unpressBtnColor = getColor(R.styleable.CircularSeekBar_controller_color, unpressBtnColor)
        }.recycle()

        isInit = true
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                // Checking the position is inside of the control button.
                if (e.x in pos[0] - btnRadius..pos[0] + btnRadius &&
                    e.y in pos[1] - btnRadius..pos[1] + btnRadius) {
                    isTouchButton = true
                    controllerBtnPaint.color = pressBtnColor
                    invalidate()

                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isTouchButton) {
                    return false
                }

                val down_degree = calculateTouchDegree(preX, preY)
                val degree = calculateTouchDegree(e.x, e.y)

                if (sweepDegree <= degree) {
                    return false
                }

                if (animatorPlay.isRunning) {
                    isAnimationRunning = true
                    stopAnimator()
                }

                isVolumeUp = down_degree < degree
                progress = calculateTouchProgress(degree)

                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isTouchButton = false
                controllerBtnPaint.color = unpressBtnColor
                invalidate()
                if (isAnimationRunning) {
                    playAnimator(remainedTime)
                    isAnimationRunning = false
                }
            }
        }
        // Keep the previous position.
        preX = e.x
        preY = e.y

        return false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val square = minOf(ViewGroup.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
            ViewGroup.getDefaultSize(suggestedMinimumHeight, heightMeasureSpec))
        setMeasuredDimension(square, square)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawArc(rectF,
            startDegree + sweepDegree,
            (-sweepDegree + progress).toFloat(),
            false,
            unplayProgressPaint)
        canvas.drawArc(rectF, startDegree, (0f + progress).toFloat(), false, playedProgressPaint)

        pm.getPosTan((progress / rate * pm.length / MAX_VALUE).toFloat(), pos, null)

        canvas.drawCircle(pos[0], pos[1], btnRadius, controllerBtnPaint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        onProgressChanged = null
        onProgressFinished = null
    }

    /**
     * Start playing the progress animation.
     *
     * @param secondDuration Remained time(second).
     */
    fun playAnimator(secondDuration: Long) {
        animatorPlay = ValueAnimator.ofFloat((progress / rate).toFloat(), MAX_VALUE).apply {
            duration = secondDuration * 1000
            interpolator = LinearInterpolator()
            addUpdateListener {
                val value = it.animatedValue as Float

                remainedTime = secondDuration - it.currentPlayTime / 1000
                progress = value.toDouble()
                // When the value reaches the max, the process is finished.
                if (MAX_VALUE == value) {
                    onProgressFinished?.invoke()
                }
            }
            start()
        }
    }

    /**
     * Stop playing the progress animation.
     */
    fun stopAnimator() = animatorPlay.cancel()

    /**
     * Calculating the degree which is from touching position to leaving from the screen.
     *
     * @param posX x_coordination.
     * @param posY y_coordination.
     * @return the degree. ex: 45°
     */
    private fun calculateTouchDegree(posX: Float, posY: Float): Double {
        val x = posX - pivotX.toDouble()
        val y = posY - pivotY.toDouble()

        println("$y  $x")
        println(Math.atan2(y, x))
        println(startDegree / 180)
        // Let's angel in 360°
        val angle = (Math.toDegrees(Math.atan2(y, x) - startDegree / 180 * Math.PI) + 360) % 360

        return if (angle >= sweepDegree) sweepDegree.toDouble() else angle
    }

    /**
     * According to angle, calculating to progress.
     *
     * @param angle angle.
     * @return progress.
     */
    private fun calculateTouchProgress(angle: Double): Double = angle / sweepDegree * 100  // As like passed percent process.
}