//
// InhalerNameItem.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.device

import android.databinding.BaseObservable
import android.databinding.Bindable

import com.teva.respiratoryapp.BR
import com.teva.devices.enumerations.InhalerNameType

/**
 * Item viewmodel class for inhaler name choices.
 *
 * @param type The name type of the inhaler
 * @param name The nickname of the inhaler
 */
class InhalerNameItem(type: InhalerNameType, name: String) : BaseObservable() {

    /**
     * The nickname of the inhaler item.
     */
    @get:Bindable
    var name: String = name
        set(value) {
            field = value
            notifyPropertyChanged(BR.name)
        }

    /**
     * The name type of the inhaler item.
     */
    @get:Bindable
    var type: InhalerNameType = type
        set(value) {
            field = value
            notifyPropertyChanged(BR.type)
        }

}
