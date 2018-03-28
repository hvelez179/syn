//
//
// ViewExtensions.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//
//

package com.teva.respiratoryapp.mvvmframework.utils

import android.view.View
import android.view.ViewGroup

fun ViewGroup.findViewsWithTag(tag: String, pruneAtMatch: Boolean = false): List<View> {
    val views = ArrayList<View>()

    for (i in 0 until childCount) {
        val child = getChildAt(i)

        val hasTag = child.tag == tag;
        if (hasTag) {
            views.add(child)
        }

        if ((child is ViewGroup) && (!pruneAtMatch || !hasTag)) {
            views.addAll(child.findViewsWithTag(tag, pruneAtMatch))
        }
    }

    return views
}