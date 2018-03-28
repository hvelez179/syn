//
// DashboardFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view


import android.graphics.Outline
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.view.*
import com.teva.analysis.enumerations.SummaryTextId
import com.teva.common.services.analytics.AnalyticsService
import com.teva.common.services.analytics.enumerations.AnalyticsScreen
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.MessageShadeViewModel
import com.teva.respiratoryapp.activity.viewmodel.dashboard.DashboardStateViewModel
import com.teva.respiratoryapp.activity.viewmodel.dashboard.DashboardViewModel
import com.teva.respiratoryapp.activity.viewmodel.dashboard.MenuItem
import com.teva.respiratoryapp.databinding.DashboardFragmentBinding
import com.teva.respiratoryapp.databinding.MenuItemBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseActivity
import com.teva.respiratoryapp.mvvmframework.ui.ViewModelListFragment
import kotlinx.android.synthetic.main.dashboard_fragment.*

/**
 * Fragment class for the Dashboard screen.
 */
class DashboardFragment
    : ViewModelListFragment<DashboardFragmentBinding, DashboardViewModel, MenuItemBinding, MenuItem>(
        R.layout.dashboard_fragment, R.layout.menu_item) {

    private var messageShadeViewModel: MessageShadeViewModel? = null

    init {
        screen = AnalyticsScreen.Dashboard()
    }

    /**
     * Closes the menu drawer.
     */
    fun closeMenu() {
        drawer?.closeDrawer(Gravity.START)
    }

    /**
     * Opens the menu drawer.
     */
    fun openMenu() {
        drawer?.openDrawer(Gravity.START)
    }

    /**
     * Sets the ViewModel for the fragment.
     */
    override fun inject(fragmentArguments: Bundle?) {
        messageShadeViewModel = MessageShadeViewModel(dependencyProvider!!)

        val vm = DashboardViewModel(dependencyProvider!!)
        vm.closeMenuCallback = { closeMenu() }

        viewModel = vm
    }

    /**
     * Android lifecycle method called to create the fragment's view
     *
     * @param inflater           The view inflater for the fragment.
     * @param container          The container that the view will be added to.
     * @param savedInstanceState The saved state of the fragment.
     * @return The view for the fragment.
     */
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        // navigation drawer doesn't respond unless the fragment has an empty touch listener.
        view.setOnTouchListener { _, _ -> false }
        drawer?.addDrawerListener(object: DrawerLayout.DrawerListener {
            override fun onDrawerSlide(view: View, v: Float) {
            }

            override fun onDrawerOpened(view: View) {
                dependencyProvider?.resolve<AnalyticsService>()?.enterScreen(AnalyticsScreen.Menu().screenName)
            }

            override fun onDrawerClosed(view: View) {
                dependencyProvider?.resolve<AnalyticsService>()?.leaveScreen(AnalyticsScreen.Menu().screenName)
            }

            override fun onDrawerStateChanged(i: Int) {
            }
        })
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        card.outlineProvider = CardOutlineProvider

        val menu = findViewById(R.id.menu_drawer)
        val screen = findViewById(R.id.screen)
        view.setOnApplyWindowInsetsListener { v, windowInsets ->
            menu?.onApplyWindowInsets(windowInsets)
            screen?.onApplyWindowInsets(windowInsets)
        }
    }

    /**
     * Updates the status bar and navigation bar colors if the fragment
     * is still attached to the activity.
     */
    private fun updateScreenFeatures() {
        val baseActivity = activity as? BaseActivity
        baseActivity?.updateScreenFeatures()
    }

    /**
     * Initializes the binding with the viewmodel. Can be overloaded by derived fragment classes
     * to perform additional initialization of the binding.
     *
     * @param binding The main fragment binding.
     */
    override fun initBinding(binding: DashboardFragmentBinding) {
        super.initBinding(binding)

        binding.messageShade?.let { it.viewmodel = messageShadeViewModel }
        binding.dashboardState = dependencyProvider!!.resolve<DashboardStateViewModel>()
    }

    /**
     * Android lifecycle method called when the fragment is displayed on the screen.
     */
    override fun onStart() {
        super.onStart()

        messageShadeViewModel?.onStart()
    }

    /**
     * Android lifecycle method called when the fragment is removed from the screen.
     */
    override fun onStop() {
        super.onStop()

        messageShadeViewModel?.onStop()
    }

    companion object {
        /**
         * An outline provider used to display a shadow that is narrower than the card.
         */
        val CardOutlineProvider = object : ViewOutlineProvider() {

            /**
             * Creates an outline for the view.
             * @param view The view to provide an outline for.
             * @param outline The outline object to be defined.
             */
            override fun getOutline(view: View, outline: Outline) {
                val width = view.measuredWidth
                val height = view.measuredHeight
                val padding = view.resources.getDimensionPixelOffset(R.dimen.dashboard_card_shadow_padding)
                val radius = view.resources.getDimension(R.dimen.dashboard_card_corner_radius)
                outline.setRoundRect(padding, height/2, width - padding, height, radius)
            }
        }

        @JvmStatic
        fun summaryCardToImage(summaryCard: SummaryTextId): Int {
            return when (summaryCard) {
                SummaryTextId.OVERUSE -> 0
                SummaryTextId.NO_INHALERS -> R.drawable.ic_myinhalers
                SummaryTextId.EMPTY_INHALER -> R.drawable.check_dose_counter
                SummaryTextId.ENVIRONMENT_MESSAGE -> 0
                SummaryTextId.NEUTRAL_MESSAGE -> 0
            }
        }
    }
}
