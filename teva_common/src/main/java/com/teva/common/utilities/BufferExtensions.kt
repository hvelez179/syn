/*
 *
 *  BufferExtensions.kt
 *  teva_common
 *
 *  Copyright © 2018 Teva. All rights reserved.
 *
 */

package com.teva.common.utilities

import java.nio.ByteBuffer

val UNSIGNED_SHORT_BIT_MASK = 0xffff
val UNSIGNED_BYTE_BIT_MASK = 0xff

val ByteBuffer.ushort : Int
    get() = short.toInt() and UNSIGNED_SHORT_BIT_MASK

val ByteBuffer.ubyte : Int
    get() = get().toInt() and UNSIGNED_BYTE_BIT_MASK
