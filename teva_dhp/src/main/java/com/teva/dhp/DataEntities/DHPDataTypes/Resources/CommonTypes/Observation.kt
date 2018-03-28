//
// Observation.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.Resources.CommonTypes

data class Observation(var id: String?, var resourceType: String?, var identifier: List<SystemValuePair>?,  var meta: Meta?, var extension: List<Extension>?, var valueQuantity: ValueWithUnit?)
