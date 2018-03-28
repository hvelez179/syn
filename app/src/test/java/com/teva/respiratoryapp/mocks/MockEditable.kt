package com.teva.respiratoryapp.mocks

import android.text.Editable
import android.text.InputFilter
import java.util.stream.IntStream

/**
 * A unit testable implementation of Editable.
 */
class MockEditable(initialString: String = "") : Editable {

    private val stringBuilder = StringBuilder(initialString)

    override fun chars(): IntStream {
        return stringBuilder.chars()
    }

    override fun toString(): String {
        return stringBuilder.toString()
    }

    override fun setSpan(p0: Any?, p1: Int, p2: Int, p3: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun insert(p0: Int, p1: CharSequence?, p2: Int, p3: Int): Editable {
        stringBuilder.insert(p0, p1, p2, p3)

        return this
    }

    override fun insert(p0: Int, p1: CharSequence?): Editable {
        stringBuilder.insert(p0, p1)

        return this
    }

    override fun <T : Any?> getSpans(p0: Int, p1: Int, p2: Class<T>?): Array<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clear() {
        stringBuilder.delete(0, stringBuilder.length)
    }

    override fun getFilters(): Array<InputFilter> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeSpan(p0: Any?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun nextSpanTransition(p0: Int, p1: Int, p2: Class<*>?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun append(p0: CharSequence?): Editable {
        stringBuilder.append(p0)
        return this
    }

    override fun append(p0: CharSequence?, p1: Int, p2: Int): Editable {
        stringBuilder.append(p0, p1, p2)
        return this
    }

    override fun append(p0: Char): Editable {
        stringBuilder.append(p0)
        return this
    }

    override fun getSpanEnd(p0: Any?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun replace(p0: Int, p1: Int, p2: CharSequence?, p3: Int, p4: Int): Editable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun replace(p0: Int, p1: Int, p2: CharSequence?): Editable {
        stringBuilder.replace(p0, p1, p2.toString())
        return this
    }

    override fun getChars(p0: Int, p1: Int, p2: CharArray?, p3: Int) {
        stringBuilder.getChars(p0, p1, p2, p3)
    }

    /**
     * Returns the character at the specified [index] in this character sequence.
     */
    override fun get(index: Int): Char {
        return stringBuilder.get(index)
    }

    override fun clearSpans() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSpanStart(p0: Any?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(p0: Int, p1: Int): Editable {
        stringBuilder.delete(p0, p1)
        return this
    }

    override fun setFilters(p0: Array<out InputFilter>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSpanFlags(p0: Any?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Returns the length of this character sequence.
     */
    override val length: Int
        get() = stringBuilder.length

    /**
     * Returns a new character sequence that is a subsequence of this character sequence,
     * starting at the specified [startIndex] and ending right before the specified [endIndex].
     *
     * @param startIndex the start index (inclusive).
     * @param endIndex the end index (exclusive).
     */
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        return stringBuilder.subSequence(startIndex, endIndex)
    }
}