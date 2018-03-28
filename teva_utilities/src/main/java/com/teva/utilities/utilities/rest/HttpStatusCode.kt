//
// HttpStatusCode.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.utilities.utilities.rest

/**
 * This enum contains the HTTP status codes.
 */

enum class HttpStatusCode(val value: Int) {
    OK(200),
    CREATED(201),
    NO_CONTENT(204),
    NOT_MODIFIED(304),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500)
}
