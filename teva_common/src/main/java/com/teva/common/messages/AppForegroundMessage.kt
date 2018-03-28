//
// AppForegroundMessage.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.common.messages

/**
 * Message sent when the application moves into or out of the foreground
 *
 * @property inForeground Indicates whether the app is in the foreground.
 */
class AppForegroundMessage(val inForeground: Boolean)
