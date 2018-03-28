//
// BaseFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.mvvmframework.ui

import android.animation.Animator
import android.animation.AnimatorInflater
import android.content.Context
import android.content.pm.ActivityInfo
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ScrollingView
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.teva.common.services.analytics.AnalyticsService
import com.teva.common.services.analytics.enumerations.AnalyticsScreen
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.DEBUG
import com.teva.utilities.utilities.Logger.Level.VERBOSE
import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.services.alert.SystemAlertManager
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

@Suppress("unused")
abstract class BaseFragment<Binding : ViewDataBinding, ViewModel : FragmentViewModel>(protected var viewLayoutId: Int) : Fragment() {
    protected val logger: Logger = Logger(this.javaClass.simpleName)

    private var appbar: AppBarLayout? = null
    protected var toolbar: Toolbar? = null
    protected var toolbarShadowThreshold = 0

    protected var dependencyProvider: DependencyProvider? = null
    protected var localizationService: LocalizationService? = null

    private var fragmentResult: Bundle? = null
    private var lightStatusBar: Boolean = true
    private var saveViewModelState: Boolean = false
    private var titleVisible = true
    private var titleThreshold = 0f
    private var showTitleAnimation: Animator? = null
    private var hideTitleAnimation: Animator? = null

    private var showTopShadow = false
    private var showBottomShadow = false
    private var topShadow:View? = null
    private var bottomShadow:View? = null
    private var shadowsInitialized = false

    protected var binding: Binding? = null

    protected var screen: AnalyticsScreen? = null

    open var isLogoutable = true

    private var rootingCheckCompleted = false

    /**
     * The current state of the keyboard visibility.
     */
    var isKeyboardVisible = false
        set(value) {
            if (field != value) {
                field = value

                onKeyboardVisibleChanged()
            }
        }

    var transitionComplete: (()->Unit)? = null

    /**
     * The ViewModel for the fragment.
     */
    open var viewModel: ViewModel? = null

    /**
     * A value indicating whether the fragment is opaque or transparent.
     */
    var isOpaque = true

    /**
     * The title to use in the toolbar.
     */
    protected var toolbarTitle: String? = null
        set(value) {
            field = value
            toolbar?.title = value
        }

    /**
     * The id of the menu to use for the fragment.
     */
    protected var menuId: Int = 0

    /**
     * The screen orientation for the fragment.
     */
    open val orientation: Int
        get() = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    /**
     * A value indicating whether the status bar and navigation bar should be hidden.
     */
    open val isImmersive: Boolean
        get() = false

    /**
     * Android lifecycle method called when the fragment is attached to an activity.
     *
     * @param context The context for the activity.
     */
    override fun onAttach(context: Context?) {
        logger.log(VERBOSE, "onAttach")

        super.onAttach(context)

        dependencyProvider = (context as BaseActivity).dependencyProvider
        localizationService = dependencyProvider!!.resolve<LocalizationService>()

        configureFragment()
    }

    /**
     * Implemented by derived fragments to configure the properties of the fragment.
     */
    protected open fun configureFragment() {}

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The fragment arguments.
     */
    protected abstract fun inject(fragmentArguments: Bundle?)

    /**
     * Gets a value indicating whether the light status bar theme should be used for this fragment.
     */
    protected fun hasLightStatusBar(): Boolean {
        return lightStatusBar
    }

    /**
     * Sets a value indicating whether the light status bar theme should be used for this fragment.
     */
    protected fun setLightStatusBar(value: Boolean) {
        lightStatusBar = value
    }

    /**
     * Gets a value indicating whether the viewmodel's state shoud be saved with the fragment state.
     */
    protected fun shouldSaveViewModelState(): Boolean {
        return saveViewModelState
    }

    /**
     * Sets a value indicating whether the viewmodel's state shoud be saved with the fragment state.
     */
    protected fun setSaveViewModelState(value: Boolean) {
        saveViewModelState = value
    }

    /**
     * Android lifecycle method called when the fragment is created.
     *
     * @param savedInstanceState The saved state of the fragment.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.log(VERBOSE, "onCreate")

        inject(arguments)

        if (shouldSaveViewModelState()) {
            var viewModelBundle: Bundle? = null
            if (savedInstanceState != null) {
                viewModelBundle = savedInstanceState.getBundle(VIEW_MODEL_BUNDLE_KEY)
            }

            viewModel!!.restoreInstanceState(viewModelBundle)
        }

        viewModel!!.onCreate()

        fragmentResult?.let { result ->
            viewModel!!.onResult(result)
        }
    }

    /**
     * Android lifecycle method called to create the fragment's view
     *
     * @param inflater           The view inflater for the fragment.
     * @param container          The container that the view will be added to.
     * @param savedInstanceState The saved state of the fragment.
     * @return The view for the fragment.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = DataBindingUtil.inflate<Binding>(inflater!!, viewLayoutId, container, false)
        this.binding = binding

        initBinding(binding)

        val view = binding.root

        // root view of the fragment should swallow all touch events to prevent them
        // from propagating to any fragments behind it.
        view.setOnTouchListener { _, _ -> true }

        return view
    }

    /**
     * This method is called when the isKeyboardVisible property changes.
     */
    protected open fun onKeyboardVisibleChanged() {
        binding?.setVariable(BR.isKeyboardVisible, isKeyboardVisible)
        binding?.executePendingBindings()
    }

    /**
     * Called by the base class after the view is created.
     *
     * @param view The view that was created
     * @param savedInstanceState The saved instance state of the fragment.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appbar = view.findViewById<AppBarLayout>(R.id.appbar)
        toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        if (toolbar != null) {
            initToolbar(view)
        }
    }

    /**
     * Initializes the toolbar properties.
     */
    protected open fun initToolbar(rootView: View) {
        toolbar?.setOnMenuItemClickListener { item -> viewModel?.onMenuItem(item) ?: false }
        toolbar?.setNavigationOnClickListener { viewModel?.onNavigation() }

        toolbar?.title = toolbarTitle
        toolbar?.setNavigationIcon(R.drawable.ic_navigate_back)
        initToolbarMenu()
    }

    /**
     * Attaches a listener to a scrollable view that shows/hides the toolbar shadow and title.
     */
    protected fun attachScrollBehavior(scrollingView: View, titleThreshold: Float = 0f) {

        this.titleThreshold = titleThreshold

        if (titleThreshold > 0) {

            showTitleAnimation = AnimatorInflater.loadAnimator(context, R.animator.toolbar_title_in)
            hideTitleAnimation = AnimatorInflater.loadAnimator(context, R.animator.toolbar_title_out)
            showTitleAnimation?.setTarget(toolbar)
            hideTitleAnimation?.setTarget(toolbar)

            toolbar?.setTitleTextColor(getColor(android.R.color.transparent))
            titleVisible = false
        }

        topShadow = findViewById(R.id.topShadow)
        bottomShadow = findViewById(R.id.bottomShadow)
        shadowsInitialized = false

        when (scrollingView) {
            is RecyclerView ->  scrollingView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    updateShadows(scrollingView)
                }
            })

            is NestedScrollView -> scrollingView.setOnScrollChangeListener {
                _: NestedScrollView?, _: Int, _: Int, _: Int, _: Int -> updateShadows(scrollingView) }

            else -> scrollingView.viewTreeObserver.addOnScrollChangedListener {
                updateShadows(scrollingView) }
        }

        updateShadows(scrollingView)
    }

    /**
     * Determines the state of the scroll shadows based on the state of the scrolling view.
     *
     * @param scrollingView The scrolling view associated with the shadows.
     */
    private fun updateShadows(scrollingView: View) {
        var changed = !shadowsInitialized

        val offset: Int
        val extent: Int

        if (scrollingView is ScrollingView) {
            extent = scrollingView.computeVerticalScrollRange() - scrollingView.computeVerticalScrollExtent()
            offset = scrollingView.computeVerticalScrollOffset()
        } else {
            extent = 0
            offset = scrollingView.scrollY
        }

        logger.log(DEBUG, "onScrolled: extent: $extent offset: $offset")

        if (offset >= titleThreshold && !titleVisible) {
            showTitleAnimation?.start()
            titleVisible = true
        } else if (offset < titleThreshold && titleVisible) {
            hideTitleAnimation?.start()
            titleVisible = false
        }

        if (offset > 0 && !showTopShadow)
        {
            showTopShadow = true
            changed = true
        } else if (offset == 0 && showTopShadow) {
            showTopShadow = false
            changed = true
        }

        if (offset < extent && !showBottomShadow)
        {
            showBottomShadow = true
            changed = true
        } else if (offset == extent && showBottomShadow) {
            showBottomShadow = false
            changed = true
        }

        if (changed) {
            showShadows(showTopShadow, showBottomShadow)

            shadowsInitialized = true
        }
    }

    /**
     * Updates the visibility of scroll shadows.
     * @param showTop Indicates that the top shadow should be shown.
     * @param showBottom Indicates that the bottom shadow should be shown.
     */
    protected open fun showShadows(showTop: Boolean, showBottom: Boolean) {
        bottomShadow?.visibility = if (showBottom) View.VISIBLE else View.GONE

        if (topShadow != null) {
            topShadow?.visibility = if (showTop) View.VISIBLE else View.GONE
        } else {
            appbar?.elevation = if (showTop) getDimension(R.dimen.toolbar_elevation) else 0f
        }
    }

    /**
     * Initializes the toolbar menu.
     */
    protected fun initToolbarMenu() {
        if (menuId != 0) {
            toolbar!!.inflateMenu(menuId)
        }
    }

    /**
     * Initializes the binding with the viewmodel. Can be overloaded by derived fragment classes
     * to perform additional initialization of the binding.
     *
     * @param binding The main fragment binding.
     */
    protected open fun initBinding(binding: Binding) {
        binding.setVariable(BR.viewmodel, viewModel)

//        if (binding.setVariable(BR.isKeyboardVisible, isKeyboardVisible)) {
//
//            // the binding has a isKeyboardVisible variable, so connect up a handler
//            // to listen for inset changes that imply that the keyboard is visible.
//            val keyboardSizeThreshold = getDimension(R.dimen.keyboard_inset_threshold)
//            binding.root.setOnApplyWindowInsetsListener { _, windowInsets ->
//                // The keyboard is visible if the bottom system window inset is greater than
//                // a predetermined threshold.
//                val isKeyboardVisible = windowInsets.systemWindowInsetBottom > keyboardSizeThreshold
//                if (isKeyboardVisible != isKeyboardVisible) {
//                    isKeyboardVisible = isKeyboardVisible
//                    binding.setVariable(BR.isKeyboardVisible, isKeyboardVisible)
//                }
//
//                windowInsets
//            }
//        }
    }

    /**
     * Android lifecycle method called when the fragment is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()
        logger.log(VERBOSE, "onDestroy")

        viewModel?.onDestroy()
    }

    /**
     * Android lifecycle method called when the fragment is displayed on the screen.
     */
    override fun onStart() {
        super.onStart()
        logger.log(VERBOSE, "onStart")

        if(!rootingCheckCompleted) {
            checkIfDeviceIsRooted()
        }

        if(screen != null) {
            dependencyProvider?.resolve<AnalyticsService>()?.enterScreen(screen!!.screenName)
        }

        viewModel?.onStart()
    }

    /**
     * Android lifecycle method called when the fragment is removed from the screen.
     */
    override fun onStop() {
        super.onStop()
        logger.log(VERBOSE, "onStop")

        if(screen != null) {
            dependencyProvider?.resolve<AnalyticsService>()?.leaveScreen(screen!!.screenName)
        }

        viewModel?.onStop()
    }

    /**
     * Android lifecycle method called when the fragment is becomes the focused fragment.
     */
    override fun onResume() {
        super.onResume()
        logger.log(VERBOSE, "onResume")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val view = view
            if (view != null) {
                if (hasLightStatusBar()) {
                    view.systemUiVisibility = view.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    view.systemUiVisibility = view.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
            }
        }

        viewModel!!.onResume()
    }

    /**
     * Android lifecycle method called when the fragment is no longer the focused fragment.
     */
    override fun onPause() {
        super.onPause()
        logger.log(VERBOSE, "onPause")

        viewModel!!.onPause()
    }

    /**
     * Called to ask the fragment to save its current dynamic state, so it
     * can later be reconstructed when a new instance of its process is
     * restarted.
     *
     * @param outState Bundle in which to place your saved state.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        logger.log(VERBOSE, "onSaveInstanceState")

        if (shouldSaveViewModelState()) {
            val viewModelBundle = Bundle()
            viewModel!!.saveInstanceState(viewModelBundle)
            outState!!.putBundle(VIEW_MODEL_BUNDLE_KEY, viewModelBundle)
        }
    }

    /**
     * This method is called by the BaseActivity() when the hardware back button is pressed.
     */
    fun onBackPressed() {
        viewModel!!.onBackPressed()
    }

    /**
     * Helper method to retrieve drawable objects.
     */
    @Suppress("DEPRECATION")
    fun getDrawable(id: Int): Drawable {

        return resources.getDrawable(id)
    }

    /**
     * Helper method to retrieve colors
     */
    @Suppress("DEPRECATION")
    fun getColor(id: Int): Int {
        return resources.getColor(id)
    }

    /**
     * Helper method to retrieve dimensions.
     */
    fun getDimension(id: Int): Float {
        return resources.getDimension(id)
    }

    /**
     * Helper method to find a view on the fragment.
     */
    fun findViewById(id: Int): View? {
        var view: View? = null

        val root = getView()
        if (root != null) {
            view = root.findViewById(id)
        }

        return view
    }

    /**
     * Called to deliver a fragment result to this fragment.
     *
     * @param result The result data to be delivered to this fragment.
     */
    open fun onResult(result: Bundle) {
        if (viewModel != null ) {
            viewModel?.onResult(result)
        } else {
            fragmentResult = result
        }
    }

    /**
     * Called when a fragment loads an animation.
     *
     * @param transit  The transition state
     * @param enter    Indicates whether the fragment is entering or exiting.
     * @param nextAnim The id of the next animation.
     */
    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (nextAnim != 0x0) {
            val animation = AnimationUtils.loadAnimation(activity, nextAnim)
            if (transitionComplete != null) {
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {

                    }

                    override fun onAnimationEnd(animation: Animation) {
                        transitionComplete?.invoke()
                    }

                    override fun onAnimationRepeat(animation: Animation) {

                    }
                })
            }

            return animation
        }
        return null
    }

    /**
     * This method checks if the device has been rooted and displays an alert to the user if it is.
     */
    private fun checkIfDeviceIsRooted() {
        rootingCheckCompleted = true
        if(areTestKeysPresent() || doesSuperUserPathExist() || canFindSuperUserExecutable()) {
            val ROOTING_ALERT_ID = "RootingAlert"
            dependencyProvider?.resolve<SystemAlertManager>()?.showAlert (
                    id = ROOTING_ALERT_ID,
                    titleId = R.string.jailbreakWarning_text,
                    primaryButtonTextId = R.string.i_understand_text
            )
        }
    }

    /**
     * This method checks if the OS build tags have the test-keys tag.
     */
    private fun areTestKeysPresent() : Boolean {
        val buildTags = android.os.Build.TAGS
        return buildTags.contains("test-keys")
    }

    /**
     * This method checks if any of the su paths exist.
     */
    private fun doesSuperUserPathExist() : Boolean {
        val possibleSuperUserPaths = listOf("/sbin/su", "su/bin/su", "/system/bin/su", "/system/xbin/su",
                "/system/sd/xbin/su", "/system/bin/failsafe/su", "/system/app/superuser.apk",
                "/data/local/su", "/data/local/bin/su", "/data/local/xbin/su")
        possibleSuperUserPaths.forEach{if(File(it).exists()) return true}
        return false
    }

    /**
     * This method checks if the su executable can be found.
     */
    private fun canFindSuperUserExecutable() : Boolean {
        val paths = listOf("which su", "/system/bin/which su", "/system/xbin/which su")
        for(i in 0 until paths.size) {
            try {
                val process = Runtime.getRuntime().exec(paths[i])
                val br = BufferedReader(InputStreamReader(process.inputStream))
                if (br.readLine() != null) {
                    return true
                }
            } catch (e: Exception) {
            }
        }
        return false
    }

    companion object {
        private val VIEW_MODEL_BUNDLE_KEY = "viewmodel"
        private val SYSTEM_BAR_INSET = "system_bar_inset"
    }
}

