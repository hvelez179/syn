//
// TrackerFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view.tracker


import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.teva.common.services.analytics.enumerations.AnalyticsScreen
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.controls.OddRowDecoration
import com.teva.respiratoryapp.activity.viewmodel.MessageShadeViewModel
import com.teva.respiratoryapp.activity.viewmodel.tracker.TrackerHeaderViewModel
import com.teva.respiratoryapp.activity.viewmodel.tracker.TrackerItemViewModel
import com.teva.respiratoryapp.activity.viewmodel.tracker.TrackerViewModel
import com.teva.respiratoryapp.databinding.TrackerFragmentBinding
import com.teva.respiratoryapp.databinding.TrackerHeaderBinding
import com.teva.respiratoryapp.databinding.TrackerItemBinding
import com.teva.respiratoryapp.mvvmframework.controls.DividerDecoration
import com.teva.respiratoryapp.mvvmframework.controls.HeaderDecoration
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment
import com.teva.respiratoryapp.mvvmframework.ui.HeaderAdapter
import com.teva.userfeedback.enumerations.UserFeeling
import java.time.LocalDate

/**
 * The Tracker screen view.
 */
class TrackerFragment : BaseFragment<TrackerFragmentBinding, TrackerViewModel>(R.layout.tracker_fragment),
        TrackerViewModel.TrackerListener {

    private var trackerAdapter: TrackerAdapter? = null
    private var messageShadeViewModel: MessageShadeViewModel? = null

    init {
        screen = AnalyticsScreen.Tracker()
    }

    /**
     * Implemented by derived fragments to configure the properties of the fragment.
     */
    override fun configureFragment() {
        super.configureFragment()

        toolbarTitle = localizationService!!.getString(R.string.trackerRescueTitle_text)
        menuId = R.menu.tracker_menu
        setSaveViewModelState(true)
    }

    /**
     * Android lifecycle method called when the fragment is displayed on the screen.
     */
    override fun onStart() {
        super.onStart()

        messageShadeViewModel!!.onStart()
    }

    /**
     * Android lifecycle method called when the fragment is removed from the screen.
     */
    override fun onStop() {
        super.onStop()

        messageShadeViewModel!!.onStop()
    }

    /**
     * Android lifecycle method called to create the fragment's view

     * @param inflater           The view inflater for the fragment.
     * *
     * @param container          The container that the view will be added to.
     * *
     * @param savedInstanceState The saved state of the fragment.
     * *
     * @return The view for the fragment.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        // setup the RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.item_list)
        if (recyclerView != null) {
            // setup the adapter
            trackerAdapter = TrackerAdapter()
            recyclerView.adapter = trackerAdapter

            // add odd row background decoration
            val oddRowDecoration = OddRowDecoration(getColor(R.color.colorOddRow))
            recyclerView.addItemDecoration(oddRowDecoration)

            // add the header decoration
            val decor = HeaderDecoration<TrackerHeaderBinding, TrackerHeaderViewModel>(
                    trackerAdapter!!, R.layout.tracker_header,
                    getColor(R.color.colorLightGray),
                    getDrawable(R.drawable.list_header_shadow),
                    getDimension(R.dimen.list_header_shadow_height))
            recyclerView.addItemDecoration(decor)
        }

        return view
    }

    /**
     * Sets the ViewModel for the fragment.

     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        messageShadeViewModel = MessageShadeViewModel(dependencyProvider!!)
        viewModel = TrackerViewModel(dependencyProvider!!)
    }

    /**
     * Initializes the binding with the viewmodel. Can be overloaded by derived fragment classes
     * to perform additional initialization of the binding.

     * @param binding The main fragment binding.
     */
    override fun initBinding(binding: TrackerFragmentBinding) {
        super.initBinding(binding)

        binding.messageShade?.let { it.viewmodel = messageShadeViewModel }
    }

    /**
     * Android lifecycle method called when the fragment is becomes the focused fragment.
     */
    override fun onResume() {
        super.onResume()

        viewModel!!.setListener(this)

        trackerAdapter!!.notifyDataSetChanged()
    }

    /**
     * Android lifecycle method called when the fragment is no longer the focused fragment.
     */
    override fun onPause() {
        super.onPause()

        viewModel!!.setListener(null)
    }

    /**
     * This method is called when something in the tracker changes.
     */
    override fun onUpdated() {
        trackerAdapter!!.notifyDataSetChanged()
    }

    /**
     * This method is called by the RecyclerView adapter when the user clicks on an item.

     * @param position The position that was clicked.
     */
    private fun handleClick(position: Int) {
        viewModel!!.onTrackerItemClicked(position)
    }

    /**
     * This class is the RecyclerView.ViewHolder class for Tracker items.
     *
     * @param binding The data binding for the Tracker item.
     */
    inner class TrackerViewHolder(private val binding: TrackerItemBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {

            itemView.setOnClickListener(this)
        }

        /**
         * The viewmodel object for the data binding.
         */
        var viewModel: TrackerItemViewModel?
            get() = binding.viewmodel
            set(viewModel) {
                binding.viewmodel = viewModel
                binding.executePendingBindings()
            }

        /**
         * This method is the click handler for the Tracker item.
         *
         * @param view The view that was clicked on.
         */
        override fun onClick(view: View) {
            val position = adapterPosition
            handleClick(position)
        }
    }

    /**
     * This class is the RecyclerView.Adapter for the Tracker screen.
     */
    private inner class TrackerAdapter : RecyclerView.Adapter<TrackerViewHolder>(), HeaderAdapter<TrackerHeaderViewModel> {
        private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

        /**
         * Called when RecyclerView needs a new ViewHolder of the given type to represent
         * an item.
         *
         * @param parent   The ViewGroup into which the new View will be added after it is bound to
         * *                 an adapter position.
         * @param viewType The view type of the new View.
         * @return A new ViewHolder that holds a View of the given view type.
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackerViewHolder {
            val binding = DataBindingUtil.inflate<TrackerItemBinding>(layoutInflater, R.layout
                    .tracker_item, parent, false)

            return TrackerViewHolder(binding)
        }

        /**
         * Called by RecyclerView to display the data at the specified position.

         * @param holder   The ViewHolder which should be updated to represent the contents of the
         * *                 item at the given position in the data set.
         * *
         * @param position The position of the item within the adapter's data set.
         */
        override fun onBindViewHolder(holder: TrackerViewHolder, position: Int) {
            val trackerItem = viewModel!!.getTrackerItem(position)
            holder.viewModel = trackerItem
        }

        /**
         * Called when a view created by this adapter has been recycled.

         * @param holder The ViewHolder for the view being recycled
         */
        override fun onViewRecycled(holder: TrackerViewHolder?) {
            val trackerItem = holder?.viewModel
            if (trackerItem != null) {
                viewModel?.recycleTrackerItem(trackerItem)
            }
        }

        /**
         * Returns the total number of items in the data set held by the adapter.

         * @return The total number of items in this adapter.
         */
        override fun getItemCount(): Int {
            return viewModel!!.itemCount
        }

        /**
         * Returns the header id for the item at the given position.

         * @param position the item position
         * *
         * @return the header id
         */
        override fun getHeaderId(position: Int): Int {
            return viewModel!!.getLocalDateForPosition(position).year
        }

        /**
         * Gets the header viewmodel at the specified adapter position.
         */
        override fun getHeaderViewModel(position: Int): TrackerHeaderViewModel {
            val date = viewModel!!.getLocalDateForPosition(position)
            return TrackerHeaderViewModel(date)
        }
    }
}
