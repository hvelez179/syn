package com.teva.respiratoryapp.activity.viewmodel.setup

import android.databinding.BaseObservable
import com.teva.cloud.dataentities.UserProfile

/**
 * ViewModel for the items of the Profile Setup screen
 *
 * @property name The name of the user
 * @property dob The date of birth of the user
 */
class ProfileItemViewModel(
        val id: String? = null,
        val name: String? = null,
        val dob: String? = null,
        val userProfile: UserProfile? = null)
    : BaseObservable() {

    var itemType = ItemType.PROFILE
        private set

    constructor(type: ItemType) : this() {
        itemType = type
    }

    enum class ItemType {
        PROFILE,
        ADD_ANOTHER_DEPENDENT
    }
}