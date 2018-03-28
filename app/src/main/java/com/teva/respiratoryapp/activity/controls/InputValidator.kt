//
// InputValidator/ app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.controls


import android.text.*
import android.text.method.NumberKeyListener
import android.view.KeyEvent
import android.view.View
import android.widget.TextView

/**
 * Validates an TextView's text against a pattern.
 */
open class InputValidator : NumberKeyListener(), InputFilter, TextWatcher {

    private var updating = false
    private var textDeleted = false
    private var characterSet = CharArray(0)

    /**
     * The current state of the input text validation.
     */
    var validationState: ValidationState = ValidationState.INCOMPLETE
        set(value) {
            field = value
            validationListener?.onValidationChanged(value)
        }

    /**
     * The validation state changed listener.
     */
    var validationListener: ValidationListener? = null

    /**
     * The color used to highlight validation errors
     */
    var highlightColor: Int = 0xffff0000.toInt()

    /**
     * The pattern to validate the input text against.
     */
    var pattern: String? = null
        set(value) {
            field = value
            updateAcceptedCharacters()
        }

    /**
     * Retrieves the set of accepted characters.
     */
    override fun getAcceptedChars(): CharArray {
        return characterSet
    }

    /**
     * Updates the set of characters accepted by the pattern.
     */
    private fun updateAcceptedCharacters() {

        characterSet = pattern?.let { patternStr ->
            val list = patternStr
                    .toList()
                    .filterNot { it == '#' }
                    .distinct()
                    .toMutableList()

            if (patternStr.contains('#')) {
                list.addAll(DIGITS.toList())
            }

            list.toCharArray()
        } ?: CharArray(0)
    }

    /**
     * Called after the text has been changed.
     *
     * @param editable The current text of the EditText control.
     */
    override fun afterTextChanged(editable: Editable?) {
        if (updating || (editable == null)) {
            return
        }

        // Indicate that the EditText's text is being updated to
        // prevent an endless loop.
        updating = true

        try {
            pattern?.let { patternStr ->
                // compare the current text to the pattern and update it if necessary.
                var index = 0
                while (index < editable.length) {
                    if (index >= patternStr.length) {
                        editable.delete(index, index + 1)
                    } else {
                        val cPattern = patternStr[index]
                        val c = editable[index]
                        if (cPattern == '#') {
                            if (c.isDigit()) {
                                index++
                            } else {
                                editable.delete(index, index + 1)
                            }
                        } else {
                            if (c == cPattern) {
                                index++
                            } else {
                                editable.insert(index, cPattern.toString())
                            }
                        }
                    }
                }

                // if text was added, then append any pattern characters
                // that follow the text.
                if (!textDeleted) {
                    index = editable.length
                    while (index < patternStr.length) {
                        val cPattern = patternStr[index]
                        if (cPattern == '#') {
                            break
                        }

                        editable.append(cPattern.toString())
                        index++
                    }
                }

                validate(editable)
            }
        } finally {
            updating = false
        }
    }

    /**
     * Implemented by derived classes to perform specific validation.
     */
    protected open fun validate(editable: Editable) {}

    /**
     * Called before the text is changed.
     *
     * @param s The current text.
     * @param start The start index of the change.
     * @param countBefore The number of characters being replaced.
     * @param countAfter The number of characters that will replace the source characters.
     */
    override fun beforeTextChanged(
            s: CharSequence?,
            start: Int,
            countBefore: Int,
            countAfter: Int) {
        // detect whether text is being added or deleted
        if (!updating) {
            textDeleted = countAfter < countBefore
        }
    }

    /**
     * Called when text is changed.
     *
     * @param s The current text.
     * @param start The start index of the change.
     * @param countBefore The number of characters being replaced.
     * @param countAfter The number of characters that will replace the source characters.
     */
    override fun onTextChanged(
            s: CharSequence?,
            start: Int,
            countBefore: Int,
            countAfter: Int) {
    }

    /**
     * Checks whether the specified character is a wildcard character.
     *
     * @param c The character to check.
     */
    private fun isWildCard(c: Char): Boolean = c == '#'

    /**
     * Return the type of text that this key listener is manipulating,
     * as per [android.text.InputType].  This is used to
     * determine the mode of the soft keyboard that is shown for the editor.
     */
    override fun getInputType(): Int = InputType.TYPE_CLASS_NUMBER

    /**
     * Performs the action that happens when you press the [KeyEvent.KEYCODE_DEL] key in
     * a [TextView].  If there is a selection, deletes the selection; otherwise,
     * deletes the character before the cursor, if any; ALT+DEL deletes everything on
     * the line the cursor is on.
     *
     * @return true if anything was deleted; false otherwise.
     */
    override fun backspace(view: View?, content: Editable?, keyCode: Int, event: KeyEvent?): Boolean {
        val currentCursorOffset = Selection.getSelectionStart(content)

        // If there no pattern or is a selection, do normal deletion
        if ( pattern.isNullOrEmpty() ||
                content == null ||
                (currentCursorOffset == 0) ||
                (currentCursorOffset != Selection.getSelectionEnd(content))) {
            return super.backspace(view, content, keyCode, event)
        }

        // otherwise delete past pattern characters
        pattern?.let { patternStr ->
            var deleteFrom = currentCursorOffset - 1
            while((deleteFrom > 0) && !isWildCard(patternStr[deleteFrom]))
            {
                deleteFrom--
            }

            content.delete(deleteFrom, currentCursorOffset)
        }

        return true
    }

    /**
     * Performs the action that happens when you press the [KeyEvent.KEYCODE_FORWARD_DEL]
     * key in a [TextView].  If there is a selection, deletes the selection; otherwise,
     * deletes the character before the cursor, if any; ALT+FORWARD_DEL deletes everything on
     * the line the cursor is on.
     *
     * @return true if anything was deleted; false otherwise.
     */
    override fun forwardDelete(view: View?, content: Editable?, keyCode: Int, event: KeyEvent?): Boolean {
        val currentCursorOffset = Selection.getSelectionStart(content)

        // If there no pattern or is a selection, do normal deletion
        if ( pattern.isNullOrEmpty() ||
                content == null ||
                (currentCursorOffset == content.length) ||
                (currentCursorOffset != Selection.getSelectionEnd(content))) {
            return super.forwardDelete(view, content, keyCode, event)
        }

        // otherwise delete past pattern characters
        pattern?.let { patternStr ->
            var deleteTo = currentCursorOffset + 1
            while((deleteTo > content.length) && !isWildCard(patternStr[deleteTo-1]))
            {
                deleteTo++
            }

            content.delete(currentCursorOffset, deleteTo)
        }

        return true
    }

    /**
     * An enumeration of the validation states.
     */
    enum class ValidationState {
        INCOMPLETE,
        IN_ERROR,
        VALID
    }

    /**
     * Listener interface used to notify clients when the validation state changes.
     */
    interface ValidationListener {
        fun onValidationChanged(validationState: ValidationState)
    }

    companion object {
        private val DIGITS = "0123456789"
    }
}
