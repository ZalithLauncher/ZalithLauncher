package com.movtery.zalithlauncher.ui.subassembly.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ToggleButton
import com.movtery.zalithlauncher.R
import com.kdt.DefocusableScrollView

class FloatingLoggerWindow(context: Context) {

    private val activity = context as Activity
    private val density = context.resources.displayMetrics.density

    private val defaultWidthDp = 360
    private val defaultHeightDp = 240

    private var minW = dp(180)
    private var minH = dp(120)
    private var maxW = 0
    private var maxH = 0

    private enum class Dir {
        TOP, BOTTOM, LEFT, RIGHT,
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    private val contentView: View = LayoutInflater.from(context)
        .inflate(R.layout.view_floating_logger, null)

    private val wrapper = FrameLayout(activity).apply {
        layoutParams = FrameLayout.LayoutParams(dp(defaultWidthDp), dp(defaultHeightDp))
        addView(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private val titleBar: View = contentView.findViewById(R.id.floating_log_titlebar)
    private val scrollView: DefocusableScrollView = contentView.findViewById(R.id.floating_log_scroll)
    private val logTextView: TextView = contentView.findViewById(R.id.floating_log_view)
    private val closeBtn: ImageButton = contentView.findViewById(R.id.floating_close_button)
    private val autoScrollBtn: ToggleButton = contentView.findViewById(R.id.floating_toggle_autoscroll)
    private val outputBtn: ToggleButton = contentView.findViewById(R.id.floating_toggle_log)

    private val handles: Map<Dir, View> = mapOf(
        Dir.TOP to contentView.findViewById(R.id.rz_top),
        Dir.BOTTOM to contentView.findViewById(R.id.rz_bottom),
        Dir.LEFT to contentView.findViewById(R.id.rz_left),
        Dir.RIGHT to contentView.findViewById(R.id.rz_right),
        Dir.TOP_LEFT to contentView.findViewById(R.id.rz_top_left),
        Dir.TOP_RIGHT to contentView.findViewById(R.id.rz_top_right),
        Dir.BOTTOM_LEFT to contentView.findViewById(R.id.rz_bottom_left),
        Dir.BOTTOM_RIGHT to contentView.findViewById(R.id.rz_bottom_right)
    )

    private var winX = 40f
    private var winY = 80f
    private var isDragging = false
    private var isResizing = false

    private val contentParent: FrameLayout by lazy {
        activity.window.decorView.findViewById<FrameLayout>(android.R.id.content)
    }

    init {
        setupDrag()
        setupAllResizeHandles()
        setupButtons()

        wrapper.x = winX
        wrapper.y = winY
        wrapper.visibility = View.GONE

        wrapper.post {
            maxW = (contentParent.width * 0.8).toInt().coerceAtLeast(minW)
            maxH = (contentParent.height * 0.8).toInt().coerceAtLeast(minH)
            clampPositionAndSize()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupDrag() {
        var dX = 0f
        var dY = 0f
        titleBar.setOnTouchListener { _, e ->
            if (isResizing) return@setOnTouchListener false
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = wrapper.x - e.rawX
                    dY = wrapper.y - e.rawY
                    wrapper.elevation = 12f * density
                    isDragging = true
                }
                MotionEvent.ACTION_MOVE -> {
                    val nx = (e.rawX + dX).coerceIn(0f, maxX())
                    val ny = (e.rawY + dY).coerceIn(0f, maxY())
                    wrapper.x = nx
                    wrapper.y = ny
                    winX = nx
                    winY = ny
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    wrapper.elevation = 8f * density
                    isDragging = false
                }
            }
            true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupAllResizeHandles() {
        handles.forEach { (dir, handle) ->
            var startRawX = 0f
            var startRawY = 0f
            var startW = 0
            var startH = 0
            var startX = 0f
            var startY = 0f

            handle.setOnTouchListener { _, e ->
                if (isDragging) return@setOnTouchListener false
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startRawX = e.rawX
                        startRawY = e.rawY
                        startW = wrapper.width
                        startH = wrapper.height
                        startX = wrapper.x
                        startY = wrapper.y
                        wrapper.elevation = 12f * density
                        isResizing = true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = e.rawX - startRawX
                        val dy = e.rawY - startRawY

                        var newW = startW
                        var newH = startH
                        var newX = startX
                        var newY = startY

                        when (dir) {
                            Dir.RIGHT -> newW = (startW + dx).toInt()
                            Dir.LEFT -> {
                                newW = (startW - dx).toInt()
                                newX = startX + dx
                            }
                            Dir.BOTTOM -> newH = (startH + dy).toInt()
                            Dir.TOP -> {
                                newH = (startH - dy).toInt()
                                newY = startY + dy
                            }
                            Dir.BOTTOM_RIGHT -> {
                                newW = (startW + dx).toInt()
                                newH = (startH + dy).toInt()
                            }
                            Dir.BOTTOM_LEFT -> {
                                newW = (startW - dx).toInt()
                                newH = (startH + dy).toInt()
                                newX = startX + dx
                            }
                            Dir.TOP_RIGHT -> {
                                newW = (startW + dx).toInt()
                                newH = (startH - dy).toInt()
                                newY = startY + dy
                            }
                            Dir.TOP_LEFT -> {
                                newW = (startW - dx).toInt()
                                newH = (startH - dy).toInt()
                                newX = startX + dx
                                newY = startY + dy
                            }
                        }

                        newW = newW.coerceIn(minW, maxW)
                        newH = newH.coerceIn(minH, maxH)

                        if (dir == Dir.LEFT || dir == Dir.BOTTOM_LEFT || dir == Dir.TOP_LEFT) {
                            newX = (startX + startW - newW).coerceIn(0f, maxX())
                        }
                        if (dir == Dir.TOP || dir == Dir.TOP_LEFT || dir == Dir.TOP_RIGHT) {
                            newY = (startY + startH - newH).coerceIn(0f, maxY())
                        }

                        newX = newX.coerceIn(0f, maxX())
                        newY = newY.coerceIn(0f, maxY())

                        applySize(newW, newH)
                        wrapper.x = newX
                        wrapper.y = newY
                        winX = newX
                        winY = newY
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        wrapper.elevation = 8f * density
                        isResizing = false
                    }
                }
                true
            }
        }
    }

    private fun setupButtons() {
        closeBtn.setOnClickListener { hide() }
        autoScrollBtn.setOnCheckedChangeListener { _, checked ->
            if (checked) scrollToBottom()
        }
    }

    private fun applySize(w: Int, h: Int) {
        val lp = wrapper.layoutParams
        if (lp != null) {
            lp.width = w
            lp.height = h
            wrapper.layoutParams = lp
        } else {
            wrapper.layoutParams = FrameLayout.LayoutParams(w, h)
        }
    }

    private fun maxX() = (contentParent.width - wrapper.width).toFloat().coerceAtLeast(0f)
    private fun maxY() = (contentParent.height - wrapper.height).toFloat().coerceAtLeast(0f)

    private fun clampPositionAndSize() {
        val w = wrapper.width.coerceIn(minW, maxW)
        val h = wrapper.height.coerceIn(minH, maxH)
        applySize(w, h)

        val x = wrapper.x.coerceIn(0f, maxX())
        val y = wrapper.y.coerceIn(0f, maxY())
        wrapper.x = x
        wrapper.y = y
        winX = x
        winY = y
    }

    private fun dp(value: Int) = (value * density).toInt()

    fun show() {
        if (wrapper.parent == null) {
            applySize(dp(defaultWidthDp), dp(defaultHeightDp))
            contentParent.addView(wrapper)
            wrapper.post { clampPositionAndSize() }
        }
        wrapper.visibility = View.VISIBLE
    }

    fun hide() {
        wrapper.visibility = View.GONE
    }

    fun toggle() {
        if (wrapper.visibility == View.VISIBLE) hide() else show()
    }

    val isVisible: Boolean get() = wrapper.visibility == View.VISIBLE

    fun isOutputEnabled(): Boolean = outputBtn.isChecked

    fun appendLog(text: String) {
        if (!isOutputEnabled()) return
        logTextView.append(text)
        if (autoScrollBtn.isChecked) scrollToBottom()
    }

    fun setLog(text: CharSequence) {
        logTextView.text = text
        if (autoScrollBtn.isChecked) scrollToBottom()
    }

    private fun scrollToBottom() {
        scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
    }
}
