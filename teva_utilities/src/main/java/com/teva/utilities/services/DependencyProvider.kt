/*
 *
 *  DependencyProvider.kt
 *  teva_utilities
 *
 *  Copyright © 2018 Teva. All rights reserved.
 *
 */

package com.teva.utilities.services

import com.teva.utilities.utilities.Logger
import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import com.teva.utilities.utilities.Logger.Level.WARN
import java.security.InvalidParameterException
import kotlin.reflect.KClass

/**
 * IOC object that provides the ability to instantiate and locate
 * loosely coupled service and model components.
 */
class DependencyProvider(vararg containers: DependencyProvider) {

    private val recordMap = HashMap<Class<*>, MutableList<Record>>()
    private val parentContainers: MutableList<DependencyProvider>?

    init {
        this.parentContainers = ArrayList<DependencyProvider>()

        Collections.addAll(this.parentContainers, *containers)
    }

    /**
     * Adds a parent container.
     *
     * @param container The new container to add to the parentContainer list
     */
    @Synchronized
    fun addParent(container: DependencyProvider?) {
        if (container != null) {
            parentContainers!!.add(container)
        }
    }

    /**
     * Removes a parent container.
     *
     * @param container - The container to remove from the parentContainer list
     */
    @Synchronized
    fun removeParent(container: DependencyProvider?) {
        if (container != null) {
            parentContainers!!.remove(container)
        }
    }

    /**
     * Registers a singleton object reference by it's class.
     *
     * @param obj The singleton object
     */
    fun register(obj: Any) {
        val record = Record(obj.javaClass, obj, null)
        addRecord(record)
    }

    /**
     * Registers a named object.
     *
     * @param obj  The named object.
     * @param name The name of the object.
     */
    fun register(obj: Any, name: String) {
        val record = Record(obj.javaClass, obj, name)
        addRecord(record)
    }

    /**
     * Registers a factory for an object
     *
     * @param clas    The class of the generated object
     * @param factory The factory for the object
     */
    @Synchronized
    fun register(clas: KClass<*>, factory: IFactory) {
        val record = Record(clas.java, factory)
        addRecord(record)
    }

    /**
     * Registers a factory for an object
     *
     * @param clas    The class of the generated object
     * @param lambda The factory for the object
     */
    @Synchronized
    fun register(clas: KClass<*>, lambda: ((String?)->Any)?) {
        val record = Record(clas.java, lambda)
        addRecord(record)
    }

    /**
     * Registers a named object referenced by a base class or interface.
     *
     * @param clas The class or interface to register the singleton under
     * @param obj  The object
     * @param name The name of the instance
     */
    fun register(clas: KClass<*>, obj: Any, name: String? = null) {
        val record = Record(clas.java, obj, name)
        addRecord(record)
    }

    /**
     * Adds a record to the record map.
     *
     * @param record The record to add.
     */
    private fun addRecord(record: Record) {
        var recordList: MutableList<Record>? = recordMap[record.clas]
        if (recordList == null) {
            recordList = ArrayList<Record>()
            recordMap.put(record.clas, recordList)
        }

        if (record.factory != null || record.lambda != null) {
            // factories take priority over all registered instances
            recordList.clear()
            recordList.add(record)
        } else {
            var found = false
            for (i in recordList.indices) {
                val current = recordList[i]
                if (current.name == null && record.name == null || current.name != null && current.name == record.name) {
                    found = true
                    recordList[i] = record
                }
            }

            if (!found) {
                recordList.add(record)
            }
        }
    }

    /**
     * Finds a dependency record with the given class type and instance name.
     *
     * @param clas         The type of the dependency.
     * @param instanceName The instance name of the dependency.
     * @return The dependency record or null if not found.
     */
    private fun findRecord(clas: Class<*>, instanceName: String?): Record? {
        var record: Record? = null

        val recordList = recordMap[clas]
        if (recordList != null) {
            for (i in recordList.indices) {
                val current = recordList[i]
                if (current.factory != null
                        || current.lambda != null
                        || current.name == null && instanceName == null
                        || current.name != null && current.name == instanceName) {
                    record = current
                    break
                }
            }
        }

        return record
    }

    /**
     * Finds a dependency record for the specified class and instance name
     * and retrieves an object from it.
     *
     * @param clas         The class type of the dependency object
     * @param instanceName The instance name of the dependency object.
     * @return An instance of the dependency object of a dependency record can be found.
     */
    @Synchronized
    fun getObject(clas: Class<*>, instanceName: String?): Any? {
        var obj: Any? = null

        val record = findRecord(clas, instanceName)
        if (record != null) {
            if (record.obj != null) {
                obj = record.obj
            } else if (record.factory != null) {
                obj = record.factory.create(instanceName)
            } else if (record.lambda != null) {
                obj = record.lambda.invoke(instanceName)
            }
        }

        if (obj == null && parentContainers != null) {
            for (container in parentContainers) {
                obj = container.getObject(clas, instanceName)
                if (obj != null) {
                    break
                }
            }
        }

        return obj
    }

    /**
     * Locates a singleton or creates an object.
     *
     * @param clas The class type used to find the object
     * @param <T>  Same as clas
     * @return The singled or factory created object registered under the specified class.
     */
    @Synchronized
    fun <T> resolve(clas: Class<T>): T {
        val result: T? = getObject(clas, null) as T?

        if (result == null) {
            logger.log(WARN, "Object not found: " + clas.name)
        }

        return result ?: throw InvalidParameterException("Object not found: $clas")
    }

    /**
     * Locates a singleton or creates an object.
     *
     * @param <T>  The class type used to find the object
     * @return The singled or factory created object registered under the specified class.
     */
    inline fun <reified T> resolve(): T {
        val result = getObject(T::class.java, null) as T?

        if (result == null) {
            logger.log(WARN, "Object not found: " + T::class.java.name)
        }

        return result ?: throw InvalidParameterException("Object not found: " + T::class.java)
    }

    /**
     * Locates a named instance of an object.
     * Currently only supported for objects registered with a factory.
     *
     * @param <T> The class type used to find the object
     * @param name The instance name.
     * @return The singled or factory created object registered under the specified class.
     */
    inline fun <reified T> resolve(name: String?): T {
        val result = getObject(T::class.java, name) as T?

        if (result == null) {
            logger.log(WARN, "Object not found: " + T::class.java.name)
        }

        return result ?: throw InvalidParameterException("Object not found: " + T::class.java)
    }

    /**
     * Locates a named instance of an object.
     * Currently only supported for objects registered with a factory.
     *
     * @param clas The class type used to find the object
     * @param name The instance name.
     * @param <T>  Same as clas
     * @return The singled or factory created object registered under the specified class.
     */
    @Synchronized
    fun <T> resolve(clas: Class<T>, name: String?): T {
        val result: T? = getObject(clas, name) as T?

        return result ?: throw InvalidParameterException("Object not found: $clas")
    }

    /**
     * Removes all dependency object registrations.
     */
    @Synchronized
    fun unregisterAll() {
        recordMap.clear()
    }

    /**
     * The class used to store the object registrations
     */
    private class Record {
        internal val clas: Class<*>
        internal val name: String?
        internal val obj: Any?
        internal val factory: IFactory?
        internal val lambda: ((String?) -> Any)?

        /**
         * Constructor for registration of a singleton object.
         *
         * @param clas The class to register the singleton under.
         * @param obj  The singleton object
         */
        internal constructor(clas: Class<*>, obj: Any, name: String?) {
            this.clas = clas
            this.obj = obj
            this.factory = null
            this.lambda = null
            this.name = name
        }

        /**
         * Constructor for registration of a factory object.
         *
         * @param clas    The class to register the factory under.
         * @param factory The object factory
         */
        internal constructor(clas: Class<*>, factory: IFactory) {
            this.clas = clas
            this.factory = factory
            this.obj = null
            this.name = null
            this.lambda = null
        }

        /**
         * Constructor for registration of a factory lambda.
         *
         * @param clas    The class to register the factory under.
         * @param lambda The object factory lambda
         */
        internal constructor(clas: Class<*>, lambda: ((String?) -> Any)?) {
            this.clas = clas
            this.lambda = lambda
            this.factory = null
            this.obj = null
            this.name = null
        }
    }

    /**
     * Interface used to define object factories
     */
    interface IFactory {
        /**
         * Creates an instance of an object

         * @return The new object
         */
        fun create(instanceName: String?): Any
    }

    companion object {
        val logger = Logger("DependencyProvider")

        /**
         * Gets the default DependencyProvider.
         */
        val default: DependencyProvider by lazy { DependencyProvider() }
    }
}
