//
// BaseTest.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.testutils

import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.testutils.mocks.AsyncTaskHelper
import org.junit.After

/**
 * Base class for unit tests that cleans up the DependencyProvider
 */
open class BaseTest {
    @After
    @Throws(Exception::class)
    fun tearDown() {
        DependencyProvider.default.unregisterAll()
        AsyncTaskHelper.endTaskQueue()
    }
}
