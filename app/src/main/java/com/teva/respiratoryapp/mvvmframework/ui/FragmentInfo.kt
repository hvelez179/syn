//
// FragmentInfo.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.mvvmframework.ui

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.app.Fragment
import com.teva.common.utilities.createParcel
import com.teva.notifications.services.notification.NotificationInfo
import kotlin.reflect.KClass

/**
 * This class represents an entry on the fragment back stack;
 *
 * @property fragmentClass The class of the fragment.
 * @property fragmentTag The tag associated with the fragment that is used to locate it in the Android FragmentManager.
 * @property arguments The fragment construction arguments.
 * @property state The saved state of a fragment that was running before it was added to the back stack.
 * @property animation The animation to use when adding the fragment to the activity.
 * @property isOpaque A value indicating whether the fragment background is opaque or transparent.
 * @property stackTag The stack tag for the fragment
 */
class FragmentInfo(var fragmentClass: KClass<*> = Unit::class,
                   var fragmentTag: String = "",
                   var arguments: Bundle? = null,
                   var state: Fragment.SavedState? = null,
                   var animation: FragmentAnimation = FragmentAnimation.NO_ANIMATION,
                   var isOpaque: Boolean = false,
                   var stackTag: String? = null) : Parcelable {

    /**
     * Constructor used to create a FragmentInfo from a Parcel.
     *
     * @param parcel The Parcel to read the objects fields from.
     * @throws ClassNotFoundException
     */
    private constructor(parcel: Parcel) : this() {
        val fragmentClassName = parcel.readString()
        fragmentClass = Class.forName(fragmentClassName).kotlin
        fragmentTag = parcel.readString()
        arguments = parcel.readBundle(FragmentInfo::class.java.classLoader)
        state = parcel.readParcelable<Fragment.SavedState>(FragmentInfo::class.java.classLoader)
        stackTag = parcel.readString()
        animation = FragmentAnimation.fromOrdinal(parcel.readInt())
        isOpaque = parcel.readInt() != 0
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or [.PARCELABLE_WRITE_RETURN_VALUE].
     */
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(fragmentClass.java.name)
        dest.writeString(fragmentTag)
        dest.writeBundle(arguments)
        dest.writeParcelable(state, flags)
        dest.writeString(stackTag)
        dest.writeInt(animation.ordinal)
        dest.writeInt(if (isOpaque) 1 else 0)
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of [.writeToParcel],
     * the return value of this method must include the
     * [.CONTENTS_FILE_DESCRIPTOR] bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     *         by this Parcelable object instance.
     * @see .CONTENTS_FILE_DESCRIPTOR
     */
    override fun describeContents(): Int {
        return 0
    }

    companion object {
        /**
         * This class is used to create instances of the FragmentInfo during parcel deserialization.
         */
        @JvmField @Suppress("unused")
        val CREATOR = createParcel { FragmentInfo(it) }
    }
}
