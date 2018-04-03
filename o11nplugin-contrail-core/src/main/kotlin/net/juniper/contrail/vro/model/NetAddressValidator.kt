/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import org.apache.commons.validator.routines.InetAddressValidator

enum class NetAddressValidator(val maxPrefixLength: Int) {
    IPv4(30) {
        override fun isValidAddress(address: String) =
            validator.isValidInet4Address(address.trim())
    },
    IPv6(126) {
        override fun isValidAddress(address: String) =
            validator.isValidInet6Address(address.trim())
    };

    protected val validator get() =
        InetAddressValidator.getInstance()

    abstract fun isValidAddress(address: String): Boolean

    fun isValidPool(pool: String): Boolean {
        val parts = pool.trim().split('-')
        if (parts.size != 2) return false
        return isValidAddress(parts[0]) && isValidAddress(parts[1])
    }

    fun isValidSubnet(subnet: String): Boolean =
        isCidr(subnet, maxPrefixLength)

    fun isValidCidr(subnet: String): Boolean =
        isCidr(subnet, maxPrefixLength + 2)

    fun isCidr(cidr: String, maxPrefix: Int) : Boolean {
        val parts = cidr.trim().split('/')
        if (parts.size != 2) return false
        val prefixLen = parts[1].toIntOrNull() ?: return false
        if (prefixLen < 0 || prefixLen > maxPrefix) return false
        return isValidAddress(parts[0])
    }

    fun areValidPools(pools: List<String>): Boolean {
        if (pools.isBlankList()) return false
        return pools.all { isValidPool(it) }
    }

    companion object {
        private val values = values()

        fun isValidPool(pool: String): Boolean {
            val parts = pool.trim().split('-')
            if (parts.size != 2) return false
            return values.asSequence().filter { it.isValidAddress(parts[0]) && it.isValidAddress(parts[1]) }.any()
        }

        fun isValidAddress(address: String): Boolean =
            values.asSequence().filter { it.isValidAddress(address) }.any()

        fun isValidSubnet(subnet: String): Boolean =
            values.asSequence().filter { it.isValidSubnet(subnet) }.any()

        fun isValidCidr(subnet: String): Boolean =
            values.asSequence().filter { it.isValidCidr(subnet) }.any()

        fun areValidPools(pools: List<String>) : Boolean {
            return values.asSequence().filter { it.areValidPools(pools) }.any()
        }
    }
}