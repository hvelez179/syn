//
// SystemClock.java
// teva_common
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package android.os

/**
 * This class is a mock implementation of the android SystemClock class.
 */
object SystemClock {
    @JvmStatic
    fun elapsedRealtime(): Long {
        return 0
    }
}
