//
// ItemListTests.java
// app
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.mvvmframework.ui

import android.databinding.ObservableList.OnListChangedCallback
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify

import com.teva.respiratoryapp.testutils.BaseTest

import org.junit.Before
import org.junit.Test

import java.util.ArrayList

import org.junit.Assert.assertEquals
import org.mockito.ArgumentMatchers

/**
 * This class defines unit tests for the ItemList class.
 */

class ItemListTests : BaseTest() {

    private inner class TestModel {
        internal var id: Int = 0
    }

    private lateinit var listChangedCallback: OnListChangedCallback<ItemList<TestModel, Int>>

    @Before
    fun setup() {
        listChangedCallback = mock()
    }

    @Test
    fun testAddItemAddsItemAtCorrectLocationAndTriggersCallback() {
        // create an empty item list.
        val itemList = object : ItemList<TestModel, Int>() {
            override fun getItemId(item: TestModel): Int {
                return item.id
            }

            override fun compareItems(item1: TestModel, item2: TestModel): Boolean {
                return item1.id == item2.id
            }
        }

        // add listener for list changed callback
        itemList.addOnListChangedCallback(listChangedCallback)

        // add an item.
        val model1 = TestModel()
        model1.id = 1
        itemList.add(model1)

        // verify that the item is added at index 0.
        assertEquals(model1.id.toLong(), itemList[0].id.toLong())

        // add a new item at index 0.
        val model2 = TestModel()
        model2.id = 2
        itemList.add(0, model2)

        // verify that the new item is added at index 0
        // and old item is moved to index 1.
        assertEquals(model2.id.toLong(), itemList[0].id.toLong())
        assertEquals(model1.id.toLong(), itemList[1].id.toLong())

        // verify that the callback was invoked twice.
        verify(listChangedCallback, times(2)).onItemRangeInserted(eq(itemList), eq(0), eq(1))
    }

    @Test
    fun testRemoveItemRemovesItemFromCorrectLocationAndTriggersCallback() {
        // create an empty item list.
        val itemList = object : ItemList<TestModel, Int>() {
            override fun getItemId(item: TestModel): Int {
                return item.id
            }

            override fun compareItems(item1: TestModel, item2: TestModel): Boolean {
                return item1.id == item2.id
            }
        }

        // add listener for list changed callback
        itemList.addOnListChangedCallback(listChangedCallback)

        // add a list of items.
        val model1 = TestModel()
        model1.id = 1

        val model2 = TestModel()
        model2.id = 2

        val model3 = TestModel()
        model3.id = 3

        val model4 = TestModel()
        model4.id = 4

        itemList.add(model1)
        itemList.add(model2)
        itemList.add(model3)
        itemList.add(model4)

        //remove an item from index 0.
        itemList.removeAt(0)

        // remove an item by specifying the item.
        itemList.remove(model3)

        // verify that the correct items have been removed.
        assertEquals(model2.id.toLong(), itemList[0].id.toLong())
        assertEquals(model4.id.toLong(), itemList[1].id.toLong())

        // verify that the callback was invoked twice.
        verify(listChangedCallback).onItemRangeRemoved(eq(itemList), eq(0), eq(1))
        verify(listChangedCallback).onItemRangeRemoved(eq(itemList), eq(1), eq(1))
    }

    @Test
    fun testCallbackIsNotTriggeredFromItemListIfListenerIsRemoved() {
        // create an empty item list.
        val itemList = object : ItemList<TestModel, Int>() {
            override fun getItemId(item: TestModel): Int {
                return item.id
            }

            override fun compareItems(item1: TestModel, item2: TestModel): Boolean {
                return item1.id == item2.id
            }
        }

        // add listener for list changed callback
        itemList.addOnListChangedCallback(listChangedCallback)

        // add an item.
        val model1 = TestModel()
        model1.id = 1
        itemList.add(model1)

        // verify that the callback was invoked.
        verify(listChangedCallback, times(1)).onItemRangeInserted(eq(itemList), eq(0), eq(1))

        // remove the listener.
        itemList.removeOnListChangedCallback(listChangedCallback)

        // add a new item at index 0.
        val model2 = TestModel()
        model2.id = 2
        itemList.add(0, model2)

        // verify that the callback was not invoked the second time.
        verify(listChangedCallback, times(1)).onItemRangeInserted(eq(itemList), eq(0), eq(1))
    }

    @Test
    fun testReplaceReplacesItemsInTheItemListAndTriggersCallback() {
        // create an empty item list.
        val itemList = object : ItemList<TestModel, Int>() {
            override fun getItemId(item: TestModel): Int {
                return item.id
            }

            override fun compareItems(item1: TestModel, item2: TestModel): Boolean {
                return item1.id == item2.id
            }
        }

        // add listener for list changed callback.
        itemList.addOnListChangedCallback(listChangedCallback)

        // add a list of items.
        val model1 = TestModel()
        model1.id = 1

        val model2 = TestModel()
        model2.id = 2

        itemList.add(model1)
        itemList.add(model2)

        // replace the existing items with a new list of items.
        val model3 = TestModel()
        model3.id = 3

        val model4 = TestModel()
        model4.id = 4

        val newList = ArrayList<TestModel>()
        newList.add(model3)
        newList.add(model4)

        itemList.replace(newList)

        // verify that the items have been replaced and
        // only the new items exist in the list.
        assertEquals(2, itemList.size.toLong())
        assertEquals(model3.id.toLong(), itemList[0].id.toLong())
        assertEquals(model4.id.toLong(), itemList[1].id.toLong())

        // verify that the list changed callback was invoked.
        verify(listChangedCallback).onChanged(eq(itemList))
    }

    @Test
    fun testMoveMovesItemsInTheItemListAndTriggersCallback() {
        // create an empty item list.
        val itemList = object : ItemList<TestModel, Int>() {
            override fun getItemId(item: TestModel): Int {
                return item.id
            }

            override fun compareItems(item1: TestModel, item2: TestModel): Boolean {
                return item1.id == item2.id
            }
        }

        // add a list of items.
        val model1 = TestModel()
        model1.id = 1

        val model2 = TestModel()
        model2.id = 2

        val model3 = TestModel()
        model3.id = 3

        val model4 = TestModel()
        model4.id = 4

        itemList.add(model1)
        itemList.add(model2)
        itemList.add(model3)
        itemList.add(model4)

        // add listener for list changed callback.
        itemList.addOnListChangedCallback(listChangedCallback)

        // move the item at index 0 to index 2.
        itemList.move(0, 2)

        // verify that the item has been moved.
        assertEquals(4, itemList.size.toLong())
        assertEquals(model2.id.toLong(), itemList[0].id.toLong())
        assertEquals(model3.id.toLong(), itemList[1].id.toLong())
        assertEquals(model1.id.toLong(), itemList[2].id.toLong())
        assertEquals(model4.id.toLong(), itemList[3].id.toLong())

        // verify that the list changed callback was invoked for the moved item.
        verify(listChangedCallback).onItemRangeMoved(eq(itemList), eq(0), eq(2), eq(1))
    }

    @Test
    fun testSetItemReplacesItemAtTheSpecifiedLocationAndTriggersCallback() {
        // create an empty item list.
        val itemList = object : ItemList<TestModel, Int>() {
            override fun getItemId(item: TestModel): Int {
                return item.id
            }

            override fun compareItems(item1: TestModel, item2: TestModel): Boolean {
                return item1.id == item2.id
            }
        }

        // add a list of items.
        val model1 = TestModel()
        model1.id = 1

        val model2 = TestModel()
        model2.id = 2

        val model3 = TestModel()
        model3.id = 3

        itemList.add(model1)
        itemList.add(model2)
        itemList.add(model3)

        // add listener for list changed callback.
        itemList.addOnListChangedCallback(listChangedCallback)

        // set a new item at index 0.
        val model4 = TestModel()
        model4.id = 4
        itemList[0] = model4

        // verify that the item has been moved.
        assertEquals(3, itemList.size.toLong())
        assertEquals(model4.id.toLong(), itemList[0].id.toLong())
        assertEquals(model2.id.toLong(), itemList[1].id.toLong())
        assertEquals(model3.id.toLong(), itemList[2].id.toLong())

        // verify that the list changed callback was invoked for the replaced item.
        verify(listChangedCallback).onItemRangeChanged(eq(itemList), eq(0), eq(1))
    }

    @Test
    fun testMergeListWithoutMatchingItemsMergesItemsInTheItemListAndTriggersCallback() {
        // create an empty item list.
        val itemList = object : ItemList<TestModel, Int>() {
            override fun getItemId(item: TestModel): Int {
                return item.id
            }

            override fun compareItems(item1: TestModel, item2: TestModel): Boolean {
                return item1.id == item2.id
            }
        }

        // add a list of items.
        val model1 = TestModel()
        model1.id = 1

        val model4 = TestModel()
        model4.id = 4

        val model6 = TestModel()
        model6.id = 6

        val model7 = TestModel()
        model7.id = 7

        val model8 = TestModel()
        model8.id = 8

        val model10 = TestModel()
        model10.id = 10

        val model11 = TestModel()
        model11.id = 11

        itemList.add(model1)
        itemList.add(model4)
        itemList.add(model6)
        itemList.add(model7)
        itemList.add(model8)
        itemList.add(model10)
        itemList.add(model11)

        // add listener for list changed callback.
        itemList.addOnListChangedCallback(listChangedCallback)

        // create a new list of items for merging
        val model2 = TestModel()
        model2.id = 2

        val model3 = TestModel()
        model3.id = 3

        val model5 = TestModel()
        model5.id = 5

        val model9 = TestModel()
        model9.id = 9

        val model12 = TestModel()
        model12.id = 12

        val newModels = ArrayList<TestModel>()
        newModels.add(model2)
        newModels.add(model3)
        newModels.add(model5)
        newModels.add(model9)
        newModels.add(model12)

        // merge the list.
        itemList.merge(newModels)

        // verify that the item has been moved.
        assertEquals(5, itemList.size.toLong())
        assertEquals(model2.id.toLong(), itemList[0].id.toLong())
        assertEquals(model3.id.toLong(), itemList[1].id.toLong())
        assertEquals(model5.id.toLong(), itemList[2].id.toLong())
        assertEquals(model9.id.toLong(), itemList[3].id.toLong())
        assertEquals(model12.id.toLong(), itemList[4].id.toLong())

        // verify that the list changed callback was invoked.
        verify(listChangedCallback).onChanged(ArgumentMatchers.eq(itemList))
    }

    @Test
    fun testMergeListWithMatchingItemsMergesItemsInTheItemListAndTriggersCallback() {
        // create an empty item list.
        val itemList = object : ItemList<TestModel, Int>() {
            override fun getItemId(item: TestModel): Int {
                return item.id
            }

            override fun compareItems(item1: TestModel, item2: TestModel): Boolean {
                return item1.id == item2.id
            }
        }

        // add a list of items.
        val model1 = TestModel()
        model1.id = 1

        val model4 = TestModel()
        model4.id = 4

        val model6 = TestModel()
        model6.id = 6

        val model7 = TestModel()
        model7.id = 7

        val model8 = TestModel()
        model8.id = 8

        val model10 = TestModel()
        model10.id = 10

        val model11 = TestModel()
        model11.id = 11

        itemList.add(model1)
        itemList.add(model4)
        itemList.add(model6)
        itemList.add(model7)
        itemList.add(model8)
        itemList.add(model10)
        itemList.add(model11)

        // add listener for list changed callback.
        itemList.addOnListChangedCallback(listChangedCallback)

        // create a new list of items for merging
        val model2 = TestModel()
        model2.id = 2

        val model3 = TestModel()
        model3.id = 3

        val model5 = TestModel()
        model5.id = 5

        val model7a = TestModel()
        model7a.id = 7

        val model9 = TestModel()
        model9.id = 9

        val model10a = TestModel()
        model10a.id = 10

        val model12 = TestModel()
        model12.id = 12

        val newModels = ArrayList<TestModel>()
        newModels.add(model2)
        newModels.add(model3)
        newModels.add(model5)
        newModels.add(model10a)
        newModels.add(model9)
        newModels.add(model7a)
        newModels.add(model12)

        // merge the new list.
        itemList.merge(newModels)

        // verify that the item has been moved.
        assertEquals(7, itemList.size.toLong())
        assertEquals(model2.id.toLong(), itemList[0].id.toLong())
        assertEquals(model3.id.toLong(), itemList[1].id.toLong())
        assertEquals(model5.id.toLong(), itemList[2].id.toLong())
        assertEquals(model10a.id.toLong(), itemList[3].id.toLong())
        assertEquals(model9.id.toLong(), itemList[4].id.toLong())
        assertEquals(model7a.id.toLong(), itemList[5].id.toLong())
        assertEquals(model12.id.toLong(), itemList[6].id.toLong())

        // verify that the list changed callback was invoked for the replaced item.
        verify(listChangedCallback).onChanged(ArgumentMatchers.eq(itemList))
    }

    @Test
    fun testMergeListWithLessItemsToTriggerIndividualInsertCallback() {
        // create an empty item list.
        val itemList = object : ItemList<TestModel, Int>() {
            override fun getItemId(item: TestModel): Int {
                return item.id
            }

            override fun compareItems(item1: TestModel, item2: TestModel): Boolean {
                return item1.id == item2.id
            }
        }

        // add a list of items.
        val model1 = TestModel()
        model1.id = 1

        val model2 = TestModel()
        model2.id = 2

        itemList.add(model1)
        itemList.add(model2)

        // add listener for list changed callback.
        itemList.addOnListChangedCallback(listChangedCallback)

        // create a new list of items for merging
        val model4 = TestModel()
        model4.id = 4

        val model2a = TestModel()
        model2a.id = 2

        val model1a = TestModel()
        model1a.id = 1

        val newModels = ArrayList<TestModel>()
        newModels.add(model4)
        newModels.add(model2a)
        newModels.add(model1a)

        // merge the new list.
        itemList.merge(newModels)

        // verify that the item has been moved.
        assertEquals(3, itemList.size.toLong())
        assertEquals(model4.id.toLong(), itemList[0].id.toLong())
        assertEquals(model2a.id.toLong(), itemList[1].id.toLong())
        assertEquals(model1a.id.toLong(), itemList[2].id.toLong())

        // verify that the list changed callback was invoked for the replaced item.
        verify(listChangedCallback).onItemRangeInserted(ArgumentMatchers.eq(itemList), ArgumentMatchers.eq(0), ArgumentMatchers.eq(1))
        verify(listChangedCallback).onItemRangeChanged(ArgumentMatchers.eq(itemList), ArgumentMatchers.eq(0), ArgumentMatchers.eq(1))
        verify(listChangedCallback).onItemRangeChanged(ArgumentMatchers.eq(itemList), ArgumentMatchers.eq(1), ArgumentMatchers.eq(1))
        verify(listChangedCallback).onItemRangeMoved(ArgumentMatchers.eq(itemList), ArgumentMatchers.eq(2), ArgumentMatchers.eq(1), ArgumentMatchers.eq(1))
    }

    @Test
    fun testMergeListWithLessItemsToTriggerIndividualRemoveCallback() {
        // create an empty item list.
        val itemList = object : ItemList<TestModel, Int>() {
            override fun getItemId(item: TestModel): Int {
                return item.id
            }

            override fun compareItems(item1: TestModel, item2: TestModel): Boolean {
                return item1.id == item2.id
            }
        }

        // add a list of items.
        val model1 = TestModel()
        model1.id = 1

        val model2 = TestModel()
        model2.id = 2

        val model3 = TestModel()
        model3.id = 3

        itemList.add(model1)
        itemList.add(model2)
        itemList.add(model3)

        // add listener for list changed callback.
        itemList.addOnListChangedCallback(listChangedCallback)

        // create a new list of items for merging
        val model2a = TestModel()
        model2a.id = 2

        val model1a = TestModel()
        model1a.id = 1

        val newModels = ArrayList<TestModel>()
        newModels.add(model2a)
        newModels.add(model1a)

        // merge the new list.
        itemList.merge(newModels)

        // verify that the item has been moved.
        assertEquals(2, itemList.size.toLong())
        assertEquals(model2a.id.toLong(), itemList[0].id.toLong())
        assertEquals(model1a.id.toLong(), itemList[1].id.toLong())

        // verify that the list changed callback was invoked for the replaced item.
        verify(listChangedCallback).onItemRangeRemoved(ArgumentMatchers.eq(itemList), ArgumentMatchers.eq(2), ArgumentMatchers.eq(1))
        verify(listChangedCallback).onItemRangeChanged(ArgumentMatchers.eq(itemList), ArgumentMatchers.eq(0), ArgumentMatchers.eq(1))
        verify(listChangedCallback).onItemRangeChanged(ArgumentMatchers.eq(itemList), ArgumentMatchers.eq(1), ArgumentMatchers.eq(1))
        verify(listChangedCallback).onItemRangeMoved(ArgumentMatchers.eq(itemList), ArgumentMatchers.eq(1), ArgumentMatchers.eq(0), ArgumentMatchers.eq(1))
    }
}
