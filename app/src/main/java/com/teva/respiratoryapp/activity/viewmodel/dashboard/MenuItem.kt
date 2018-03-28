//
// MenuItem.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.dashboard

import android.databinding.BaseObservable

import com.teva.respiratoryapp.activity.viewmodel.dashboard.DashboardViewModel.MENU_ITEM_ID

/**
 * This class is the Item ViewModel for the menu item.
 *
 * @property id - the id of the menu item.
 * @property name - the name of the menu item.
 */

class MenuItem(val id: MENU_ITEM_ID, val name: String) : BaseObservable()
