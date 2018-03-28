//
// SplashActivity.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AppCompatActivity
import com.teva.respiratoryapp.R

/**
 * Activity class for the splash screen
 */
class SplashActivity : AppCompatActivity() {
    private val handler = Handler()

    /**
     * Android lifecycle method called when the activity is created.
     * @param savedInstanceState The saved state of the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    /**
     * Android lifecycle method called when the activity is resumed.
     */
    override fun onResume() {
        super.onResume()

        handler.postDelayed({
            val intent = Intent(this, DashboardActivity::class.java)
            intent.putExtra(FROM_SPLASH_KEY, true);
            ActivityCompat.startActivity(this, intent, null)
            finish()
        }, SPLASH_TRANSITION_DELAY)
    }

    companion object {
        val FROM_SPLASH_KEY = "FromSplash"
        val SPLASH_TRANSITION_DELAY = 250L
    }
}
