//
// MedicationSKU.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.services.data.encrypteddata

/**
 * This class is used for de-serializing the medication SKU
 * information from the json file.
 *
 * @property drugUID The id of the medication
 * @property doseCount The inital dose count of the device.
 * @property expInMonthsFromFirstUse The number months after
 *           first use that the device is considered expired.
 */
class MedicationSKU(var drugUID: String? = null,
                    var doseCount: Int = 0,
                    var expInMonthsFromFirstUse: Int = 0)