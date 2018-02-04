package com.devrapid.musicdiskplayer

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import com.mikhaellopez.circularimageview.CircularImageView

/**
 * [CircleImageView] with rotated animation.
 *
 * @author  jieyi
 * @since   7/4/17
 */
open class RotatedCircleImageView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0):
    CircularImageView(context, attrs, defStyleAttr) {
    companion object {
        private const val ONE_ROUND_ROTATE_TIME = 10
        private const val TIME_MILLION = 1000L
        private const val A_CIRCLE_ANGEL = 360f
    }

    //region Variables of setting
    var onClickCallback: ((RotatedCircleImageView) -> Unit)? = null
    // Basically this's that controlling the rotating speed.
    var oneRoundTime = ONE_ROUND_ROTATE_TIME.toLong()
        private set(value) {
            field = value * TIME_MILLION
        }
    var isPauseState = false
        private set
    //endregion

    private val rotateAnimator by lazy {
        ObjectAnimator.ofFloat(this, "rotation", 0f, A_CIRCLE_ANGEL).apply {
            interpolator = LinearInterpolator()
            duration = oneRoundTime
            repeatCount = Animation.INFINITE
        }
    }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.RotatedCircleImageView, defStyleAttr, 0).also {
            oneRoundTime = it.getInteger(R.styleable.RotatedCircleImageView_rotate_sec, ONE_ROUND_ROTATE_TIME).toLong()
        }.recycle()

        setOnClickListener {
            rotateAnimator.let {
                isPauseState = when {
                    !it.isStarted -> {
                        it.start()
                        false
                    }
                    it.isPaused -> {
                        it.resume()
                        false
                    }
                    it.isRunning -> {
                        it.pause()
                        true
                    }
                    else -> true
                }
            }
            onClickCallback?.let { it(this@RotatedCircleImageView) }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val square = minOf(ViewGroup.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
            ViewGroup.getDefaultSize(suggestedMinimumHeight, heightMeasureSpec))
        setMeasuredDimension(square, square)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
        // Checking the clicking is inside of circle imageview.
            MotionEvent.ACTION_DOWN -> return isInCircleRange(e.x, e.y)
            MotionEvent.ACTION_UP -> {
                if (isInCircleRange(e.x, e.y)) {
                    // After confirming the clicking is inside of the image.
                    performClick()

                    return true
                }
            }
        }

        return false
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        onClickCallback = null
    }

    /**
     * Start the rotating animation of the circle image.
     */
    fun start() {
        // According to the state then start the animation.
        if (!rotateAnimator.isStarted) {
            rotateAnimator.start()
        }
        else if (rotateAnimator.isPaused) {
            rotateAnimator.resume()
        }
        else {
            return
        }
        isPauseState = false
    }

    /**
     * Stop the rotating animation of the circle image.
     */
    fun stop() {
        if (rotateAnimator.isRunning) {
            rotateAnimator.pause()
            isPauseState = true
        }
    }

    /**
     * Check the position [x] & [y] is inside the circle.
     *
     * @param x x-coordination.
     * @param y y-coordination.
     * @return [true] if the position of clicking is in the circle range ; otherwise [false].
     */
    private fun isInCircleRange(x: Float, y: Float): Boolean =
        width / 2 > distance(x, y, pivotX, pivotY)

    /**
     * Calculating the distance between two positions.
     *
     * @param sX a position's x-coordination.
     * @param sY a position's y-coordination.
     * @param eX another position's x-coordination.
     * @param eY another position's y-coordination.
     * @return the distance length.
     */
    private fun distance(sX: Float, sY: Float, eX: Float, eY: Float): Double =
        Math.sqrt(Math.pow((sX - eX).toDouble(), 2.0) + Math.pow((sY - eY).toDouble(), 2.0))
}