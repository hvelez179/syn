//
// SortedArrayListTests.java
// teva_common
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.common.utilities

import org.junit.Test

import org.junit.Assert.assertEquals

/**
 * This class defines unit tests for the SortedArrayList class.
 */

class SortedArrayListTests {

    private inner class SortableElement(internal var value: Int) : Comparable<SortableElement> {

        override fun compareTo(other: SortableElement): Int {
            return this.value - other.value
        }
    }

    private inner class UnsortableElement(internal var value: Int)

    @Test
    fun testInsertElementsInSortedArrayListInAscendingOrderInsertsElementsCorrectly() {
        // add elements in random order specifying sort order ascending
        val sortedList = SortedArrayList<SortableElement>()
        val element1 = SortableElement(10)
        sortedList.insertSorted(element1, true)
        val element2 = SortableElement(3)
        sortedList.insertSorted(element2, true)
        val element3 = SortableElement(18)
        sortedList.insertSorted(element3, true)
        val element4 = SortableElement(7)
        sortedList.insertSorted(element4, true)
        val element5 = SortableElement(15)
        sortedList.insertSorted(element5, true)

        // verify that the elements are in the ascending order.
        assertEquals(5, sortedList.size().toLong())
        assertEquals(sortedList[0], element2)
        assertEquals(sortedList[1], element4)
        assertEquals(sortedList[2], element1)
        assertEquals(sortedList[3], element5)
        assertEquals(sortedList[4], element3)
    }

    @Test
    fun testInsertElementsInSortedArrayListInDescendingOrderInsertsElementsCorrectly() {
        // add elements in random order specifying sort order descending
        val sortedList = SortedArrayList<SortableElement>()
        val element1 = SortableElement(10)
        sortedList.insertSorted(element1, false)
        val element2 = SortableElement(3)
        sortedList.insertSorted(element2, false)
        val element3 = SortableElement(18)
        sortedList.insertSorted(element3, false)
        val element4 = SortableElement(7)
        sortedList.insertSorted(element4, false)
        val element5 = SortableElement(15)
        sortedList.insertSorted(element5, false)

        // verify that the elements are in the descending order.
        assertEquals(5, sortedList.size().toLong())
        assertEquals(sortedList[0], element3)
        assertEquals(sortedList[1], element5)
        assertEquals(sortedList[2], element1)
        assertEquals(sortedList[3], element4)
        assertEquals(sortedList[4], element2)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun testChangingSortOrderWhileInsertingElementsInSortedArrayListThrowsException() {
        // add elements in random order specifying initial sort order as descending
        val sortedList = SortedArrayList<SortableElement>()
        val element1 = SortableElement(10)
        sortedList.insertSorted(element1, false)
        val element2 = SortableElement(3)
        sortedList.insertSorted(element2, false)

        // change sort order in the middle of insertion.
        // verify that an UnsupportedOperation exception is thrown.
        val element3 = SortableElement(18)
        sortedList.insertSorted(element3, true)
        val element4 = SortableElement(7)
        sortedList.insertSorted(element4, true)
        val element5 = SortableElement(15)
        sortedList.insertSorted(element5, true)
    }

    @Test
    fun testSortedArrayListAllowsIterationUsingForEachLoop() {
        // add elements in random order specifying sort order ascending
        val sortedList = SortedArrayList<SortableElement>()
        val element1 = SortableElement(10)
        sortedList.insertSorted(element1, true)
        val element2 = SortableElement(3)
        sortedList.insertSorted(element2, true)
        val element3 = SortableElement(18)
        sortedList.insertSorted(element3, true)
        val element4 = SortableElement(7)
        sortedList.insertSorted(element4, true)
        val element5 = SortableElement(15)
        sortedList.insertSorted(element5, true)

        // verify that iteration using a foreach loop works correctly.

        for ((loop, element) in sortedList.withIndex()) {
            assertEquals(element, sortedList[loop])
        }
    }

    @Test
    fun testSortedArrayListToListMethodReturnsAValidList() {
        // add elements in random order specifying sort order ascending
        val sortedList = SortedArrayList<SortableElement>()
        val element1 = SortableElement(10)
        sortedList.insertSorted(element1, true)
        val element2 = SortableElement(3)
        sortedList.insertSorted(element2, true)
        val element3 = SortableElement(18)
        sortedList.insertSorted(element3, true)
        val element4 = SortableElement(7)
        sortedList.insertSorted(element4, true)
        val element5 = SortableElement(15)
        sortedList.insertSorted(element5, true)

        val list = sortedList.toList()

        // verify that iteration using a foreach loop works correctly.
        assertEquals(5, list.size.toLong())
        assertEquals(list[0], element2)
        assertEquals(list[1], element4)
        assertEquals(list[2], element1)
        assertEquals(list[3], element5)
        assertEquals(list[4], element3)
    }

    @Test
    fun testRemovingElementFromSortedArrayListLeavesTheElementsSorted() {
        // add elements in random order specifying sort order descending
        val sortedList = SortedArrayList<SortableElement>()
        val element1 = SortableElement(10)
        sortedList.insertSorted(element1, false)
        val element2 = SortableElement(3)
        sortedList.insertSorted(element2, false)
        val element3 = SortableElement(18)
        sortedList.insertSorted(element3, false)
        val element4 = SortableElement(7)
        sortedList.insertSorted(element4, false)
        val element5 = SortableElement(15)
        sortedList.insertSorted(element5, false)

        sortedList.remove(element4)

        // verify that the elements are in the descending order.
        assertEquals(4, sortedList.size().toLong())
        assertEquals(sortedList[0], element3)
        assertEquals(sortedList[1], element5)
        assertEquals(sortedList[2], element1)
        assertEquals(sortedList[3], element2)
    }
}
