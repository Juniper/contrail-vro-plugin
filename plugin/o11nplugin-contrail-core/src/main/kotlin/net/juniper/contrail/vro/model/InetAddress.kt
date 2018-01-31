/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import org.apache.commons.validator.routines.InetAddressValidator

enum class InetAddress (val maxPrefixLength: Int) {
    IPv4(30) {
        override fun isValidAddress(address: String) =
            validator.isValidInet4Address(address)
    },
    IPv6(126) {
        override fun isValidAddress(address: String) =
            validator.isValidInet6Address(address)
    };

    protected val validator get() =
        InetAddressValidator.getInstance()

    abstract fun isValidAddress(address: String): Boolean

    fun isValidSubnet(subnet: String): Boolean {
        val parts = subnet.split('/')
        if (parts.size != 2) return false
        val prefixLen = parts[1].toIntOrNull() ?: return false
        if (prefixLen < 0 || prefixLen > maxPrefixLength) return false
        return isValidAddress(parts[0])
    }

    companion object {
        private val values = values()

        fun isValidAddress(address: String): Boolean =
            values.asSequence().filter { it.isValidAddress(address) }.any()

        fun isValidSubnet(subnet: String): Boolean =
            values.asSequence().filter { it.isValidSubnet(subnet) }.any()
    }
}