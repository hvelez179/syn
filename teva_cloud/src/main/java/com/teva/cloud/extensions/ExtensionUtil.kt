//
// ExtensionUtil.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.extensions

fun stringOrUnknown(str: String?): String {

    if (str.isNullOrEmpty()) {
        return "Unknown"
    }

    return str!!
}
