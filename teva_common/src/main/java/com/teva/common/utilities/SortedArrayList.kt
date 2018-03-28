//
// SortedArrayList.java
// teva_common
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.common.utilities

import java.util.*

/**
 * This class implements an array list supporting sorting of elements on insert.
 * The elements being inserted should implement the Comparable interface
 * else a ClassCastException is thrown.
 */
class SortedArrayList<T> : Iterable<T>
    where T : Comparable<T> {

    private val arrayList = ArrayList<T>()
    private var isSortedAscending = true

    /**
     * This method inserts the specified value into the ArrayList in a sorted order.
     *
     * @param value     -  the value to be inserted into the ArrayList.
     * @param sortAscending - indicates if the sorting order should be ascending or descending.
     */
    fun insertSorted(value: T, sortAscending: Boolean) {

        val size = arrayList.size

        // if there are elements already inserted,
        // do not allow changing the sort order
        if (size > 0 && isSortedAscending != sortAscending) {
            throw UnsupportedOperationException()
        } else if (size == 0) {
            isSortedAscending = sortAscending
        }

        // add the new element at the end
        arrayList.add(value)

        // iterate from the end of the array list and compare and swap elements till they are in the correct order
        // or we reach the beginning of the array list.
        var i = arrayList.size - 1
        while (i > 0 && if (sortAscending) value < arrayList[i - 1] else value > arrayList[i - 1]) {
            Collections.swap(arrayList, i, i - 1)
            i--
        }
    }

    /**
     * Returns the element at the specified index.
     *
     * @param index - the index at which the element needs to be retrieved.
     * @return - the element at the specified index.
     */
    operator fun get(index: Int): T {
        return arrayList[index]
    }

    /**
     * Returns the size of the list.
     *
     * @return - the size of the list.
     */
    fun size(): Int {
        return arrayList.size
    }

    /**
     * Returns an iterator for iterating over the list.
     *
     * @return - an iterator for iterating over the list.
     */
    override fun iterator(): Iterator<T> {
        return arrayList.iterator()
    }

    /**
     * Removes the specified element from the list.
     *
     * @param value - the value to be removed from the list.
     */
    fun remove(value: T) {
        arrayList.remove(value)
    }

    /**
     * Returns a List.
     *
     * @return - a List.
     */
    fun toList(): List<T> {
        return arrayList
    }
}
