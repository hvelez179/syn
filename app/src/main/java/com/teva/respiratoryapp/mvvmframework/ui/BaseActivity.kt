//
// BaseActivity.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.mvvmframework.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.support.annotation.UiThread
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.teva.common.messages.AppForegroundMessage
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.*
import com.teva.common.utilities.Messenger
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.controls.LoadingIndicator
import com.teva.respiratoryapp.activity.viewmodel.LoadingEvents
import com.teva.respiratoryapp.mvvmframework.controls.attachInsetsForwarder
import com.teva.respiratoryapp.services.alert.AlertConfiguration
import com.teva.respiratoryapp.services.alert.AlertConfigurationProvider
import com.teva.respiratoryapp.services.alert.SystemAlertManager
import com.teva.respiratoryapp.services.alert.SystemAlertManagerImpl
import java.util.*
import kotlin.reflect.KClass

@SuppressLint("Registered")
@UiThread
abstract class BaseActivity : AppCompatActivity(), AlertConfigurationProvider, LoadingEvents {

    protected val logger: Logger = Logger(this.javaClass.simpleName)

    protected var resumed: Boolean = false

    protected var pendingFragmentInfo: FragmentInfo? = null
    protected var pendingFragmentsToRemove: List<String>? = null
    protected var pendingAnimationOverride: FragmentAnimation? = null

    protected var currentFragment: BaseFragment<*, *>? = null
    protected var currentFragmentInfo: FragmentInfo? = null

    protected var backStack = BackStack()

    protected var isKeyboardVisible = false
        set(value) {
            if (field != value) {
                field = value
                currentFragment?.isKeyboardVisible = value
            }
        }

    /**
     * Returns the dependency injection container for the activity.
     */
    val dependencyProvider = DependencyProvider(DependencyProvider.default)

    protected var permissionChecker: PermissionChecker? = null
    protected var localizationService: LocalizationService? = null
    protected var systemAlertManager: SystemAlertManager? = null

    protected var loadingIndicator: LoadingIndicator? = null

    /**
     * Android lifecycle method called when the activity is created.

     * @param savedInstanceState The saved state of the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        val decorView = window.decorView

        var options = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            options = options or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        decorView.systemUiVisibility = options

        val keyboardSizeThreshold = resources.getDimension(R.dimen.keyboard_inset_threshold)
        decorView.setOnApplyWindowInsetsListener { v, windowInsets ->

            // The keyboard is visible if the bottom system window inset is greater than
            // a predetermined threshold.
            isKeyboardVisible = windowInsets.systemWindowInsetBottom > keyboardSizeThreshold

            v.onApplyWindowInsets(windowInsets)
        }

        permissionChecker = PermissionChecker(this, dependencyProvider)
        dependencyProvider.register(permissionChecker!!)

        systemAlertManager = SystemAlertManagerImpl(this, this)
        dependencyProvider.register(SystemAlertManager::class, systemAlertManager!!)

        localizationService = dependencyProvider.resolve()

        initViewModels(savedInstanceState)

        super.onCreate(savedInstanceState)
    }

    /**
     * Called to set the content view of the activity.
     *
     * @param layoutResID The layout id of the content view
     */
    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)

        findViewById<FrameLayout>(R.id.fragment_container)?.attachInsetsForwarder()

        loadingIndicator = findViewById<LoadingIndicator>(R.id.loadingIndicator)
    }

    /**
     * Called to set the content view of the activity.
     *
     * @param view The content view
     */
    override fun setContentView(view: View?) {
        super.setContentView(view)

        findViewById<FrameLayout>(R.id.fragment_container)?.attachInsetsForwarder()
    }

    /**
     * Called to set the content view of the activity.
     *
     * @param view The content view
     * @param params The layoutParams of the content view
     */
    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)

        findViewById<FrameLayout>(R.id.fragment_container)?.attachInsetsForwarder()
    }

    /**
     * Called when the current Window of the activity gains or loses
     * focus.
     *
     * @param hasFocus Whether the window of this activity has focus.
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            updateScreenFeatures()
        }
    }

    /**
     * Creates and initializes the ViewModels used by the activity and
     * adds them to the activity's DependencyProvider.
     *
     * @param savedInstanceState The saved state of the activity
     */
    open protected fun initViewModels(savedInstanceState: Bundle?) {
        dependencyProvider.register(LoadingEvents::class, this)
    }

    override fun showLoadingIndicator() {
        loadingIndicator?.show()
    }

    override fun hideLoadingIndicator() {
        loadingIndicator?.hide()
    }

    /**
     * Android lifecycle method called when the activity becomes visible.
     */
    override fun onStart() {
        super.onStart()

        activeCount++
        if (activeCount == 1) {
            // App is now in foreground
            sendAppForegroudMessage()
        }
    }

    /**
     * Android lifecycle method called when the activity is hidden.
     */
    override fun onStop() {
        super.onStop()

        activeCount--
        if (activeCount == 0) {
            // App is now in background
            sendAppForegroudMessage()
        }
    }

    /**
     * Android lifecycle method called when the activity becomes the currently focused activity.
     */
    override fun onResume() {
        super.onResume()
        resumed = true

        if (pendingFragmentInfo != null) {
            doShowView(pendingFragmentInfo!!, pendingAnimationOverride, null)
            pendingFragmentInfo = null
        }
    }

    /**
     * Android lifecycle method called when the activity is not the currently focused activity.
     */
    override fun onPause() {
        super.onPause()
        resumed = false
    }

    /**
     * Broadcasts a message indicating whether the app is in the foreground or background.
     */
    protected fun sendAppForegroudMessage() {
        dependencyProvider.resolve<Messenger>().publish(AppForegroundMessage(activeCount != 0))
    }

    protected fun getLocalizedString(stringId: Int,
                                     stringReplacements: Map<String, Any>? = null): String {
        return localizationService?.getString(stringId, stringReplacements) ?: ""
    }

    /**
     * Displays a popup dialog.
     *
     * @param dialogClass The dialog class to display.
     */
    protected fun showDialog(dialogClass: KClass<*>) {
        val fragment = createDialog(dialogClass)
        fragment.show(supportFragmentManager, "dialog")
    }

    /**
     * Displays a new view and pushes the current view onto the back stack.
     *
     * @param fragmentClass The view's Fragment class.
     * @param stackTag      The tag to apply to the view on the back stack.
     */
    protected fun showView(fragmentClass: KClass<*>, stackTag: String) {
        showView(fragmentClass, FragmentAnimation.NO_ANIMATION, stackTag, null)
    }

    /**
     * Displays a new view and pushes the current view onto the back stack.
     *
     * @param fragmentClass The view's Fragment class.
     * @param animation     The animation to use for the fragment transition
     * @param stackTag      The tag to apply to the view on the back stack.
     * @param bundle        The argument Bundle for the view.
     */
    @JvmOverloads protected fun showView(fragmentClass: KClass<*>, animation: FragmentAnimation = FragmentAnimation.NO_ANIMATION, stackTag: String? = null, bundle: Bundle? = null) {
        logger.log(VERBOSE, "showView(): " + fragmentClass.java.name)

        val fragmentInfo = FragmentInfo()
        fragmentInfo.fragmentClass = fragmentClass
        fragmentInfo.arguments = bundle
        fragmentInfo.animation = animation
        fragmentInfo.stackTag = stackTag

        if (resumed) {
            doShowView(fragmentInfo, null, null)
        } else {
            pendingFragmentInfo = fragmentInfo
            pendingFragmentsToRemove = null
            pendingAnimationOverride = null
        }
    }

    /**
     * Displays a new view and pushes the current view onto the back stack.
     *
     * @param fragmentClass The view's Fragment class.
     * @param animation     The animation to use for the fragment transition
     */
    protected fun replaceView(fragmentClass: KClass<*>,
                              animation: FragmentAnimation) {
        replaceView(null, false, fragmentClass, animation, null, null, null)
    }

    /**
     * Pushes a fragment onto the back stack behind the current view.
     * @param fragmentClass The view's Fragment class.
     * @param animation     The animation to use for the fragment transition
     * @param stackTag      The tag to apply to the view on the back stack.
     * @param bundle        The argument Bundle for the view.
     */
    protected fun insertView(fragmentClass: KClass<*>,
                             animation: FragmentAnimation,
                             stackTag: String?,
                             bundle: Bundle?) {
        val fragmentInfo = FragmentInfo()
        fragmentInfo.fragmentClass = fragmentClass
        fragmentInfo.arguments = bundle
        fragmentInfo.animation = animation
        fragmentInfo.stackTag = stackTag
        fragmentInfo.fragmentTag = backStack.createFragmentTag()

        backStack.add(fragmentInfo)
    }

    /**
     * Displays a new view and pushes the current view onto the back stack.
     *
     * @param popToTag      The stack tag indicating when to stop popping views.
     *                      Null to replace only the current view.
     * @param inclusive     Indicates whether the view with the popToTag is included in the popping.
     * @param fragmentClass The view's Fragment class.
     * @param animation     The animation to use for the fragment transition
     * @param stackTag      The tag to apply to the view on the back stack.
     * @param bundle        The argument Bundle for the view.
     */
    protected fun replaceView(popToTag: String?,
                              inclusive: Boolean,
                              fragmentClass: KClass<*>,
                              animation: FragmentAnimation,
                              animationOverride: FragmentAnimation?,
                              stackTag: String?,
                              bundle: Bundle?) {
        val fragmentInfo = FragmentInfo()
        fragmentInfo.fragmentClass = fragmentClass
        fragmentInfo.arguments = bundle
        fragmentInfo.animation = animation
        fragmentInfo.stackTag = stackTag

        val fragmentTagsToRemove = ArrayList<String>()

        if (popToTag != null) {
            // pop views off the stack only if the current view isn't the one with the tag.
            if (currentFragmentInfo!!.stackTag != popToTag) {
                // clear the back stack to the target fragment.
                fragmentTagsToRemove.add(currentFragmentInfo!!.fragmentTag)
                fragmentTagsToRemove.addAll(backStack.clearToStackTag(popToTag, inclusive))
            } else if (inclusive) {
                fragmentTagsToRemove.add(currentFragmentInfo!!.fragmentTag)
            }
        } else {
            fragmentTagsToRemove.add(currentFragmentInfo!!.fragmentTag)
        }

        if (resumed) {
            doShowView(fragmentInfo, animationOverride, fragmentTagsToRemove)
        } else {
            pendingFragmentInfo = fragmentInfo
            pendingFragmentsToRemove = fragmentTagsToRemove
            pendingAnimationOverride = animationOverride
        }
    }

    /**
     * Displays a new view using the information in the FragmentInfo and pushes the current view
     * onto the back stack.

     * @param fragmentInfo The information describing the new view.
     */
    protected fun doShowView(fragmentInfo: FragmentInfo,
                             animationOverride: FragmentAnimation?,
                             fragmentTagsToRemove: List<String>?) {
        val removeTagSet = HashSet<String>()

        if (fragmentTagsToRemove != null) {
            removeTagSet.addAll(fragmentTagsToRemove)
        }

        // create the fragment and initialize it with its arguments.
        val fragment = createFragment(fragmentInfo.fragmentClass)
        if (fragmentInfo.arguments != null) {
            fragment.arguments = fragmentInfo.arguments
        }

        val fragmentManager = supportFragmentManager

        // create and initialize the fragment transaction.
        val animation = animationOverride ?: fragmentInfo.animation
        val transaction = fragmentManager.beginTransaction()
        when (animation) {
            FragmentAnimation.FADE -> transaction.setCustomAnimations(R.anim.fade_in, R.anim.hold)

            FragmentAnimation.SLIDE_OVER -> transaction.setCustomAnimations(R.anim.slide_in_over,
                    R.anim.slide_in_under)
            else -> { /* do nothing */
            }
        }

        // update the back stack with the current fragment.
        // An illegal state exception is thrown if fragment manager does not have the current fragment.
        // This should normally not happen but if it does, handle the exception.
        try {
            if (currentFragment != null && !removeTagSet.contains(currentFragmentInfo!!.fragmentTag)) {
                currentFragmentInfo!!.state = fragmentManager.saveFragmentInstanceState(currentFragment)
                backStack.add(currentFragmentInfo!!)
            }
        } catch(exception: IllegalStateException) {
            logger.log(ERROR, exception.message ?: "Failed to save current fragment.")
        }

        // retrieve references for all visible fragments from the FragmentManager.
        // these will be removed when a new opaque fragment is displayed.
        for (stackIndex in backStack.indices.reversed()) {
            val entry = backStack[stackIndex]

            val oldFragment = fragmentManager.findFragmentByTag(entry.fragmentTag)
            if (oldFragment != null) {
                entry.state = fragmentManager.saveFragmentInstanceState(oldFragment)
                removeTagSet.add(entry.fragmentTag)
            }

            if (entry.isOpaque) {
                // stop after the first opaque fragment is encountered
                break
            }
        }

        val oldFragments = removeTagSet.mapNotNull { fragmentManager.findFragmentByTag(it) }

        // Set the new fragment as the current fragment.
        currentFragment = fragment
        currentFragmentInfo = fragmentInfo
        currentFragmentInfo!!.fragmentTag = backStack.createFragmentTag()
        currentFragmentInfo!!.isOpaque = fragment.isOpaque

        // Remove the currently visible fragments from the fragment container.
        if (oldFragments.isNotEmpty()) {
            // only remove the old fragments if this fragment is opaque
            if (fragment.isOpaque) {
                if (animation == FragmentAnimation.NO_ANIMATION) {
                    // If there's not animation, then remove the old fragment immediately.
                    for (oldFragment in oldFragments) {
                        transaction.remove(oldFragment)
                    }
                } else {
                    // If the new fragment is opaque, then we need to hide all the
                    // fragments that are currently being delayed.

                    // If there's an animation, then just hide the old fragment and
                    // remove it later.  This ensures that the new fragment will
                    // be on the top when animating the transition.
                    for (oldFragment in oldFragments) {
                        transaction.hide(oldFragment)
                    }

                    val fragment = currentFragment
                    fragment?.transitionComplete = {
                        for (oldFragment in oldFragments) {
                            removeHiddenFragment(oldFragment)
                        }

                        fragment?.transitionComplete = null
                    }
                }
            }
        }
        transaction.add(R.id.fragment_container, fragment, currentFragmentInfo!!.fragmentTag)

        // Update the activity orientation and immersive mode
        updateScreenFeatures()

        // execute the fragment transaction
        transaction.commitNow()
    }

    /**
     * Updates the orientation and immersive mode features for a fragment.
     */
    @Suppress("DEPRECATION")
    fun updateScreenFeatures() {
        val decorView = window.decorView

        if (currentFragment != null) {

            requestedOrientation = currentFragment!!.orientation

            // show or hide the navigation and status bar
            val uiOptions = if (currentFragment!!.isImmersive) {
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
            } else {
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }

            decorView.systemUiVisibility = uiOptions
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
    }

    /**
     * Removes a fragment that was hidden during the fragment transaction.

     * @param fragment The fragment to remove.
     */
    private fun removeHiddenFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.remove(fragment)
        transaction.commit()
    }

    /**
     * This method performs the Back operation after it has been offered to the
     * current fragment.
     *
     * @param noAnimation Disables the fragment animation when a view is popped
     */
    protected fun handleBackPressed(noAnimation: Boolean = false) {
        if (backStack.size == 0) {
            logger.log(INFO, "No backstack, closing activity.")
            super.onBackPressed()
        } else {
            popView(noAnimation)
        }
    }

    /**
     * Called by Android when the phone's back button is pressed.
     */
    override fun onBackPressed() {
        if (currentFragment != null) {
            currentFragment!!.onBackPressed()
        } else {
            handleBackPressed()
        }
    }

    /**
     * Creates a fragment instance from it's class object.

     * @param fragmentClass The fragment's class
     * *
     * @return A new instance of the fragment
     */
    protected fun createFragment(fragmentClass: KClass<*>): BaseFragment<*, *> {
        val fragment: BaseFragment<*, *>

        // create fragment from class
        try {
            val ctor = fragmentClass.java.getConstructor()
            fragment = ctor.newInstance() as BaseFragment<*, *>
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalStateException(e)
        }

        return fragment
    }

    /**
     * Creates a fragment from its class type using reflection and initializes
     * it with data from a FragmentInfo object.

     * @param fragmentInfo The FragmentInfo describing the fragment.
     * *
     * @return A new instance of the fragment.
     */
    private fun createFragment(fragmentInfo: FragmentInfo): BaseFragment<*, *> {

        var fragment: BaseFragment<*, *>? = null

        // create fragment from class
        try {
            val ctor = fragmentInfo.fragmentClass.java.getConstructor()
            fragment = ctor.newInstance() as BaseFragment<*, *>
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (fragment == null) {
            fragment = createFragment(fragmentInfo.fragmentClass)
        }

        if (fragmentInfo.arguments != null) {
            fragment.arguments = fragmentInfo.arguments
        }

        if (fragmentInfo.state != null) {
            fragment.setInitialSavedState(fragmentInfo.state)
        }

        return fragment
    }

    /**
     * Dismisses the current view and displays the fragment from the top
     * of the back stack.
     */
    protected fun popView(noAnimation: Boolean = false) {
        popViews(null, false, noAnimation)
    }

    /**
     * Dismisses the current view and all the views back to the specifed stackTag
     * and displays the fragment from the top of the back stack.

     * @param stackTag The stack tag to pop the stack back to.
     * *
     * @param inclusive Indicates whether the fragment with the stack tag is also removed.
     */
    protected fun popViews(stackTag: String?, inclusive: Boolean, noAnimation: Boolean = false) {
        logger.log(ERROR, "popViews")
        val fragmentManager = supportFragmentManager

        if (backStack.size > 0) {
            val tagsToRemove = ArrayList<String>()
            if (stackTag != null) {
                if (currentFragmentInfo!!.stackTag == stackTag) {
                    // the target fragment is already the current fragment
                    // pop it off if inclusive
                    if (!inclusive) {
                        // Caller wanted to pop views until a stack tag was reached, but
                        // to leave the fragment with that tag.
                        // The current fragment has that tag, so we should leave it
                        // and do nothing.
                        return
                    }
                } else {
                    // clear the back stack to the target fragment.
                    tagsToRemove.addAll(backStack.clearToStackTag(stackTag, inclusive))
                }
            }

            tagsToRemove.add(currentFragmentInfo!!.fragmentTag)

            val animation = if (noAnimation) FragmentAnimation.NO_ANIMATION else currentFragmentInfo!!.animation

            val transaction = fragmentManager.beginTransaction()

            // pop the next view off the back stack.
            if (backStack.size > 0) {
                val fragmentInfo = backStack.removeAt(backStack.size - 1)

                when (animation) {
                    FragmentAnimation.FADE -> transaction.setCustomAnimations(R.anim.hold, R.anim.fade_out)

                    FragmentAnimation.SLIDE_OVER -> transaction.setCustomAnimations(R.anim.slide_out_under,
                            R.anim.slide_out_over)
                    else -> { /* do nothing */ }
                }

                // Check to see if the fragment is already loaded in the FragmentManager.
                // This would occur if fragment being popped is not opaque.
                var fragment: BaseFragment<*, *>? = fragmentManager.findFragmentByTag(fragmentInfo.fragmentTag) as BaseFragment<*, *>?

                // If fragment didn't already exist, then create it and any fragments behind it
                // if it's transparent.  If it does exist already, then any fragments behind it
                // will have already been created if the fragment is transparent.
                if (fragment == null) {
                    // if this fragment is not opaque, then we need to also create any
                    // fragments that would be displayed behind it.
                    if (!fragmentInfo.isOpaque) {
                        val restoreIndex = backStack.indices.lastOrNull { backStack[it].isOpaque } ?: 0

                        for (stackIndex in restoreIndex until backStack.size) {
                            val entry = backStack[stackIndex]

                            val additionalFragment = fragmentManager.findFragmentByTag(entry.fragmentTag) as BaseFragment<*, *>?

                            if (additionalFragment == null) {
                                val fragmentToAdd = createFragment(entry)
                                transaction.add(R.id.fragment_container,
                                        fragmentToAdd, entry.fragmentTag)
                            }
                        }
                    }

                    fragment = createFragment(fragmentInfo)
                    transaction.add(R.id.fragment_container, fragment, fragmentInfo.fragmentTag)
                }

                currentFragment = fragment
                currentFragmentInfo = fragmentInfo
            } else {
                currentFragment = null
                currentFragmentInfo = null
            }

            // remove the fragments
            tagsToRemove
                    .mapNotNull { fragmentManager.findFragmentByTag(it) }
                    .forEach { transaction.remove(it) }

            transaction.commitNow()
        } else if (currentFragment != null) {
            // no items on the back stack, so just remove the current view.
            val transaction = fragmentManager.beginTransaction()

            transaction.remove(currentFragment)

            currentFragment = null
            currentFragmentInfo = null

            transaction.commitNow()
        }

        // Update the activity orientation and immersive mode
        updateScreenFeatures()
    }

    /**
     * Creates a dialog fragment instance from it's class object.

     * @param fragmentClass The fragment's class
     * *
     * @return A new instance of the dialog fragment
     */
    protected fun createDialog(fragmentClass: KClass<*>): DialogFragment {
        val fragment: DialogFragment

        // create fragment from class
        try {
            val ctor = fragmentClass.java.getConstructor()
            fragment = ctor.newInstance() as DialogFragment
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }

        return fragment
    }

    /**
     * Called to save the state of the activity into a Bundle.

     * @param outState The Bundle to save the activity's state into.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelableArrayList(BACK_STACK_BUNDLE_KEY, backStack)
        outState.putParcelable(CURRENT_FRAGMENT_INFO_KEY, currentFragmentInfo)
    }

    /**
     * Called to restore the state of an activity from a Bundle.

     * @param savedInstanceState The Bundle containing the activity's state.
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val list = savedInstanceState.getParcelableArrayList<FragmentInfo>(BACK_STACK_BUNDLE_KEY)
        backStack = BackStack()
        if (list != null) {
            backStack.addAll(list)
        }

        currentFragmentInfo = savedInstanceState.getParcelable<FragmentInfo>(CURRENT_FRAGMENT_INFO_KEY)

        val fragmentManager = supportFragmentManager
        currentFragment = fragmentManager.findFragmentById(R.id.fragment_container) as BaseFragment<*, *>

        updateScreenFeatures()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        permissionChecker?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * Gets the layout id for an alert.
     *
     * @param id The id of the dialog
     * @return The layout id to use or null to use the default layout.
     */
    override open fun getAlertConfiguration(id: String?): AlertConfiguration? {
        return (currentFragment as? AlertConfigurationProvider)?.getAlertConfiguration(id)
    }

    companion object {
        private val BACK_STACK_BUNDLE_KEY = "backstack"
        private val CURRENT_FRAGMENT_INFO_KEY = "currentFragmentInfo"
        val COLOR_ANIMATION_DURATION = 250

        private var activeCount: Int = 0
    }
}
