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
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
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
    var isShowLabel = true
        set(value) {
            field = value
            timeLabels.forEach { it.visibility = if (field) View.VISIBLE else View.GONE }
        }
    var currProgress = 0f
        set(value) {
            field = value
            intervalRate = currProgress / interval
        }
    var startTime = START_TIME
        set(value) {
            field = value
            interval = endTime - startTime
            if (raisedInitFlag) {
                // FIXME(jieyi): 2018/02/04 couldn't set the correct time.
                circleSeekBar.progress = startTime.toDouble() / endTime.toDouble() * 100
//                circleSeekBar.progress = currProgress.toDouble()
                start()
                stop()
//                circleSeekBar.playAnimator(interval.toLong())
//                circleSeekBar.stopAnimator()
            }
        }
    var endTime = END_TIME
        set(value) {
            field = value
            interval = endTime - startTime
            if (raisedInitFlag) {
                timeLabels[1].text = field.toTimeString()
                circleSeekBar.totalTime = field
            }
        }
    var remainedTime = END_TIME - START_TIME
    var src by Delegates.notNull<Int>()
    var interval by Delegates.notNull<Int>()
    var intervalRate by Delegates.notNull<Float>()
    /** For clicking running button. */
    var onClickEvent: ((view: RotatedCircleWithIconImageView, isPaused: Boolean) -> Unit)? = null
    var onChangeTime: ((view: RotatedCircleWithIconImageView, currTime: Int) -> Unit)? = null
    // The variable is for [CircularSeekBar]
    var progressColor = 0xFFFF7F50.toInt()
    var unprogressColor = 0xFFA9A9A9.toInt()
    var unpressBtnColor = 0xFFFFFFFF.toInt()
    var pressBtnColor = 0xFFD3D3D3.toInt()
    var progressWidth = WIDTH_OF_PROGRESS
    var btnRadius = BUTTON_RADIUS
    //endregion

    //region Progress bar components.
    var circleSeekBar by Delegates.notNull<CircularSeekBar>()
        private set
    var statusIcon by Delegates.notNull<ImageView>()
        private set
    var timeLabels by Delegates.notNull<List<TextView>>()
        private set
    private var rotatedCircleImageView by Delegates.notNull<RotatedCircleImageView>()
    //endregion

    private var currTime = 0  // For keeping passed time and calling callback or not.
    private var raisedInitFlag = false

    init {
        context.obtainStyledAttributes(attrs, R.styleable.RotatedCircleWithIconImageView, defStyleAttr, 0).apply {
            src = getResourceId(R.styleable.RotatedCircleWithIconImageView_src, 0)
            endTime = getInteger(R.styleable.RotatedCircleWithIconImageView_end_time, END_TIME)
            iconInactive = getInteger(R.styleable.RotatedCircleWithIconImageView_fore_icon, iconInactive)
            iconActive = getInteger(R.styleable.RotatedCircleWithIconImageView_running_icon, iconActive)
            progressWidth = getFloat(R.styleable.RotatedCircleWithIconImageView_progress_width, progressWidth)
            btnRadius = getFloat(R.styleable.RotatedCircleWithIconImageView_controller_radius, btnRadius)
            progressColor = getColor(R.styleable.RotatedCircleWithIconImageView_progress_color, progressColor)
            unprogressColor = getColor(R.styleable.RotatedCircleWithIconImageView_unprogress_color, unprogressColor)
            pressBtnColor = getColor(R.styleable.RotatedCircleWithIconImageView_unpress_controller_color, pressBtnColor)
            unpressBtnColor = getColor(R.styleable.RotatedCircleWithIconImageView_controller_color, unpressBtnColor)
        }.recycle()

        // Setting variables.
        startTime = START_TIME
        remainedTime = endTime - startTime
        rotatedCircleImageView = RotatedCircleImageView(context).apply {
            setImageResource(src)
            setPadding(INNER_PADDING, INNER_PADDING, INNER_PADDING, INNER_PADDING)
            setShadowRadius(0f)
            setBorderWidth(0f)
            onClickEvent = {
                this@RotatedCircleWithIconImageView.onClickEvent?.invoke(this@RotatedCircleWithIconImageView,
                                                                         isPauseState)
                ?: if (isPauseState) this@RotatedCircleWithIconImageView.stop() else this@RotatedCircleWithIconImageView.start()
            }
        }
        circleSeekBar = (attrs?.let {
            CircularSeekBar(context, attrs, defStyleAttr)
        } ?: CircularSeekBar(context)).also {
            it.progressColor = progressColor
            it.unprogressColor = unprogressColor
            it.pressBtnColor = pressBtnColor
            it.unpressBtnColor = unpressBtnColor
            it.progressWidth = progressWidth
            it.btnRadius = btnRadius
            it.totalTime = endTime
            it.onProgressChanged = { progress, remainedTime ->
                val passedTime = endTime - remainedTime
                val accordingProcessTime = endTime - progress * endTime / 100

                this.remainedTime = remainedTime
                // Fixed the time isn't correct when clicking the non-stop the button of play and stop.
                if (accordingProcessTime != remainedTime) {
                    this.remainedTime = accordingProcessTime
                }
                timeLabels[0].text = passedTime.toTimeString()
                // Callback only changing time.
                if (passedTime != currTime) {
                    onChangeTime?.invoke(this@RotatedCircleWithIconImageView, passedTime)
                }
                currTime = passedTime
            }
            it.onProgressFinished = {
                rotatedCircleImageView.stop()
                statusIcon.setImageResource(iconInactive)
            }
        }
        // Add children view into this group.
        addView(circleSeekBar)
        addView(rotatedCircleImageView)
        statusIcon = ImageView(context).apply {
            setImageDrawable(context.getDrawable(iconInactive))
        }.also { addView(it) }
        timeLabels = listOf(
            TextView(context).apply {
                setTextColor(0xFFA9A9A9.toInt())
                text = startTime.toTimeString()
            },
            TextView(context).apply {
                setTextColor(0xFFA9A9A9.toInt())
                text = endTime.toTimeString()
            }).also { it.forEach { v -> addView(v) } }
        currProgress = 0f

        // NOTE: is_show_label needs to process after `timeLabels` was initialed.
        context.obtainStyledAttributes(attrs, R.styleable.RotatedCircleWithIconImageView, defStyleAttr, 0).apply {
            isShowLabel = getBoolean(R.styleable.RotatedCircleWithIconImageView_time_label, isShowLabel)
        }

        raisedInitFlag = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Measure all of children's width & height.
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        // Measure width & height of this view_group's layout(layout_width or layout_height will be `match_parent`
        // no matter what we set `wrap_content` or `match_patent` when we're using getDefaultSize).
        // We'll reset this method by another way for achieving `wrap_content`.
        val square = minOf(getDefaultSize(suggestedMinimumWidth, widthMeasureSpec),
                           getDefaultSize(suggestedMinimumHeight, heightMeasureSpec))
        setMeasuredDimension(square, square)
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val w = width
        val h = height

        forEachChildWithIndex { index, view ->
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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        onClickEvent = null
        onChangeTime = null
    }

    fun start() {
        circleSeekBar.playAnimator(remainedTime.toLong())
        // Changing the icon by the state.
        statusIcon.setImageResource(iconActive)
        // Changing the state dependence state.
        rotatedCircleImageView.isPauseState.not()
    }

    fun stop() {
        circleSeekBar.stopAnimator()
        // Changing the icon by the state.
        statusIcon.setImageResource(iconInactive)
        // Changing the state dependence state.
        rotatedCircleImageView.isPauseState.not()
    }

    fun loading() {}

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
