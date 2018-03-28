//
// ObservableHashSetTests.java
// app
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.mvvmframework.utils

import com.nhaarman.mockito_kotlin.*
import com.teva.respiratoryapp.testutils.BaseTest

import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor

import java.util.ArrayList

import org.junit.Assert.assertEquals

/**
 * This class defines unit tests for the ObservableHashSet class.
 */
class ObservableHashSetTests : BaseTest() {

    private inner class Foo(var id: Int, var name: String, var value: Double)

    @Before
    fun setup() {

    }

    @Test
    fun testAddingAnItemToObservableHashSetTriggersACallback() {
        val foo = Foo(12345, "class", 729.1)

        val observableHashSet = ObservableHashSet<Foo>()
        val onSetChangedCallback: ObservableSet.OnSetChangedCallback<ObservableSet<Foo>, Foo> = mock()

        // set the callback
        observableHashSet.addOnSetChangedCallback(onSetChangedCallback)
        // add a new object
        observableHashSet.add(foo)

        // verify that the callback is onvoked wth the collection and the newly added object
        verify(onSetChangedCallback).onSetChanged(eq(observableHashSet), eq(foo))
    }

    @Test
    fun testAddingACollectionToObservableHashSetTriggersCallbacks() {
        val listToAdd = ArrayList<Foo>()
        listToAdd.add(Foo(12345, "class", 729.1))
        listToAdd.add(Foo(11234, "method", 513.2))
        listToAdd.add(Foo(10123, "file", 411.25))

        val objectArgumentCaptor = argumentCaptor<Foo>()

        val observableHashSet = ObservableHashSet<Foo>()

        // set the callback
        val onSetChangedCallback: ObservableSet.OnSetChangedCallback<ObservableSet<Foo>, Foo> = mock()
        observableHashSet.addOnSetChangedCallback(onSetChangedCallback)

        // add a collection of objects
        observableHashSet.addAll(listToAdd)

        // verify that the callback is invoked for each new object being added
        // and after adding the objects.
        verify(onSetChangedCallback, times(4)).onSetChanged(eq(observableHashSet), objectArgumentCaptor.capture())

        // verify that each callback invocation included the object
        // being added from the collection
        val addedObjects = objectArgumentCaptor.allValues
        assertEquals(listToAdd[0], addedObjects[0])
        assertEquals(listToAdd[1], addedObjects[1])
        assertEquals(listToAdd[2], addedObjects[2])
    }

    @Test
    fun testRemovingAnItemFromObservableHashSetTriggersACallback() {
        val foo = Foo(12345, "class", 729.1)
        val observableHashSet = ObservableHashSet<Foo>()
        val onSetChangedCallback: ObservableSet.OnSetChangedCallback<ObservableSet<Foo>, Foo> = mock()

        // add an item
        observableHashSet.add(foo)

        // set the callback
        observableHashSet.addOnSetChangedCallback(onSetChangedCallback)

        // remove the item
        observableHashSet.remove(foo)

        // verify that the callback was invoked with the item being removed
        verify(onSetChangedCallback).onSetChanged(eq(observableHashSet), eq(foo))
    }

    @Test
    fun testRemovingMultipleElementsFromObservableHashSetTriggersCallback() {
        val listToAdd = ArrayList<Foo>()
        listToAdd.add(Foo(12345, "class", 729.1))
        listToAdd.add(Foo(11234, "method", 513.2))
        listToAdd.add(Foo(10123, "file", 411.25))

        val listToRemove = ArrayList<Foo>()
        listToRemove.add(listToAdd[0])
        listToRemove.add(listToAdd[1])

        val observableHashSet = ObservableHashSet<Foo>()

        // add a collection of objects
        observableHashSet.addAll(listToAdd)

        // set the callback
        val onSetChangedCallback: ObservableSet.OnSetChangedCallback<ObservableSet<Foo>, Foo> = mock()
        observableHashSet.addOnSetChangedCallback(onSetChangedCallback)

        // remove some of the items
        observableHashSet.removeAll(listToRemove)

        // verify that the callback is invoked
        verify(onSetChangedCallback).onSetChanged(eq(observableHashSet), isNull())
    }

    @Test
    fun testClearingObservableHashSetTriggersCallback() {
        val listToAdd = ArrayList<Foo>()
        val obj1 = Foo(12345, "class", 729.1);
        val obj2 = Foo(11234, "method", 513.2)
        val obj3 = Foo(10123, "file", 411.25)
        listToAdd.add(obj1)
        listToAdd.add(obj2)
        listToAdd.add(obj3)

        val observableHashSet = ObservableHashSet<Foo>()

        // add a collection of objects
        observableHashSet.addAll(listToAdd)

        // set the callback
        val onSetChangedCallback: ObservableSet.OnSetChangedCallback<ObservableSet<Foo>, Foo> = mock()
        observableHashSet.addOnSetChangedCallback(onSetChangedCallback)

        // clear all the items
        observableHashSet.clear()

        // verify that the callback is invoked
        verify(onSetChangedCallback).onSetChanged(eq(observableHashSet), eq(obj1))
        verify(onSetChangedCallback).onSetChanged(eq(observableHashSet), eq(obj2))
        verify(onSetChangedCallback).onSetChanged(eq(observableHashSet), eq(obj3))
    }

    @Test
    fun testRetainingSomeElementsAndRemovingOthersFromObservableHashSetTriggersCallback() {
        val listToAdd = ArrayList<Foo>()
        listToAdd.add(Foo(12345, "class", 729.1))
        listToAdd.add(Foo(11234, "method", 513.2))
        listToAdd.add(Foo(10123, "file", 411.25))
        listToAdd.add(Foo(20123, "directory", 522.14))

        val listToRetain = ArrayList<Foo>()
        listToRetain.add(listToAdd[0])
        listToRetain.add(listToAdd[1])

        val observableHashSet = ObservableHashSet<Foo>()

        // add a collection of objects
        observableHashSet.addAll(listToAdd)

        // set the callback
        val onSetChangedCallback: ObservableSet.OnSetChangedCallback<ObservableSet<Foo>, Foo> = mock()
        observableHashSet.addOnSetChangedCallback(onSetChangedCallback)

        // retain some of the items and remove others
        observableHashSet.retainAll(listToRetain)

        // verify that the callback is invoked
        verify(onSetChangedCallback).onSetChanged(eq(observableHashSet), isNull())
    }

    @Test
    fun testCallbackIsNotTriggeredIfUnregisteredFromObservableHashSet() {
        val listToAdd = ArrayList<Foo>()
        listToAdd.add(Foo(12345, "class", 729.1))
        listToAdd.add(Foo(11234, "method", 513.2))
        listToAdd.add(Foo(10123, "file", 411.25))
        listToAdd.add(Foo(20123, "directory", 522.14))

        val observableHashSet = ObservableHashSet<Foo>()

        // add a collection of objects
        observableHashSet.addAll(listToAdd)

        // set the callback
        val onSetChangedCallback: ObservableSet.OnSetChangedCallback<ObservableSet<Foo>, Foo> = mock()
        observableHashSet.addOnSetChangedCallback(onSetChangedCallback)

        // remove an item
        observableHashSet.remove(listToAdd[0])

        // verify that the callback is invoked
        verify(onSetChangedCallback).onSetChanged(eq(observableHashSet), eq(listToAdd[0]))

        // remove another item
        observableHashSet.remove(listToAdd[1])

        // verify that the callback is invoked
        verify(onSetChangedCallback).onSetChanged(eq(observableHashSet), eq(listToAdd[1]))

        // unregister the callback
        observableHashSet.removeOnSetChangedCallback(onSetChangedCallback)

        // remove another item
        observableHashSet.remove(listToAdd[2])

        // verify that the callback is not invoked
        verify(onSetChangedCallback, never()).onSetChanged(eq(observableHashSet), eq(listToAdd[2]))

    }
}
