//
// BackStack.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.mvvmframework.ui

import java.util.*

/**
 * Manages the fragment back stack for the BaseActivity class.
 */
class BackStack : ArrayList<FragmentInfo>() {

    /**
     * Retrieves the entry at the top of the back stack or null if the stack is empty.
     */
    val top: FragmentInfo?
        get() = if (size > 0) get(size - 1) else null

    /**
     * Removes all entries from the backstack that match the predicate.
     *
     * @param predicate The predicate to use to find the items to remove.
     * @return The list of fragment tags of the fragments that were removed.
     */
    fun remove(predicate: (FragmentInfo)->Boolean): List<String> {
        val tags = ArrayList<String>()

        // Iterate from top to bottom and remove any entries that
        // match the predicate.
        for (i in size - 1 downTo 0) {
            val fragmentInfo = get(i)

            if (predicate(fragmentInfo)) {
                removeAt(i)
                tags.add(fragmentInfo.fragmentTag)
            }
        }

        return tags
    }

    /**
     * Adds a new FragmentInfo after an entry that matches a predicate.
     * The stack is searched from top to bottom.
     *
     * @param newEntry  The new FragmentInfo.
     * @param predicate The predicate to use to find the insert position.
     */
    fun addAfter(newEntry: FragmentInfo, predicate: (FragmentInfo)->Boolean) {
        // Iterate from top to bottom and add the entry after the first entry
        // encountered that matches the predicate.
        for (i in size - 1 downTo 0) {
            if (predicate(get(i))) {
                add(i + 1, newEntry)
                break
            }
        }
    }

    /**
     * Pops entries off the stack until the specified stack tag is reached.
     *
     * @param stackTag  The stack tag to search for.
     * @param inclusive True to include the entry with the stack tag, false otherwise.
     * @return The list of fragment tags of the fragments that were removed from the backstack.
     */
    fun clearToStackTag(stackTag: String, inclusive: Boolean): List<String> {
        val tags = ArrayList<String>()

        for (index in size - 1 downTo 0) {
            val fragmentInfo = get(index)
            if (stackTag != fragmentInfo.stackTag) {
                removeAt(index)
                tags.add(fragmentInfo.fragmentTag)
            } else {
                if (inclusive) {
                    removeAt(index)
                    tags.add(fragmentInfo.fragmentTag)
                }
                break
            }
        }

        return tags
    }

    /**
     * Returns a unique fragment tag.
     */
    fun createFragmentTag(): String {
        return UUID.randomUUID().toString()
    }
}
