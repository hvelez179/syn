//
// Intent.java
// teva_common
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package android.content

import android.os.Parcelable

import com.teva.common.services.AlarmService

import java.util.HashMap

/**
 * This class is a mock implementation of the android Intent class.
 */
open class Intent {
    var context: Context? = null
    var cls: Class<*>? = null
    var actionId: String? = null
    var flags: Int = 0
    var extras: MutableMap<String?, Any?> = HashMap()

    constructor() {

    }

    constructor(context: Context, cls: Class<*>) {
        this.context = context
        this.cls = cls
    }

    fun setAction(actionId: String?): Intent {
        this.actionId = actionId
        return this
    }

    open fun getAction(): String? {
        return actionId
    }

    fun putExtra(name: String?, value: Parcelable): Intent {
        extras.put(name, value)
        return this
    }

    fun putExtra(name: String?, value: Boolean): Intent {
        extras.put(name, value)
        return this
    }

    fun getBooleanExtra(name: String?, defaultValue: Boolean): Boolean {
        if (extras.containsKey(name)) {
            return extras[name] as Boolean
        }

        return defaultValue
    }

    fun getIntExtra(name: String?, defaultValue: Int): Int {
        if (extras.containsKey(name)) {
            return extras[name] as Int
        }

        return defaultValue
    }

    fun getByteArrayExtra(name: String?) : ByteArray? {
        return extras[name] as ByteArray?
    }

    fun <T : Parcelable?> getParcelableExtra(name: String?): T? {
        val result = extras.get(name)
        return result as T?
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

}
