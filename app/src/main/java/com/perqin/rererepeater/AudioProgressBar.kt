package com.perqin.rererepeater

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View


/**
 * @author perqin
 */
class AudioProgressBar : View {
    var min: Int = 0
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }
    var max: Int = 0
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }
    var durationStart: Int = 0
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }
    var durationEnd: Int = 0
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }
    var progress: Int = 0
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }
    var playedColor: Int = Color.BLACK
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }
    var restColor: Int = Color.BLACK
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }
    private val paint = Paint()

    constructor(context: Context) : super(context) {
        initView(context, null, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes) {
        initView(context, attrs, defStyleAttr, defStyleRes)
    }

    override fun onDraw(canvas: Canvas) {
        val top = paddingTop
        val bottom = height - paddingBottom
        if ((durationStart in min..progress) && (durationEnd in progress..max)) {
            // Played
            var left = paddingLeft + 1.0F * (durationStart - min) * (width - paddingLeft - paddingRight) / (max - min)
            var right = paddingLeft + 1.0F * (progress - min) * (width - paddingLeft - paddingRight) / (max - min)
            paint.color = playedColor
            canvas.drawRect(left, top.toFloat(), right, bottom.toFloat(), paint)
            // Rest
            left = right
            right = paddingLeft + 1.0F * (durationEnd - min) * (width - paddingLeft - paddingRight) / (max - min)
            paint.color = restColor
            canvas.drawRect(left, top.toFloat(), right, bottom.toFloat(), paint)
        }
    }

    private fun initView(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.AudioProgressBar, defStyleAttr, defStyleRes)
        try {
            min = a.getInteger(R.styleable.AudioProgressBar_min, 0)
            max = a.getInteger(R.styleable.AudioProgressBar_max, 0)
            durationStart = a.getInteger(R.styleable.AudioProgressBar_durationStart, 0)
            durationEnd = a.getInteger(R.styleable.AudioProgressBar_durationEnd, 0)
            progress = a.getInteger(R.styleable.AudioProgressBar_progress, 0)
            playedColor = a.getColor(R.styleable.AudioProgressBar_playedColor, Color.BLACK)
            restColor = a.getColor(R.styleable.AudioProgressBar_restColor, Color.BLACK)
        } finally {
            a.recycle()
        }
        paint.style = Paint.Style.FILL
    }
}
