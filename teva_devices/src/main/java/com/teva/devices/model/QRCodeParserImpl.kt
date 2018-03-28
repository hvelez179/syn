///
// QRCodeParserImpl.java
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.devices.model

import android.util.Base64

import com.teva.common.utilities.isNumeric

import java.util.Locale

/**
 * Parses text from a QRCode into serial number, authentication key, and rxUID.
 */
class QRCodeParserImpl : QRCodeParser {

    /**
     * Parses the QR Code text of an inhaler into a serial number and authentication key.
     * @param qrCodeText The QR Code text.
     * *
     * @return A QRCode object containing information from the QR Code text.
     */
    override fun parse(qrCodeText: String): QRCode? {
        var text = qrCodeText
        var qrCode: QRCode? = null

        val index = text.indexOf('?')
        if (index >= 0) {
            text = text.substring(index + 1)
        }

        var productID: String? = null
        var serialNumber: String? = null
        var authenticationKey: String? = null

        val parts = text.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (part in parts) {
            if(part.startsWith("p=")) {
                productID = part.substring(2)
            } else if (part.startsWith("s=")) {
                serialNumber = base64ToSerialNumber(part.substring(2))
            } else if (part.startsWith("a=")) {
                authenticationKey = base64ToAuthenticationKey(part.substring(2))
            }
        }

        if (serialNumber != null
                && isValidSerialNumber(serialNumber)
                && authenticationKey != null) {
            qrCode = QRCode(productID, serialNumber, authenticationKey)
        }

        return qrCode
    }

    /**
     * Verifies the format and length of a serial number string.
     * @param serialNumber The serial number string to verify.
     * *
     * @return True if the serial number is valid, false otherwise.
     */
    private fun isValidSerialNumber(serialNumber: String?): Boolean {
        return serialNumber != null
                && serialNumber.length == SERIAL_NUMBER_LENGTH
                && serialNumber.isNumeric()
    }

    /**
     * Converts a base64 string to an authentication key
     * @param base64Text The base64 buffer containing the encoded key.
     * *
     * @return A hex string representing the authentication key
     */
    override fun base64ToAuthenticationKey(base64Text: String): String? {
        var keyString: String? = null

        val isValid = base64Text.length == AUTHENTICATION_CODE_BASE64_LENGTH
                && base64Text.matches("^[a-zA-Z0-9-_.]*$".toRegex())

        if (isValid) {
            val stringBuilder = StringBuilder(AUTHENTICATION_CODE_STRING_LENGTH)

            try {
                val data = Base64.decode(convertToStandardForm(base64Text), Base64.DEFAULT)
                for (index in 0..AUTHENTICATION_CODE_BYTE_LENGTH - 1) {
                    val keyByte = data[index % data.size]
                    stringBuilder.append(String.format("%02x", keyByte))
                }

                keyString = stringBuilder.toString()
            } catch (ex: IllegalArgumentException) {
                // the base64 string was invalid, so just do nothing
                // as the keyString return result is already null.
            }

        }

        return keyString
    }

    /**
     * Converts a base64 string from the Teva form to the standard form.
     * @param base64Text The text to convert.
     * *
     * @return The standard form.
     */
    private fun convertToStandardForm(base64Text: String): String {
        return base64Text.replace('.', '=')
                .replace('-', '+')
                .replace('_', '/')
    }

    /**
     * Converts a base64 string to a serial number string
     * @param base64Text The base64 buffer containing the serial number
     * *
     * @return A string containing the serial number
     */
    private fun base64ToSerialNumber(base64Text: String): String {
        val data = Base64.decode(convertToStandardForm(base64Text), Base64.DEFAULT)

        var serialNumber: Long = 0
        for (b in data) {
            serialNumber = serialNumber shl 8 or (b.toLong() and 0xff)
        }

        return String.format(Locale.US, "%011d", serialNumber)
    }

    companion object {
        private val SERIAL_NUMBER_LENGTH = 11
        private val AUTHENTICATION_CODE_BASE64_LENGTH = 12
        val AUTHENTICATION_CODE_STRING_LENGTH = 32
        val AUTHENTICATION_CODE_BYTE_LENGTH = 16
    }
}
