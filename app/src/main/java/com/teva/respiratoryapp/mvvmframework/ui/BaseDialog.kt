package com.teva.respiratoryapp.mvvmframework.ui

import android.app.Dialog
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.support.v4.view.ScrollingView
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.teva.common.services.analytics.AnalyticsService
import com.teva.common.services.analytics.enumerations.AnalyticsScreen
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.*
import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.mvvmframework.utils.blur

/**
 * Base class for dialogs
 *
 * @param context The parent activity context
 * @property viewModel The dialog's viewmodel
 * @property viewLayoutId The layout id of the dialog's content.
 */
abstract class BaseDialog<in Binding : ViewDataBinding,ViewModel : FragmentViewModel>(
        activity: BaseActivity,
        protected var viewLayoutId: Int)
    : Dialog(activity, R.style.DialogTheme) {

    protected val logger: Logger = Logger(this.javaClass.simpleName)

    val dependencyProvider: DependencyProvider
    protected var localizationService: LocalizationService? = null

    protected var hasBlurredBackground = false
    private var lightStatusBar = true
    private var saveViewModelState = false
    protected var screen: AnalyticsScreen? = null

    private var showTopShadow = false
    private var showBottomShadow = false
    private var topShadow: View? = null
    private var bottomShadow: View? = null
    private var shadowsInitialized = false
    private var scrollingView: View? = null

    private var binding: Binding? = null

    var viewModel: ViewModel? = null
        set(value) {
            field = value
            value?.onBackPressedCallback = { dismiss() }
        }

    val layoutChangeListener = View.OnLayoutChangeListener { view, left, top, right, bottom, oldLeft,
                                                             oldTop, oldRight, oldBottom ->
        if (left != oldLeft || right != oldRight || top != oldTop || bottom != oldBottom) {
            val blurBitmap = view.blur()
            if (blurBitmap != null) {
                window.setBackgroundDrawable(BitmapDrawable(context.resources, blurBitmap))
            }
        }
    }

    /**
     * A value indicating whether the fragment is opaque or transparent.
     */
    var isOpaque = true

    /**
     * Implemented by derived fragments to configure the properties of the fragment.
     */
    protected open fun configureDialog() {}

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

    init {
        ownerActivity = activity

        dependencyProvider = activity.dependencyProvider
        localizationService = dependencyProvider.resolve<LocalizationService>()
    }

    /**
     * Android lifecycle method called when the fragment is created.
     *
     * @param savedInstanceState The saved state of the fragment.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.log(VERBOSE, "onCreate")

        configureDialog()

        if (shouldSaveViewModelState()) {
            var viewModelBundle: Bundle? = null
            if (savedInstanceState != null) {
                viewModelBundle = savedInstanceState.getBundle(VIEW_MODEL_BUNDLE_KEY)
            }

            viewModel!!.restoreInstanceState(viewModelBundle)
        }

        val view = onCreateView(layoutInflater, null, savedInstanceState)
        setContentView(view)

        val decorView = window.decorView

        decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        viewModel!!.onCreate()

        onViewCreated(view, savedInstanceState)
    }

    /**
     * Called by the base class after the view is created.
     *
     * @param view The view that was created
     * @param savedInstanceState The saved instance state of the fragment.
     */
    protected open fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    }

    /**
     * Android lifecycle method called to create the dialog's view
     *
     * @param inflater           The view inflater for the fragment.
     * @param container          The container that the view will be added to.
     * @param savedInstanceState The saved state of the fragment.
     * @return The view for the fragment.
     */
    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate<Binding>(inflater, viewLayoutId, container, false)

        initBinding(binding!!)

        return binding!!.root
    }

    /**
     * Attaches a listener to a scrollable view that shows/hides the toolbar shadow and title.
     */
    protected fun attachScrollBehavior(scrollingView: View) {

        this.scrollingView = scrollingView
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

        scrollingView.post {
            updateShadows(scrollingView)
        }
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
            val a = scrollingView.computeVerticalScrollRange()
            val b = scrollingView.computeVerticalScrollExtent()
            extent = scrollingView.computeVerticalScrollRange() - scrollingView.computeVerticalScrollExtent()
            offset = scrollingView.computeVerticalScrollOffset()
        } else {
            extent = 0
            offset = scrollingView.scrollY
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
        topShadow?.visibility = if (showTop) View.VISIBLE else View.GONE
    }


    /**
     * Android lifecycle method called when the fragment is displayed on the screen.
     */
    override fun onStart() {
        super.onStart()
        logger.log(VERBOSE, "onStart")

        // connect up handler to listen to layout changes of parent activity
        // to generate the blurred background
        if (hasBlurredBackground) {

            val decorView = ownerActivity.window.decorView

            decorView.addOnLayoutChangeListener(layoutChangeListener)

            val blurBitmap = decorView.blur()
            if (blurBitmap != null) {
                window.setBackgroundDrawable(BitmapDrawable(context.resources, blurBitmap))
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val view = window.decorView
            if (view != null) {
                if (hasLightStatusBar()) {
                    view.systemUiVisibility = view.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    view.systemUiVisibility = view.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
            }
        }

        if(screen != null) {
            dependencyProvider.resolve<AnalyticsService>().enterScreen(screen!!.screenName)
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
            dependencyProvider.resolve<AnalyticsService>().leaveScreen(screen!!.screenName)
        }

        viewModel?.onStop()
    }

    /**
     * Called to ask the fragment to save its current dynamic state, so it
     * can later be reconstructed when a new instance of its process is
     * restarted.
     *
     * @return Returns a bundle containing the dialog's saved state.
     */

    override fun onSaveInstanceState(): Bundle {
        val outState = super.onSaveInstanceState()

        logger.log(VERBOSE, "onSaveInstanceState")

        if (shouldSaveViewModelState()) {
            val viewModelBundle = Bundle()
            viewModel!!.saveInstanceState(viewModelBundle)
            outState.putBundle(VIEW_MODEL_BUNDLE_KEY, viewModelBundle)
        }

        return outState
    }

    /**
     * Initializes the binding with the viewmodel. Can be overloaded by derived fragment classes
     * to perform additional initialization of the binding.
     *
     * @param binding The main fragment binding.
     */
    protected open fun initBinding(binding: Binding) {
        binding.setVariable(BR.viewmodel, viewModel)
    }

    companion object {
        private val VIEW_MODEL_BUNDLE_KEY = "viewmodel"
    }

}