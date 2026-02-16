package me.debaxom.disang.ui

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import me.debaxom.disang.layout.KeyboardLayoutEngine
import me.debaxom.disang.model.KeyboardLayouts
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class KeyboardCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var onKeyPressed: ((Int) -> Unit)? = null
    var onKeyRepeat: ((Int) -> Unit)? = null
    var onSuggestionClicked: ((String) -> Unit)? = null
    var onToggleClicked: (() -> Unit)? = null

    private val engine = KeyboardLayoutEngine()
    private var rows = KeyboardLayouts.letters()

    private val density = resources.displayMetrics.density
    private val topBarHeight = 50f * density
    private val gap = 3f * density
    private val radius = 12f * density

    private val hitboxExpandX = 8f * density
    private val hitboxExpandY = 4f * density
    private val driftLockRadius = 18f * density

    var isShifted = false
    set(value) {
        field = value
        invalidateKeys()
    }

    var isCapsLock = false
    set(value) {
        field = value
        invalidateKeys()
    }

    // ------------------------------------------------
    // MULTI TOUCH
    // ------------------------------------------------
    private data class PointerState(
        var keyCode: Int,
        var startX: Float,
        var startY: Float
    )

    private val activePointers = mutableMapOf<Int, PointerState>()

    private fun isKeyPressed(code: Int): Boolean {
        return activePointers.values.any { it.keyCode == code }
    }

    // ------------------------------------------------
    // Layout caching
    // ------------------------------------------------
    private var layoutDirty = true

    private fun recomputeLayoutIfNeeded() {
        if (!layoutDirty || width == 0 || height == 0) return

        val rowsCount = rows.size
        val availableHeight = height - topBarHeight - gap
        val keyHeight =
            (availableHeight - (rowsCount - 1) * gap) / rowsCount

        engine.layout(rows, width, keyHeight, gap, topBarHeight + gap)

        layoutDirty = false
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        layoutDirty = true
    }

    // ------------------------------------------------
    // Partial invalidation
    // ------------------------------------------------
    private fun invalidateTopBar() {
        invalidate(0, 0, width, topBarHeight.toInt())
    }

    private fun invalidateKeys() {
        invalidate(0, topBarHeight.toInt(), width, height)
    }

    private fun invalidateKey(bounds: RectF) {
        invalidate(
            bounds.left.toInt(),
            bounds.top.toInt(),
            bounds.right.toInt(),
            bounds.bottom.toInt()
        )
    }

    // ------------------------------------------------
    // Toggle
    // ------------------------------------------------
    private var transliterationEnabled = true

    fun setTransliterationEnabled(enabled: Boolean) {
        transliterationEnabled = enabled
        invalidateTopBar()
    }

    // ------------------------------------------------
    // Suggestions
    // ------------------------------------------------
    private var suggestions: List<String> = emptyList()
    private val suggestionRects = mutableListOf<Pair<RectF, String>>()
    private var suggestionMoved = false
    private var topBarTapCandidate = false

    private var suggestionScrollX = 0f
    private var lastTouchX = 0f
    private var isScrollingSuggestions = false
    private var totalSuggestionWidth = 0f

    fun setSuggestions(list: List<String>) {
        suggestions = list
        suggestionScrollX = 0f
        invalidateTopBar()
    }

    // ------------------------------------------------
    // Backspace acceleration
    // ------------------------------------------------
    private val handler = Handler(Looper.getMainLooper())
    private var repeatingCode: Int? = null
    private var repeatStartTime = 0L

    private val BASE_DELAY = 120L
    private val MIN_DELAY = 28L

    private val repeatRunnable = object : Runnable {
        override fun run() {

            val code = repeatingCode ?: return

            val elapsed =
                SystemClock.elapsedRealtime() - repeatStartTime

            val t = (elapsed / 3000f).coerceIn(0f, 1f)
            val eased = 1f - (1f - t) * (1f - t)

            val delay =
                (BASE_DELAY - (BASE_DELAY - MIN_DELAY) * eased).toLong()

            val burstCount = when {
                t < 0.35f -> 1
                t < 0.7f -> 2
                else -> 3
            }

            repeat(burstCount) {
                onKeyRepeat?.invoke(code)
            }

            handler.postDelayed(this, delay)
        }
    }

    // ------------------------------------------------
    // Paints
    // ------------------------------------------------
    private val backgroundPaint = Paint().apply {
        color = Color.parseColor("#202124")
    }

    private val topBarPaint = Paint().apply {
        color = Color.parseColor("#1A1B1C")
    }

    private val keyPaint = Paint().apply {
        color = Color.parseColor("#2D2E30")
    }

    private val specialKeyPaint = Paint().apply {
        color = Color.parseColor("#3A3B3D")
    }

    private val pressedKeyPaint = Paint().apply {
        color = Color.parseColor("#4A4B4D")
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 18f * density
        textAlign = Paint.Align.CENTER
    }

    private val fadeWidth = 18f * density

    // ⭐ smaller & darker spacebar text
    private val spaceTextPaint = Paint(textPaint).apply {
        textSize = 14f * density
        color = Color.parseColor("#B0B0B0")
    }

    private val textCenterOffset =
        (textPaint.descent() + textPaint.ascent()) / 2f

    private val spaceTextOffset =
        (spaceTextPaint.descent() + spaceTextPaint.ascent()) / 2f

    private val suggestionPaint = Paint().apply {
        color = Color.WHITE
        textSize = 17f * density
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    // ------------------------------------------------
    // Special key detection
    // ------------------------------------------------
    private fun isSpecialKey(code: Int): Boolean {
        return code == -1 || code == -5 || code == -101 || code == 10
    }

    // ------------------------------------------------
    // Layout switching
    // ------------------------------------------------
    fun showLetters() {
        rows = KeyboardLayouts.letters()
        layoutDirty = true
        invalidate()
    }

    fun showSymbolsPage1() {
        rows = KeyboardLayouts.symbolsPage1()
        layoutDirty = true
        invalidate()
    }

    fun showSymbolsPage2() {
        rows = KeyboardLayouts.symbolsPage2()
        layoutDirty = true
        invalidate()
    }

    // ------------------------------------------------
    // DRAW
    // ------------------------------------------------
    override fun onDraw(canvas: Canvas) {

        recomputeLayoutIfNeeded()

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        canvas.drawRect(0f, 0f, width.toFloat(), topBarHeight, topBarPaint)

        val toggleSize = topBarHeight - 12f
        val toggleRect = RectF(6f, 6f, 6f + toggleSize, 6f + toggleSize)

        suggestionRects.clear()

        val startX = toggleRect.right + 10f
        val chipGap = 4f * density
        val chipPad = 12f * density

        // ------------------------------------------------
        // CLIP suggestion area (FIX)
        // ------------------------------------------------
        canvas.save()

        canvas.clipRect(
            startX,
            0f,
            width.toFloat(),
            topBarHeight
        )

        var x = startX - suggestionScrollX
        totalSuggestionWidth = 0f

        suggestions.forEach { s ->

            val w = suggestionPaint.measureText(s) + chipPad * 2
            val rect = RectF(x, 6f, x + w, topBarHeight - 6f)

            canvas.drawRoundRect(rect, 16f, 16f, keyPaint)

            canvas.drawText(
                s,
                rect.centerX(),
                rect.centerY() - textCenterOffset,
                suggestionPaint
            )

            suggestionRects.add(rect to s)

            x += w + chipGap
            totalSuggestionWidth += w + chipGap
        }

        canvas.restore()


        canvas.drawRoundRect(toggleRect, 10f, 10f, keyPaint)

        val label = if (transliterationEnabled) "As" else "En"

        val toggleTextPaint = Paint(textPaint).apply {
            color = if (transliterationEnabled)
                Color.parseColor("#0099ff")
            else Color.WHITE
            textSize = 16f * density
            isFakeBoldText = true
        }

        canvas.drawText(
            label,
            toggleRect.centerX(),
            toggleRect.centerY() - textCenterOffset,
            toggleTextPaint
        )

        rows.flatten().forEach { key ->

            val paint =
                if (isKeyPressed(key.code)) {
                    pressedKeyPaint
                } else {
                    if (isSpecialKey(key.code))
                        specialKeyPaint
                    else
                        keyPaint
                }

            canvas.drawRoundRect(key.bounds, radius, radius, paint)

            val text = when {
                key.code == -1 && isCapsLock -> "⇪"
                isShifted && key.code in 97..122 ->
                    key.label.uppercase()
                else -> key.label
            }

            val isSpace = key.code == 32
            val tp = if (isSpace) spaceTextPaint else textPaint
            val offset = if (isSpace) spaceTextOffset else textCenterOffset

            canvas.drawText(
                text,
                key.bounds.centerX(),
                key.bounds.centerY() - offset,
                tp
            )
        }
    }

    // ------------------------------------------------
    // Hitbox lookup
    // ------------------------------------------------
    private fun findTouchedKey(x: Float, y: Float) =
        rows.flatten().minByOrNull { key ->
            val expanded = RectF(
                key.bounds.left - hitboxExpandX,
                key.bounds.top - hitboxExpandY,
                key.bounds.right + hitboxExpandX,
                key.bounds.bottom + hitboxExpandY
            )

            if (!expanded.contains(x, y)) Float.MAX_VALUE
            else {
                val dx = key.bounds.centerX() - x
                val dy = key.bounds.centerY() - y
                dx * dx + dy * dy
            }
        }

    // ------------------------------------------------
    // TOUCH
    // ------------------------------------------------
    override fun onTouchEvent(event: MotionEvent): Boolean {

        val toggleSize = topBarHeight - 12f
        val toggleRect = RectF(6f, 6f, 6f + toggleSize, 6f + toggleSize)

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_POINTER_DOWN -> {

                val index = event.actionIndex
                val pointerId = event.getPointerId(index)

                val x = event.getX(index)
                val y = event.getY(index)

                if (toggleRect.contains(x, y)) {
                    onToggleClicked?.invoke()
                    return true
                }

                if (y <= topBarHeight) {
                    isScrollingSuggestions = true
                    topBarTapCandidate = true
                    suggestionMoved = false
                    lastTouchX = x
                    return true
                }

                val key = findTouchedKey(x, y)

                key?.let {
                    activePointers[pointerId] =
                        PointerState(it.code, x, y)

                    invalidateKey(it.bounds)
                    post { onKeyPressed?.invoke(it.code) }

                    if (it.code == -5) {
                        repeatingCode = -5
                        repeatStartTime = SystemClock.elapsedRealtime()
                        handler.postDelayed(repeatRunnable, 300)
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {

                if (isScrollingSuggestions) {

                    val dx = event.x - lastTouchX

                    if (kotlin.math.abs(dx) > 2f) {
                        suggestionMoved = true
                    }

                    suggestionScrollX -= dx

                    val toggleSize = topBarHeight - 12f
                    val toggleRight = 6f + toggleSize

                    val startX = toggleRight + 10f
                    val visibleSuggestionWidth = width - startX

                    val maxScroll = max(0f, totalSuggestionWidth - visibleSuggestionWidth)

                    suggestionScrollX =
                        min(max(0f, suggestionScrollX), maxScroll)

                    lastTouchX = event.x
                    invalidateTopBar()
                    return true
                }

                for (i in 0 until event.pointerCount) {

                    val pointerId = event.getPointerId(i)
                    val state = activePointers[pointerId] ?: continue

                    val x = event.getX(i)
                    val y = event.getY(i)

                    val dx = x - state.startX
                    val dy = y - state.startY

                    val distance = sqrt(dx * dx + dy * dy)

                    if (distance < driftLockRadius) continue

                    val newKey = findTouchedKey(x, y) ?: continue

                    if (newKey.code != state.keyCode) {

                        rows.flatten()
                            .firstOrNull { it.code == state.keyCode }
                            ?.bounds?.let { invalidateKey(it) }

                        state.keyCode = newKey.code
                        state.startX = x
                        state.startY = y

                        invalidateKey(newKey.bounds)
                        post { onKeyPressed?.invoke(newKey.code) }
                    }
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_CANCEL -> {

                val index = event.actionIndex
                val pointerId = event.getPointerId(index)

                val state = activePointers.remove(pointerId)

                state?.let {
                    rows.flatten()
                        .firstOrNull { k -> k.code == it.keyCode }
                        ?.bounds?.let { b -> invalidateKey(b) }
                }

                if (activePointers.isEmpty()) {
                    repeatingCode = null
                    handler.removeCallbacks(repeatRunnable)
                }

                if (isScrollingSuggestions &&
                    topBarTapCandidate &&
                    !suggestionMoved) {

                    val x = event.x
                    val y = event.y

                    suggestionRects.firstOrNull {
                        it.first.contains(x, y)
                    }?.let {
                        onSuggestionClicked?.invoke(it.second)
                    }
                }

                topBarTapCandidate = false
                isScrollingSuggestions = false
            }
        }

        return true
    }
}
