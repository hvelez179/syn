package com.teva.respiratoryapp.activity.viewmodel.programs

import android.databinding.BaseObservable
import android.databinding.Bindable
import com.teva.cloud.dataentities.ProgramData
import com.teva.respiratoryapp.mvvmframework.ui.IItemViewModel

/**
 * ViewModel for care program items on the Care Programs screen.
 */
class CareProgramItemViewModel : BaseObservable(), IItemViewModel<ProgramData> {

    private var programData : ProgramData? = null

    /**
     * Callback that indicates that the Leave Program hyperlink was clicked.
     */
    var leaveProgramCallback: ((ProgramData) -> Unit)? = null

    /**
     * The name of the care program.
     */
    @get:Bindable
    val programName : String?
        get() = programData?.programName

    /**
     * The id of the care program.
     */
    @get:Bindable
    val programId : String?
        get() = programData?.programId

    /**
     * The profile id of the care program item.
     */
    @get:Bindable
    val profileId : String?
        get() = programData?.profileId

    /**
     * Handler for the Leave Program hyperlink.
     */
    fun onLeaveProgram() {
        leaveProgramCallback?.invoke(programData!!)
    }

    /**
     * Sets a model object as the item for this viewmodel.
     *
     * @param item The model object to use as the item.
     */
    override fun setItem(item: ProgramData) {
        programData = item

        notifyChange()
    }
}