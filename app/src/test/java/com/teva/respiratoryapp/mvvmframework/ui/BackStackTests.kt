//
// BackStackTests.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.mvvmframework.ui

import com.teva.respiratoryapp.testutils.BaseTest

import org.junit.Before
import org.junit.Test

import java.util.ArrayList

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals

/**
 * This class defines unit tests for the BackStack class.
 */

class BackStackTests : BaseTest() {

    private val fragmentInfoList = ArrayList<FragmentInfo>()

    @Before
    fun setup() {
        val fragment1 = FragmentInfo()
        fragment1.fragmentTag = "fragment1"
        fragment1.stackTag = "stack1fragment1"
        val fragment2 = FragmentInfo()
        fragment2.fragmentTag = "fragment2"
        fragment2.stackTag = "stack1fragment2"
        val fragment3 = FragmentInfo()
        fragment3.fragmentTag = "fragment3"
        fragment3.stackTag = "stack1fragment3"
        val fragment4 = FragmentInfo()
        fragment4.fragmentTag = "fragment4"
        fragment4.stackTag = "stack1fragment4"

        fragmentInfoList.add(fragment1)
        fragmentInfoList.add(fragment2)
        fragmentInfoList.add(fragment3)
        fragmentInfoList.add(fragment4)
    }

    @Test
    fun testGetTopReturnsTheCorrectItem() {
        val backStack = BackStack()
        backStack.addAll(fragmentInfoList)
        val topFragmentInfo = backStack.top
        assertEquals("fragment4", topFragmentInfo!!.fragmentTag)
    }

    @Test
    fun testAddAfterAddsItemAtTheCorrectPosition() {
        val newFragment = FragmentInfo()
        newFragment.fragmentTag = "fragment2.5"
        newFragment.stackTag = "stack1fragment2.5"

        val backStack = BackStack()
        backStack.addAll(fragmentInfoList)

        backStack.addAfter(newFragment) { obj -> obj.fragmentTag == "fragment3" }
        assertEquals(3, backStack.indexOf(newFragment).toLong())
    }

    @Test
    fun testRemoveRemovesTheCorrectItem() {
        val newFragment = FragmentInfo()
        newFragment.fragmentTag = "fragment2.5"
        newFragment.stackTag = "stack1fragment2.5"
        val backStack = BackStack()
        backStack.addAll(fragmentInfoList)
        backStack.addAfter(newFragment) { obj -> obj.fragmentTag == "fragment3" }
        assertEquals(5, backStack.size.toLong())
        backStack.remove { obj -> obj.fragmentTag == "fragment2.5" }
        assertEquals(4, backStack.size.toLong())
        assertEquals(-1, backStack.indexOf(newFragment).toLong())
    }

    @Test
    fun testRemoveInvalidItemDoesNothing() {
        val backStack = BackStack()
        backStack.addAll(fragmentInfoList)
        assertEquals(4, backStack.size.toLong())
        backStack.remove { obj -> obj.fragmentTag == "fragment2.5" }
        assertEquals(4, backStack.size.toLong())
    }

    @Test
    fun testClearToStackTagExclusiveClearsUptoTheSpecifiedStackTag() {
        val backStack = BackStack()
        backStack.addAll(fragmentInfoList)
        backStack.clearToStackTag("stack1fragment2", false)
        assertEquals(2, backStack.size.toLong())
        assertEquals("fragment2", backStack.top!!.fragmentTag)
    }

    @Test
    fun testClearToStackTagInclusiveClearsIncludingTheSpecifiedStackTag() {
        val backStack = BackStack()
        backStack.addAll(fragmentInfoList)
        backStack.clearToStackTag("stack1fragment2", true)
        assertEquals(1, backStack.size.toLong())
        assertEquals("fragment1", backStack.top!!.fragmentTag)
    }

    @Test
    fun testClearToStackTagWithInvalidStackTagClearsEverything() {
        val backStack = BackStack()
        backStack.addAll(fragmentInfoList)
        backStack.clearToStackTag("AnInvalidStackTag", true)
        assertEquals(0, backStack.size.toLong())
    }

    @Test
    fun testCreateFragmentTagCreatesAUniqueFragmentTag() {
        val backStack = BackStack()
        backStack.addAll(fragmentInfoList)
        val fragmentTag1 = backStack.createFragmentTag()
        val fragmentTag2 = backStack.createFragmentTag()

        assertNotEquals(fragmentTag1, fragmentTag2)
    }
}
