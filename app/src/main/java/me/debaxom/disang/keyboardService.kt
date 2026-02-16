package me.debaxom.disang

import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputConnection
import me.debaxom.disang.ui.KeyboardCanvasView

class keyboardService : InputMethodService() {

    private lateinit var keyboardView: KeyboardCanvasView

    private val transliterationEngine = TransliterationEngine()

    private var transliterationEnabled = true

    // ⭐ shift states
    private var isCaps = false          // one-shot shift
    private var capsLock = false        // locked shift
    private var lastShiftTime = 0L

    private val currentWord = StringBuilder()

    private val uiHandler = Handler(Looper.getMainLooper())

    private var lastSuggestedWord = ""
    private val suggestionRunnable = Runnable {
        updateSuggestionsInternal()
    }

    override fun onCreateInputView(): View {

        val root = layoutInflater.inflate(R.layout.ime_root, null)

        keyboardView = root.findViewById(R.id.customKeyboard)

        keyboardView.setTransliterationEnabled(transliterationEnabled)

        keyboardView.onToggleClicked = {
            transliterationEnabled = !transliterationEnabled
            keyboardView.setTransliterationEnabled(transliterationEnabled)
            updateSuggestions()
        }

        keyboardView.onKeyPressed = { code ->
            handleKey(code)
        }

        keyboardView.onKeyRepeat = { code ->
            if (code == -5) {
                val ic = currentInputConnection
                if (ic != null) {

                    ic.deleteSurroundingText(1, 0)

                    if (currentWord.isNotEmpty()) {
                        currentWord.deleteCharAt(currentWord.length - 1)
                    }

                    updateSuggestions()
                }
            }
        }

        keyboardView.onSuggestionClicked = { suggestion ->
            applySuggestion(suggestion)
        }

        root.setOnApplyWindowInsetsListener { v, insets ->
            val bottomInset = insets.systemWindowInsetBottom
            v.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                bottomInset
            )
            insets
        }

        return root
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)

        // clear typing state
        currentWord.clear()
        lastSuggestedWord = ""

        // clear suggestion UI
        keyboardView.setSuggestions(emptyList())

        // reset shift if not locked
        if (!capsLock) {
            isCaps = false
            keyboardView.isShifted = false
        }
    }

    override fun onUpdateSelection(oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int,candidatesStart: Int, candidatesEnd: Int) {super.onUpdateSelection( oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)

        val ic = currentInputConnection ?: return

        if (!transliterationEnabled) {
            keyboardView.setSuggestions(emptyList())
            return
        }

        // ALWAYS rebuild from editor when cursor moves
        val word = getCurrentWordFromEditor(ic)

        currentWord.clear()

        if (word.isEmpty()) {
            keyboardView.setSuggestions(emptyList())
            lastSuggestedWord = ""
            return
        }

        currentWord.append(word)

        // ⭐ FORCE suggestions (no cache)
        val suggestions = transliterationEngine.getSuggestions(word)
        keyboardView.setSuggestions(suggestions)
        lastSuggestedWord = word

    }

    // ------------------------------------------------
    // FAST INPUT PIPELINE
    // ------------------------------------------------
    private fun handleKey(code: Int) {

        val ic: InputConnection = currentInputConnection ?: return

        when (code) {

            // ---------------- Backspace ----------------
            -5 -> {
                ic.deleteSurroundingText(1, 0)

                if (currentWord.isNotEmpty())
                    currentWord.deleteCharAt(currentWord.length - 1)

                updateSuggestions()
            }

            // ---------------- SHIFT / CAPS LOCK ----------------
            -1 -> {

                val now = SystemClock.elapsedRealtime()

                if (capsLock) {
                    // tap while locked -> OFF
                    capsLock = false
                    isCaps = false

                } else if (now - lastShiftTime < 350) {
                    // double tap -> CAPS LOCK
                    capsLock = true
                    isCaps = true

                } else {
                    // single tap -> one-shot shift
                    isCaps = !isCaps
                }

                lastShiftTime = now

                keyboardView.isShifted = isCaps
                keyboardView.isCapsLock = capsLock
            }

            // ---------------- Layout ----------------
            -101 -> keyboardView.showSymbolsPage1()
            -102 -> keyboardView.showLetters()
            -103 -> keyboardView.showSymbolsPage2()
            -104 -> keyboardView.showSymbolsPage1()

            // ---------------- Enter ----------------
            10 -> {
                applyTransliteration(ic)
                currentWord.clear()
                keyboardView.setSuggestions(emptyList())
                lastSuggestedWord = ""

                ic.sendKeyEvent(
                    KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
                )
            }

            // ---------------- Space ----------------
            32 -> {
                applyTransliteration(ic)
                currentWord.clear()
                keyboardView.setSuggestions(emptyList())
                lastSuggestedWord = ""

                ic.commitText(" ", 1)

                if (isCaps && !capsLock) {
                    isCaps = false
                    keyboardView.isShifted = false
                }
            }

            // ---------------- Normal Keys ----------------
            else -> {

                var c = code
                if (isCaps && c in 97..122) c -= 32

                val ch = c.toChar()

                uiHandler.post {
                    currentInputConnection?.commitText(ch.toString(), 1)
                }

                if (isTransliterationChar(ch)) {
                    currentWord.append(ch.lowercaseChar())
                    updateSuggestions()
                } else {
                    currentWord.clear()
                    keyboardView.setSuggestions(emptyList())
                }

                // one-shot shift auto off
                if (isCaps && !capsLock && c in 65..90) {
                    isCaps = false
                    keyboardView.isShifted = false
                }
            }
        }
    }

    // ------------------------------------------------
    // WORD EXTRACTION
    // ------------------------------------------------
    private fun getCurrentWordFromEditor(ic: InputConnection): String {
        val before = ic.getTextBeforeCursor(120, 0)?.toString() ?: ""
        return before.takeLastWhile { isTransliterationChar(it) }
    }

    // ------------------------------------------------
    // Suggestions
    // ------------------------------------------------
    private fun updateSuggestions() {

        val ic = currentInputConnection ?: return

        if (!transliterationEnabled) {
            keyboardView.setSuggestions(emptyList())
            return
        }

        var word = currentWord.toString()

        if (word.isEmpty()) {
            word = getCurrentWordFromEditor(ic)
        }

        if (word.isEmpty()) {
            keyboardView.setSuggestions(emptyList())
            return
        }

        val suggestions = transliterationEngine.getSuggestions(word)
        keyboardView.setSuggestions(suggestions)
    }

    private fun updateSuggestionsInternal() {

        if (!transliterationEnabled || currentWord.isEmpty()) {
            keyboardView.setSuggestions(emptyList())
            lastSuggestedWord = ""
            return
        }

        val word = currentWord.toString()

        if (word == lastSuggestedWord) return
        lastSuggestedWord = word

        val suggestions = transliterationEngine.getSuggestions(word)

        keyboardView.setSuggestions(suggestions)
    }

    // ------------------------------------------------
    // Suggestion click
    // ------------------------------------------------
    private fun applySuggestion(suggestion: String) {

        val ic = currentInputConnection ?: return

        val word = getCurrentWordFromEditor(ic)
        if (word.isEmpty()) return

        ic.deleteSurroundingText(word.length, 0)
        ic.commitText("$suggestion ", 1)

        currentWord.clear()
        keyboardView.setSuggestions(emptyList())
    }

    // ------------------------------------------------
    // Transliteration
    // ------------------------------------------------
    private fun isTransliterationChar(c: Char): Boolean {
        return c.isLetterOrDigit() ||
            c == '.' ||
            c == '\'' ||
            c == '-' ||
            c == '_'
    }

    private fun applyTransliteration(ic: InputConnection) {

        if (!transliterationEnabled) return

        val beforeCursor = ic.getTextBeforeCursor(100, 0)?.toString() ?: return

        val word = beforeCursor.takeLastWhile { isTransliterationChar(it) }

        if (word.isEmpty()) return

        val assamese = transliterationEngine.getSuggestions(word).firstOrNull() ?: word

        ic.deleteSurroundingText(word.length, 0)
        ic.commitText(assamese, 1)
    }
}
