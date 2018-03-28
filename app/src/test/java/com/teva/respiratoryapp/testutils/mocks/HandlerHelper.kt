//
// HandlerHelper.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.testutils.mocks

import android.os.Handler

/**
 * Helper class that hides the display of an error in the editor that is due to
 * the Intellisense checking against the Android version of the class rather than
 * the shadow version.
 */
object HandlerHelper {
    fun loopHandler() {
        // This shows as an error in the editor, but the unit tests are overriding
        // the android.os.Handler class and added a loop() method, but the editor doesn't
        // realize that. It will compile and run.
        Handler.loop()
    }
}
