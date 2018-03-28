//
// ModelCache.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.generic

import java.util.*

/**
 * Thic class represents the cache for holding the model objects
 */
class ModelCache<T> {
    var cache: List<T>? = null

    /**
     * Returns true if there are objects in the cache matching the filter predicate.
     * If no filter is specified, returns true if the cache is not empty.
     *
     * @param filter The filter predicate.
     * @return True if an item was found, false otherwise.
     */
    operator fun contains(filter: ((T) -> Boolean)?): Boolean {
        var result = false
        if (cache != null) {
            if (filter == null) {
                result = cache!!.isNotEmpty()
            } else {
                for (model in cache!!) {
                    if (filter(model)) {
                        result = true
                        break
                    }
                }
            }
        }

        return result
    }

    /**
     * Returns the first item in the cache that matches the filter predicate.
     *
     * @param filter The filter predicate.
     * @return The first item that matches the filter predicate or null if none found.
     */
    fun first(filter: ((T) -> Boolean)?): T? {
        var result: T? = null
        if (cache != null) {
            if (filter == null) {
                if (cache!!.isNotEmpty()) {
                    result = cache!![0]
                }
            } else {
                for (model in cache!!) {
                    if (filter(model)) {
                        result = model
                        break
                    }
                }
            }
        }

        return result
    }

    /**
     * Returns a list of any cached items that match the filter predicate.
     *
     * @param filter The filter predicate.
     * @return A new list of items that match the filter predicate.
     */
    fun any(filter: ((T) -> Boolean)?): List<T> {
        var result: MutableList<T>? = null

        if (cache != null) {
            if (filter == null) {
                result = ArrayList(cache!!)
            } else {
                result = ArrayList<T>()

                for (model in cache!!) {
                    if (filter(model)) {
                        result.add(model)
                    }
                }
            }
        }

        return result ?: ArrayList()
    }
}
