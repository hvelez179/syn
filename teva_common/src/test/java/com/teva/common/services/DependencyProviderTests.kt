///
// DependencyProviderTests.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.common.services

import com.teva.utilities.services.DependencyProvider
import junit.framework.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.security.InvalidParameterException

/**
 * This class defines unit tests for the DependencyProvider class.
 */
class DependencyProviderTests {

    // Mock interface for testing dependency resolution
    private interface IFoo {
        val id: Int
    }

    // Mock class for testing dependency resolution
    private inner class Foo(override val id: Int) : IFoo

    // Mock class for testing dependency resolution
    private inner class Bar(val id: Int)

    @Before
    fun setup() {
        DependencyProvider.default.unregisterAll()
    }


    @Test
    fun testRegisteredSingletonsAreResolvedCorrectly() {
        val expectedFoo = Foo(1)
        val expectedBar = Bar(2)

        val dependencyProvider = DependencyProvider.default
        // register object specifying the class type.
        dependencyProvider.register(Foo::class, expectedFoo)
        // register only the object.
        dependencyProvider.register(expectedBar)

        val foo = dependencyProvider.resolve<Foo>()
        val bar = dependencyProvider.resolve<Bar>()

        // verify that the correct objects are returned by the dependency provider.
        assertEquals(foo, expectedFoo)
        assertEquals(bar, expectedBar)
    }

    @Test
    fun testRegisteredSingletonsWithInterfaceAreResolvedCorrectly() {
        val expectedFoo = Foo(1)
        val expectedFooWithInterface = Foo(2)

        val dependencyProvider = DependencyProvider.default

        // register two instances, one with the class type and
        // other with the interface type.
        dependencyProvider.register(expectedFoo)
        dependencyProvider.register(IFoo::class, expectedFooWithInterface)

        val foo = dependencyProvider.resolve<Foo>()
        val fooWithInterface = dependencyProvider.resolve<IFoo>()

        // verify that the correct objects are returned by the dependency provider.
        assertEquals(foo, expectedFoo)
        assertEquals(fooWithInterface, expectedFooWithInterface)
    }

    @Test
    fun testObjectsAreCorrectlyCreatedWhenObjectFactoriesAreRegistered() {
        val dependencyProvider = DependencyProvider.default

        // register a factory
        dependencyProvider.register(Foo::class, object : DependencyProvider.IFactory {
            internal var index = 1

            override fun create(instanceName: String?): Any {
                return Foo(index++)
            }
        })

        val foo1 = dependencyProvider.resolve(Foo::class.java)
        val foo2 = dependencyProvider.resolve(Foo::class.java)
        val foo3 = dependencyProvider.resolve(Foo::class.java)

        // verify that the objects were created correctly.
        assertEquals(foo1.id.toLong(), 1)
        assertEquals(foo2.id.toLong(), 2)
        assertEquals(foo3.id.toLong(), 3)
    }

    @Test
    fun testObjectsRegisteredWithMultiLevelContainersAreResolvedCorrectly() {
        val parentContainer = DependencyProvider.default
        val dependencyProvider = DependencyProvider(parentContainer)

        val expectedFoo = Foo(1)
        val expectedBar = Bar(2)

        // register classes with the parent and child.
        parentContainer.register(expectedFoo)
        dependencyProvider.register(expectedBar)

        val foo = dependencyProvider.resolve(Foo::class.java)
        val bar = dependencyProvider.resolve(Bar::class.java)

        // verify that the child is able to resolve dependencies
        // registered with the parent.
        assertEquals(foo, expectedFoo)
        assertEquals(bar, expectedBar)
    }

    @Test
    fun testThatChildInstanceIsReturnedWhenOverridden() {
        val parentContainer = DependencyProvider.default
        val dependencyProvider = DependencyProvider(parentContainer)

        val parentFoo = Foo(1)
        val expectedFoo = Foo(2)

        // override the registration in the child.
        parentContainer.register(parentFoo)
        dependencyProvider.register(expectedFoo)

        val foo = dependencyProvider.resolve(Foo::class.java)

        // verify that the child instance is returned.
        assertEquals(foo, expectedFoo)
    }

    @Test
    fun testSingletonsRegisteredByNamesAreResolvedCorrectly() {
        val expectedFoo1 = Foo(1)
        val expectedFoo2 = Foo(2)

        val dependencyProvider = DependencyProvider.default
        dependencyProvider.unregisterAll()
        // register same type of objects by different names.
        dependencyProvider.register(Foo::class, expectedFoo1, "Foo1")
        dependencyProvider.register(expectedFoo2, "Foo2")

        val foo2 = dependencyProvider.resolve<Foo>("Foo2")
        val foo1 = dependencyProvider.resolve<Foo>("Foo1")

        // verify that the correct objects are returned by the dependency provider.
        assertEquals(foo1, expectedFoo1)
        assertEquals(foo2, expectedFoo2)
    }

    @Test
    fun testDependenciesRegisteredWithParentAreClearedWhenParentIsRemoved() {
        val parentContainer = DependencyProvider.default
        val dependencyProvider = DependencyProvider()

        val expectedFoo = Foo(1)
        val expectedBar = Bar(2)

        parentContainer.register(expectedFoo)
        dependencyProvider.register(expectedBar)
        dependencyProvider.addParent(parentContainer)

        val foo = dependencyProvider.resolve(Foo::class.java)
        var bar = dependencyProvider.resolve(Bar::class.java)

        assertEquals(foo, expectedFoo)
        assertEquals(bar, expectedBar)

        // remove the parent
        dependencyProvider.removeParent(parentContainer)

        bar = dependencyProvider.resolve(Bar::class.java)

        // verify that child registration is still available
        assertEquals(bar, expectedBar)

        // verify that the class registered with the parent is no longer available.
        assertEquals(bar, expectedBar)

        // verify that parent registration is not available and throws exception
        try {
            dependencyProvider.resolve(Foo::class.java)

            // should throw exception before it gets here
            assertTrue(false)
        } catch (ex: Exception) {
            assertEquals(InvalidParameterException::class.java, ex.javaClass)
        }

    }

    @Test(expected = InvalidParameterException::class)
    fun testIllegalStateExceptionThrownIfDependencyCouldNotBeResolvedAtAnyLevel() {
        val parentContainer = DependencyProvider.default
        val dependencyProvider = DependencyProvider()

        val expectedFoo = Foo(1)
        val expectedBar = Bar(2)

        class Sample

        parentContainer.register(expectedFoo)
        dependencyProvider.register(expectedBar)
        dependencyProvider.addParent(parentContainer)

        // request for a class not registered with the parent or the child.
        val sample = dependencyProvider.resolve(Sample::class.java)

        // verify that the dependency cannot be resolved and null is returned.
        assertNull(sample)
    }

    @Test
    fun testParentRegistrationsPersistIfAllChildRegistrationsAreUnregistered() {
        val parentContainer = DependencyProvider.default
        val dependencyProvider = DependencyProvider()

        val expectedFoo = Foo(1)
        val expectedBar = Bar(2)

        parentContainer.register(expectedFoo)
        dependencyProvider.register(expectedBar)
        dependencyProvider.addParent(parentContainer)

        var foo = dependencyProvider.resolve(Foo::class.java)
        val bar = dependencyProvider.resolve(Bar::class.java)

        assertEquals(foo, expectedFoo)
        assertEquals(bar, expectedBar)

        // unregister all classes from the child.
        dependencyProvider.unregisterAll()


        // verify that the parent registration is still available.
        foo = dependencyProvider.resolve(Foo::class.java)
        assertEquals(foo, expectedFoo)

        // verify that child registration is not available and throws exception
        try {
            dependencyProvider.resolve(Bar::class.java)

            // should throw exception before it gets here
            assertTrue(false)
        } catch (ex: Exception) {
            assertEquals(InvalidParameterException::class.java, ex.javaClass)
        }

    }
}
