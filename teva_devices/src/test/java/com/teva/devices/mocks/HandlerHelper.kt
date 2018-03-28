//
// HandlerHelper.java
// teva_devices
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.devices.mocks

import android.os.Handler

/**
 * Helper class that hides the display of an error in the editor that is due to
 * the Intellisense checking against the Android version of the class rather than
 * the shadow version.
 */
object HandlerHelper {
    fun loopHandler(count: Int = 1) {
        // This shows as an error in the editor, but the unit tests are overriding
        // the android.os.Handler class and added a loop() method, but the editor doesn't
        // realize that. It will compile and run.
        for(index in 1..count) {
            Handler.loop()
        }
    }

    fun clearQueue() {
        Handler.clearQueue()
    }
}
