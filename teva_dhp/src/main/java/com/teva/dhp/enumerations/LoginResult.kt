/*
 *
 *  LoginResult.kt
 *  teva_dhp
 *
 *  Copyright © 2018 Teva. All rights reserved.
 *
 */

package com.teva.dhp.enumerations

/**
 * This enumeration lists possible results of a login attempt.
 */
enum class LoginResult {
    SUCCESS,
    FAILURE,
    INCORRECT_ACCOUNT
}