//
// Intent.java
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package android.content

import android.content.Context
import android.os.Parcelable

import java.util.HashMap

/**
 * This class is a mock implementation of the android Intent class.
 */
open class Intent(var context: Context, var cls: Class<*>) {
    var actionId: String = ""
    var flags: Int = 0
    var extras: MutableMap<String, Any> = HashMap()

    fun setAction(actionId: String): Intent {
        this.actionId = actionId
        return this
    }

    fun getAction(): String {
        return actionId
    }

    fun putExtra(name: String, value: Parcelable): Intent {
        extras.put(name, value)
        return this
    }

    fun putExtra(name: String, value: Boolean): Intent {
        extras.put(name, value)
        return this
    }

    fun getBooleanExtra(name: String, defaultValue: Boolean): Boolean {
        if (extras.containsKey(name)) {
            return extras[name] as Boolean
        }

        return defaultValue
    }

    fun getIntExtra(name: String, defaultValue: Int): Int {
        if (extras.containsKey(name)) {
            return extras[name] as Int
        }

        return defaultValue
    }

    fun <T : Parcelable> getParcelableExtra(name: String): T? {
        return extras[name] as T?
    }

    fun addFlags(flags: Int): Intent {
        this.flags = this.flags or flags
        return this
    }

    fun setClass(context: Context, cls: Class<*>): Intent {
        this.context = context
        this.cls = cls
        return this
    }

    companion object {
        val FLAG_ACTIVITY_NEW_TASK = 0x10000000
    }

}
