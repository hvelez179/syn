package com.teva.medication.dataquery

import android.support.annotation.WorkerThread

import com.teva.common.dataquery.DataQueryForTrackedModels
import com.teva.medication.entities.Prescription

import org.threeten.bp.Instant

/**
 * Classes conforming to this protocol allow access to the prescription data.
 */
@WorkerThread
interface PrescriptionDataQuery : DataQueryForTrackedModels<Prescription> {
    /**
     * Returns the earliest prescription date for the specified drug.
     *
     * @return The earliest prescription date.
     */
    fun getEarliestPrescriptionDate(): Instant?
}
