//
// SecKeyHelpers.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.dhp.models

import java.math.BigInteger
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec


/**
 * This class contains methods to create, store, and retrieve a public key.
 * The helpers are needed to validate the signature of a JSON Web Token.
 */
class SecKeyHelpers {
    companion object {
        /**
         * This method generates a public key from the provided modulus and exponent.
         * @param modulusString: the public key modulus component
         * @param exponentString: the public key exponent component
         * @return: Data
         */
        internal fun createKey(modulusString: String, exponentString: String): PublicKey {
            val modulus = BigInteger(modulusString, 16)
            val exponent = BigInteger(exponentString, 16)
            val spec = RSAPublicKeySpec(modulus, exponent)

            val factory = KeyFactory.getInstance("RSA")
            return factory.generatePublic(spec)
        }
    }
}