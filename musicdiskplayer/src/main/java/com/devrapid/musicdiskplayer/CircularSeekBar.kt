package com.devrapid.musicdiskplayer

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.os.Build.VERSION_CODES
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator

/**
 * Circular seek bar.
 *
 * @author  jieyi
 * @since   7/17/17
 */
@RequiresApi(VERSION_CODES.M)
class CircularSeekBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0):
    View(context, attrs, defStyleAttr) {
    companion object {
        private const val MAX_VALUE = 100f
        private const val DEFAULT_START_DEGREE = 135f
        private const val DEFAULT_SWEEP_DEGREE = 265f
        private const val WIDTH_OF_PROGRESS = 13f
        private const val BUTTON_RADIUS = 25f
    }

    //region Variables of setting
    var progress = .0
        set (value) {
            field = value * this.rate
            val rawValue = (field / this.rate).toInt()

            if (this@CircularSeekBar.isTouchButton) {
                this@CircularSeekBar.remainedTime = (this@CircularSeekBar.totalTime - rawValue * this@CircularSeekBar.totalTime / 100).toLong()
            }
            // When change the value, it will invoke callback function.
            this.onProgressChanged?.invoke(rawValue, this@CircularSeekBar.remainedTime.toInt())
            this.invalidate()
        }
    var progressColor = 0xFF7F50
        set(value) {
            field = value
            this.playedProgressPaint.color = field
            this.postInv()
        }
    var unprogressColor = 0xA9A9A9
        set(value) {
            field = value
            this.unplayProgressPaint.color = field
            this.postInv()
        }
    var progressWidth = WIDTH_OF_PROGRESS
        set(value) {
            field = value
            this.playedProgressPaint.strokeWidth = field
            this.postInv()
        }
    var startDegree = DEFAULT_START_DEGREE
        set(value) {
            field = value
            this.postInv()
        }
    // The degree swept, that is total degree.
    var sweepDegree = DEFAULT_SWEEP_DEGREE
        set(value) {
            field = value
            this.postInv()
        }
    var unpressBtnColor = 0x8A2BE2
        set(value) {
            field = value
            this.controllerBtnPaint.color = field
            this.postInv()
        }
    var pressBtnColor = 0x00008B
        set(value) {
            field = value
            this.controllerBtnPaint.color = field
            this.postInv()
        }
    var btnRadius = BUTTON_RADIUS
        set(value) {
            field = value
            this.postInv()
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
            color = this@CircularSeekBar.progressColor
            strokeCap = Paint.Cap.ROUND
            strokeWidth = this@CircularSeekBar.progressWidth
            style = Paint.Style.STROKE
        }
    }
    private val playedProgressPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            Log.d("tag", this@CircularSeekBar.context.toString())
            color = this@CircularSeekBar.unprogressColor
            strokeCap = Paint.Cap.ROUND
            strokeWidth = this@CircularSeekBar.progressWidth
            style = Paint.Style.STROKE
        }
    }
    private val controllerBtnPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = this@CircularSeekBar.unpressBtnColor
        }
    }
    private val pm by lazy {
        PathMeasure().apply {
            setPath(Path().apply {
                addArc(RectF(0f + this@CircularSeekBar.paddingStart,
                    0f + this@CircularSeekBar.paddingTop,
                    width.toFloat() - this@CircularSeekBar.paddingEnd,
                    height.toFloat() - this@CircularSeekBar.paddingBottom),
                    this@CircularSeekBar.startDegree,
                    this@CircularSeekBar.sweepDegree)
            }, false)
        }
    }
    private val rectF by lazy {
        RectF(0f + this.paddingStart,
            0f + this.paddingTop,
            width.toFloat() - this.paddingEnd,
            height.toFloat() - this.paddingBottom)
    }
    private var rate = this.sweepDegree / MAX_VALUE
        set(value) {
            field = this.sweepDegree / MAX_VALUE
        }
    private var preX = 0f
    private var preY = 0f
    private var isVolumeUp = false
    private var pos = floatArrayOf(0f, 0f)
    private var isInit = true
    private var isAnimationRunning = false
    private var remainedTime = 0L
    private val postInv = { if (this.isInit) this.invalidate() }
    private var animatorPlay = ValueAnimator.ofFloat(0f, MAX_VALUE)
    //endregion

    init {
        this.context.obtainStyledAttributes(attrs, R.styleable.CircularSeekBar, defStyleAttr, 0).also {
            this.startDegree = it.getFloat(R.styleable.CircularSeekBar_start_degree, this.startDegree)
            this.sweepDegree = it.getFloat(R.styleable.CircularSeekBar_sweep_degree, this.sweepDegree)
            this.progressWidth = it.getFloat(R.styleable.CircularSeekBar_progress_width, this.progressWidth)
            this.progress = it.getInteger(R.styleable.CircularSeekBar_progress, this.progress.toInt()).toDouble()
            this.btnRadius = it.getFloat(R.styleable.CircularSeekBar_controller_radius, this.btnRadius)
            this.progressColor = it.getColor(R.styleable.CircularSeekBar_progress_color, this.progressColor)
            this.unprogressColor = it.getColor(R.styleable.CircularSeekBar_unprogress_color, this.unprogressColor)
            this.pressBtnColor = it.getColor(R.styleable.CircularSeekBar_unpress_controller_color, this.pressBtnColor)
            this.unpressBtnColor = it.getColor(R.styleable.CircularSeekBar_controller_color, this.unpressBtnColor)
        }.recycle()

        this.isInit = true
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                // Checking the position is inside of the control button.
                if (e.x in this.pos[0] - this.btnRadius..this.pos[0] + this.btnRadius &&
                    e.y in this.pos[1] - this.btnRadius..this.pos[1] + this.btnRadius) {
                    this.isTouchButton = true
                    this.controllerBtnPaint.color = this.context.getColor(this.pressBtnColor)
                    this.invalidate()

                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!this.isTouchButton) {
                    return false
                }

                val down_degree = this.calculateTouchDegree(this.preX, this.preY)
                val degree = this.calculateTouchDegree(e.x, e.y)

                if (this.sweepDegree <= degree) {
                    return false
                }

                if (this.animatorPlay.isRunning) {
                    this.isAnimationRunning = true
                    this.stopAnimator()
                }

                this.isVolumeUp = down_degree < degree
                this.progress = this.calculateTouchProgress(degree)

                this.invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                this.isTouchButton = false
                this.controllerBtnPaint.color = this.context.getColor(this.unpressBtnColor)
                this.invalidate()
                if (this.isAnimationRunning) {
                    this.playAnimator(this@CircularSeekBar.remainedTime)
                    this.isAnimationRunning = false
                }
            }
        }
        // Keep the previous position.
        this.preX = e.x
        this.preY = e.y

        return false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        this.setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawArc(this.rectF,
            this.startDegree + this.sweepDegree,
            (-this.sweepDegree + this.progress).toFloat(),
            false,
            this.unplayProgressPaint)
        canvas.drawArc(this.rectF, this.startDegree, (0f + this.progress).toFloat(), false, this.playedProgressPaint)

        this.pm.getPosTan((this.progress / this.rate * this.pm.length / MAX_VALUE).toFloat(), this.pos, null)

        canvas.drawCircle(this.pos[0], this.pos[1], this.btnRadius, this.controllerBtnPaint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        this.onProgressChanged = null
        this.onProgressFinished = null
    }

    /**
     * Start playing the progress animation.
     *
     * @param secondDuration Remained time(second).
     */
    fun playAnimator(secondDuration: Long) {
        this.animatorPlay = ValueAnimator.ofFloat((this.progress / this.rate).toFloat(), MAX_VALUE).apply {
            duration = secondDuration * 1000
            interpolator = LinearInterpolator()
            addUpdateListener {
                val value = it.animatedValue as Float

                this@CircularSeekBar.remainedTime = secondDuration - it.currentPlayTime / 1000
                this@CircularSeekBar.progress = value.toDouble()
                // When the value reaches the max, the process is finished.
                if (MAX_VALUE == value) {
                    this@CircularSeekBar.onProgressFinished?.invoke()
                }
            }
            start()
        }
    }

    /**
     * Stop playing the progress animation.
     */
    fun stopAnimator() = this.animatorPlay.cancel()

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
        // Let's angel in 360°
        val angle = (Math.toDegrees(Math.atan2(y, x) - this.startDegree / 180 * Math.PI) + 360) % 360

        return if (angle >= this.sweepDegree) this.sweepDegree.toDouble() else angle
    }

    /**
     * According to angle, calculating to progress.
     *
     * @param angle angle.
     * @return progress.
     */
    private fun calculateTouchProgress(angle: Double): Double = angle / this.sweepDegree * 100  // As like passed percent process.
}