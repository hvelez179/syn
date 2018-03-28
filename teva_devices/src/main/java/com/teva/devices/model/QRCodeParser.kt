//
// QRCodeParser.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.model

/**
 * Parses text from a QRCode into serial number, authentication key, and rxUID.
 */
interface QRCodeParser {

    /**
     * Parses the QR Code text of an inhaler into a serial number and authentication key.
     * @param qrCodeText The QR Code text.
     * *
     * @return A QRCode object containing information from the QR Code text.
     */
    fun parse(qrCodeText: String): QRCode?

    /**
     * Converts a base64 string to an authentication key
     * @param base64Text The base64 buffer containing the encoded key.
     * *
     * @return A hex string representing the authentication key
     */
    fun base64ToAuthenticationKey(base64Text: String): String?

}
