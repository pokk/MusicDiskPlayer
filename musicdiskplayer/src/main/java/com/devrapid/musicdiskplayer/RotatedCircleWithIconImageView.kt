package com.devrapid.musicdiskplayer

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlin.properties.Delegates

/**
 * Circle image view with shadow.
 *
 * @author  jieyi
 * @since   6/19/17
 */
class RotatedCircleWithIconImageView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0):
    ViewGroup(context, attrs, defStyleAttr) {
    companion object {
        private const val INNER_PADDING = 60
        private const val TEXT_OFFSET = 20
        private const val START_TIME = 0
        private const val END_TIME = 0
        private const val WIDTH_OF_PROGRESS = 13f
        private const val BUTTON_RADIUS = 25f
    }

    //region Variables for setting
    var iconInactive = R.drawable.ic_play_arrow
    var iconActive = R.drawable.ic_pause
    var currProgress = 0f
        set(value) {
            field = value
            this.intervalRate = this.currProgress / this.interval
        }
    var startTime = START_TIME
        set(value) {
            field = value
            this.interval = this.endTime - this.startTime
        }
    var endTime = END_TIME
        set(value) {
            field = value
            this.interval = this.endTime - this.startTime
        }
    var remainedTime = END_TIME - START_TIME
    var src by Delegates.notNull<Int>()
    var interval by Delegates.notNull<Int>()
    var intervalRate by Delegates.notNull<Float>()
    // The variable is for [CircularSeekBar]
    var progressColor = 0xFFFF7F50.toInt()
    var unprogressColor = 0xFFA9A9A9.toInt()
    var unpressBtnColor = 0xFFFFFFFF.toInt()
    var pressBtnColor = 0xFFD3D3D3.toInt()
    var progressWidth: Float = WIDTH_OF_PROGRESS
    var btnRadius: Float = BUTTON_RADIUS
    //endregion

    //region Progress bar components.
    var rotatedCircleImageView: RotatedCircleImageView
        private set
    lateinit var circleSeekBar: CircularSeekBar
        private set
    lateinit var statusIcon: ImageView
        private set
    lateinit var timeLabels: List<TextView>
        private set
    //endregion

    init {
        context.obtainStyledAttributes(attrs, R.styleable.RotatedCircleWithIconImageView, defStyleAttr, 0).also {
            this.src = it.getResourceId(R.styleable.RotatedCircleWithIconImageView_src, 0)
            this.endTime = it.getInteger(R.styleable.RotatedCircleWithIconImageView_end_time, END_TIME)
            this.iconInactive = it.getInteger(R.styleable.RotatedCircleWithIconImageView_fore_icon, this.iconInactive)
            this.iconActive = it.getInteger(R.styleable.RotatedCircleWithIconImageView_running_icon, this.iconActive)
        }.recycle()

        // Setting variables.
        this.startTime = START_TIME
        this.remainedTime = this.endTime - this.startTime
        this.rotatedCircleImageView = RotatedCircleImageView(context).apply {
            setImageResource(this@RotatedCircleWithIconImageView.src)
            setPadding(INNER_PADDING, INNER_PADDING, INNER_PADDING, INNER_PADDING)
            setShadowRadius(0f)
            setBorderWidth(0f)
            onClickEvent = {
                val icon = if (this.isPauseState) {
                    this@RotatedCircleWithIconImageView.circleSeekBar.stopAnimator()
                    this@RotatedCircleWithIconImageView.iconInactive
                }
                else {
                    this@RotatedCircleWithIconImageView.circleSeekBar.playAnimator(this@RotatedCircleWithIconImageView.remainedTime.toLong())
                    this@RotatedCircleWithIconImageView.iconActive
                }
                // Changing the icon by the state.
                this@RotatedCircleWithIconImageView.statusIcon.setImageResource(icon)
                // Changing the state dependence state.
                this.isPauseState.not()
            }
        }
        this.circleSeekBar = (attrs?.let {
            CircularSeekBar(context, attrs, defStyleAttr)
        } ?: CircularSeekBar(context)).also {
            it.progressColor = this.progressColor
            it.unprogressColor = this.unprogressColor
            it.pressBtnColor = this.pressBtnColor
            it.unpressBtnColor = this.unpressBtnColor
            it.progressWidth = this.progressWidth
            it.btnRadius = this.btnRadius
            it.totalTime = this.endTime
            it.onProgressChanged = { progress, remainedTime ->
                val passedTime = this.endTime - remainedTime
                val accordingProcessTime = endTime - progress * this.endTime / 100

                this.remainedTime = remainedTime
                // Fixed the time isn't correct when clicking the non-stop the button of play and stop.
                if (accordingProcessTime != remainedTime) {
                    this.remainedTime = accordingProcessTime
                }
                this.timeLabels[0].text = passedTime.toTimeString()
            }
            it.onProgressFinished = {
                this.rotatedCircleImageView.stop()
                this.statusIcon.setImageResource(this.iconInactive)
            }
        }
        // Add children view into this group.
        this.addView(this.circleSeekBar)
        this.addView(this.rotatedCircleImageView)
        this.statusIcon = ImageView(this.context).apply {
            setImageDrawable(context.getDrawable(iconInactive))
        }.also { addView(it) }
        this.timeLabels = listOf(
            TextView(this.context).apply {
                setTextColor(0xFFA9A9A9.toInt())
                text = this@RotatedCircleWithIconImageView.startTime.toTimeString()
            },
            TextView(this.context).apply {
                setTextColor(0xFFA9A9A9.toInt())
                text = this@RotatedCircleWithIconImageView.endTime.toTimeString()
            }).also { it.forEach { v -> this.addView(v) } }
        this.currProgress = 0f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Measure all of children's width & height.
        this.measureChildren(widthMeasureSpec, heightMeasureSpec)
        // Measure width & height of this view_group's layout(layout_width or layout_height will be `match_parent`
        // no matter what we set `wrap_content` or `match_patent` when we're using getDefaultSize).
        // We'll reset this method by another way for achieving `wrap_content`.
        val square = minOf(getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
            getDefaultSize(suggestedMinimumHeight, heightMeasureSpec))
        this.setMeasuredDimension(square, square)
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val w = this.width
        val h = this.height

        this.forEachChildWithIndex { index, view ->
            val childW = view.measuredWidth
            val childH = view.measuredHeight
            val px = pivotX.toInt()
            val py = pivotX.toInt()

            val (l, t, r, b) = when (index) {
            // Circular seek bar.
                0 -> Rect(0, 0, childW, childH)
            // 1: Inner image view and 2: Status icon.
                1 -> Rect(px - childW / 2 + INNER_PADDING,
                    py - childH / 2 + INNER_PADDING,
                    px + childW / 2 - INNER_PADDING,
                    py + childH / 2 - INNER_PADDING)
                2 -> Rect(px - childW / 2, py - childH / 2, px + childW / 2, py + childH / 2)
            // Two text views.
                3 -> Rect(w / 4 - childW / 2, (h - childH - TEXT_OFFSET), w / 4 + childW / 2, (h - TEXT_OFFSET))
                4 -> Rect(w / 4 * 3 - childW / 2, (h - childH - TEXT_OFFSET), w / 4 * 3 + childW / 2, (h - TEXT_OFFSET))
                else -> Rect(0, 0, 0, 0)
            }

            view.layout(l, t, r, b)
        }
    }

    data class Rect(val l: Int, val t: Int, val r: Int, val b: Int)

    /**
     * Execute [action] for each child of the received [ViewGroup].
     *
     * @param action the action to execute. The first index is 0.
     */
    private inline fun ViewGroup.forEachChildWithIndex(action: (Int, View) -> Unit) {
        for (i in 0..childCount - 1) {
            action(i, getChildAt(i))
        }
    }

    private fun Int.format(digits: Int) = String.format("%0${digits}d", this)

    private inline fun Int.toTimeString(): String = "${(this / 60).format(2)}:${(this % 60).format(2)}"
}