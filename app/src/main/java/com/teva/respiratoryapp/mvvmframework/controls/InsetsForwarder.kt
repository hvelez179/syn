package com.teva.respiratoryapp.mvvmframework.controls

import android.support.v4.view.OnApplyWindowInsetsListener
import android.support.v4.view.ViewCompat
import android.support.v4.view.WindowInsetsCompat
import android.view.View
import android.view.ViewGroup

/**
 * Forwards the insets of a ViewGroup to it's newly added children
 */
class InsetsForwarder : ViewGroup.OnHierarchyChangeListener, OnApplyWindowInsetsListener {

    private var viewGroup: ViewGroup? = null
    private var insets: WindowInsetsCompat? = null

    fun attach(viewGroup: ViewGroup) {
        this.viewGroup = viewGroup
        ViewCompat.setOnApplyWindowInsetsListener(viewGroup, this)
        viewGroup.setOnHierarchyChangeListener(this)
    }

    /**
     * Called when the window insets change.
     *
     * @param v The view applying window insets
     * @param insets The insets to apply
     * @return The insets supplied, minus any insets that were consumed
     */
    override fun onApplyWindowInsets(v: View?, insets: WindowInsetsCompat): WindowInsetsCompat {
        this.insets = insets

        viewGroup?.let { viewGroup ->
            for (index in 0 until viewGroup.childCount) {
                ViewCompat.dispatchApplyWindowInsets(viewGroup.getChildAt(index), insets)
            }
        }

        return insets
    }

    /**
     * Called when a child is removed.
     *
     * @param parent The parent ViewGroup
     * @param child The child view being removed
     */
    override fun onChildViewRemoved(parent: View, child: View) {
        // do nothing
    }

    /**
     * Called when a child is added. Dispatches the current insets to the new child.
     *
     * @param parent The parent ViewGroup
     * @param child The child view being added
     */
    override fun onChildViewAdded(parent: View, child: View) {
        insets?.let { insets -> ViewCompat.dispatchApplyWindowInsets(child, insets) }
    }
}

/**
 * Attaches an InsetsForwarder to a ViewGroup.
 */
fun ViewGroup.attachInsetsForwarder() {
    val insetsForwarder = InsetsForwarder()
    insetsForwarder.attach(this)
}