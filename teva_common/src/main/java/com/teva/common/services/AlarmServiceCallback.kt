//
// AlarmServiceCallback.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.common.services

import android.os.Parcelable
import android.support.annotation.UiThread

/**
 * Callback used to notify AlarmService clients of alarms.
 */
interface AlarmServiceCallback {

    /**
     * Notifies a client that an alarm has expired.
     *
     * @param id   The alarm id.
     * @param data The alarm data.
     */
    @UiThread
    fun onAlarm(id: String, data: Parcelable?)
}
