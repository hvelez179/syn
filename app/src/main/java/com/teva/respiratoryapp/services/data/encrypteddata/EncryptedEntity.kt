//
// EncryptedEntity.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.services.data.encrypteddata

import android.annotation.SuppressLint
import com.teva.respiratoryapp.services.data.EntityProtocol

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

import java.util.HashMap

/**
 * If a class conforms to this protocol, it is considered an Encrypted Database Entity.
 */
open class EncryptedEntity : EntityProtocol {

    /**
     * Indicates whether this instance of EncryptedEntity is new. This is used to determine whether to Insert or Update when saving.
     */
    override var isNew = true

    /**
     * Mapping between the entity properties and their column names/values in the database.
     */
    var schemaMap: MutableMap<String, Any?> = HashMap()

    var primaryKeyId: Int
        get() = schemaMap["Z_PK"] as Int
        set(key) {
            schemaMap.put("Z_PK", key)
        }

    var hasChanged: Int
        get() = getIntProperty("hasChanged")
        set(value) {
            schemaMap.put("hasChanged", value)
        }

    var changedTime: Instant?
        get() = getInstantProperty("changedTime")
        set(value) = setInstantProperty("changedTime", value)

    var created: Instant?
        get() = getInstantProperty("created")
        set(value) = setInstantProperty("created", value)

    protected fun getIntProperty(name: String): Int {
        val value = schemaMap[name]
        if (value != null) {
            return value as Int
        }

        throw IllegalStateException("Int $name not found")
    }

    protected fun getNullableIntProperty(name: String): Int? {
        return schemaMap[name] as Int?
    }

    protected fun getStringProperty(name: String): String {
        val value = schemaMap[name]
        if (value != null) {
            return schemaMap[name] as String
        }

        throw IllegalStateException("String $name not found")
    }

    protected fun getNullableStringProperty(name: String): String? {
        return schemaMap[name] as String?
    }

    protected fun getInstantProperty(name: String): Instant? {
        val value = schemaMap[name]
        if (value is Int) {
            return Instant.ofEpochSecond(value.toLong())
        }

        return null
    }

    protected fun setInstantProperty(name: String, value: Instant?) {
        if (value != null) {
            schemaMap.put(name, value.epochSecond.toInt())
        } else {
            schemaMap.put(name, null)
        }
    }

    protected fun getLocalDateProperty(name: String): LocalDate? {
        val value = schemaMap[name]
        if (value is Long) {
            return LocalDate.ofEpochDay(value)
        } else if (value is Int) {
            return LocalDate.ofEpochDay(value.toLong())
        }

        return null
    }

    protected fun setLocalDateProperty(name: String, value: LocalDate?) {
        if (value != null) {
            schemaMap.put(name, value.toEpochDay())
        } else {
            schemaMap.put(name, null)
        }
    }

    companion object {

        /**
         * Converts a list of EncryptedEntity derived objects into a map using the primary key as the map key.
         *
         * @param list The list of objects.
         * @param <T> A class derived from EncryptedEntity.
         * @return A map of objects keyed by the primary key.
        </T> */
        @SuppressLint("UseSparseArrays")
        fun <T : EncryptedEntity> toMap(list: List<T>): Map<Int, T> {
            val map = HashMap<Int, T>()

            for (obj in list) {
                map.put(obj.primaryKeyId, obj)
            }

            return map
        }
    }
}
