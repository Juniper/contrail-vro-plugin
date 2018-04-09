/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package net.juniper.contrail.vro.model

import java.math.BigInteger
import java.net.InetAddress

const val LONG_LENGTH = 64
val highestIpv4 = IPv4(-1)
val highestIpv6 = IPv6(-1, -1)

typealias JavaLong = java.lang.Long

interface IP<Ip : IP<Ip>> : Comparable<Ip> {
    val highestBinary: Ip
    val maxPrefix: Int
    infix fun and(other: Ip) : Ip
    infix fun or(other: Ip) : Ip
    infix fun shl(bits: Int) : Ip
    infix fun ushr(bits: Int) : Ip
}

class IPv4(val ip: Int) : IP<IPv4> {
    override val highestBinary get() = highestIpv4
    override val maxPrefix get() = 32

    constructor (address: String) : this(address.ipv4ToInt())

    override operator fun compareTo(other: IPv4) =
        Integer.compareUnsigned(ip, other.ip)

    override fun equals(other: Any?): Boolean =
        if (other is IPv4) ip == other.ip else false

    override fun hashCode(): Int = ip.hashCode()

    override infix fun shl(bits: Int) : IPv4 =
        IPv4(ip shl bits)

    override infix fun ushr(bits: Int) : IPv4 =
        IPv4(ip ushr bits)

    override infix fun and(other: IPv4) : IPv4 =
        IPv4(ip and other.ip)

    override infix fun or(other: IPv4) : IPv4 =
        IPv4(ip or other.ip)

    override fun toString() : String =
        Integer.toBinaryString(ip)
}

class IPv6(val highBits: Long, val lowBits: Long) : IP<IPv6> {
    override val highestBinary get() = highestIpv6
    override val maxPrefix get() = 128

    constructor (address: String) : this(address.ipv6toLong(0..7), address.ipv6toLong(8..15))

    override fun equals(other: Any?): Boolean =
        if (other is IPv6) highBits == other.highBits && lowBits == other.lowBits else false

    override fun hashCode(): Int =
        BigInteger(toString(), 2).hashCode()

    override operator fun compareTo(other: IPv6) : Int {
        val high = JavaLong.compareUnsigned(highBits, other.highBits)
        if (high != 0) return high
        return JavaLong.compareUnsigned(lowBits, other.lowBits)
    }

    override infix fun and(other: IPv6) : IPv6 =
        IPv6(highBits and other.highBits, lowBits and other.lowBits)

    override infix fun or(other: IPv6) : IPv6 =
        IPv6(highBits or other.highBits, lowBits or other.lowBits)

    override infix fun shl(bits: Int) : IPv6 {
        val lowToCopy : Long
        val highShifted : Long
        val newLowBits : Long
        if (bits < LONG_LENGTH) {
            highShifted = highBits shl bits
            lowToCopy = lowBits ushr (LONG_LENGTH - bits)
            newLowBits = lowBits shl bits
        } else {
            highShifted = 0
            newLowBits = 0
            if (bits < maxPrefix) {
                lowToCopy = lowBits shl (bits - LONG_LENGTH)
            } else {
                lowToCopy = 0
            }
        }
        return IPv6(highShifted or lowToCopy, newLowBits)
    }

    override infix fun ushr(bits: Int): IPv6 {
        val lowShifted : Long
        val newHighBits : Long
        val highToCopy : Long
        if (bits < LONG_LENGTH) {
            lowShifted = lowBits ushr bits
            highToCopy = highBits shl (LONG_LENGTH - bits)
            newHighBits = highBits ushr bits
        } else {
            lowShifted = 0
            newHighBits = 0
            if (bits < maxPrefix) {
                highToCopy = highBits ushr (bits - LONG_LENGTH)
            } else {
                highToCopy = 0
            }
        }
        return IPv6(newHighBits, lowShifted or highToCopy)
    }

    override fun toString() : String =
        JavaLong.toBinaryString(highBits) + JavaLong.toBinaryString(lowBits)
}

class IpRange<T : IP<T>>(val start: T, val end: T) {
    operator fun contains(ip: T): Boolean =
        ip in (start..end)

    operator fun contains(range: IpRange<T>): Boolean =
        range.start <= range.end && range.end <= end && range.start >= start

    fun overlaps(other: IpRange<T>): Boolean =
        other.start in this || other.end in this
}

fun <T: IP<T>> IpRange<T>.isValidInSubnet(subnet: IpRange<T>, allPools: List<IpRange<T>>) : Boolean =
    this in subnet && allPools.minus(this).none { it.overlaps(this) }

fun ipToByte(address: String) : ByteArray =
    InetAddress.getByName(address).address

fun String.ipv4ToInt() : Int {
    val bytes = ipToByte(trim())
    var result = 0
    for (byte in bytes) {
        result = result shl 8
        result = result or (byte.toInt() and 0xff)
    }
    return result
}

fun String.ipv6toLong(range: IntRange) : Long {
    val bytes = ipToByte(trim())
    var result : Long = 0
    for (byte in bytes.slice(range)) {
        result = result shl 8
        result = result or (byte.toLong() and 0xffL)
    }
    return result
}

fun <T: IP<T>> String?.equalsIp(ip : T, ipFactory: (String) -> T): Boolean =
    if (this != null && isNotBlank()) ipFactory(this) == ip else false

fun <T: IP<T>> T.notInPools(pools : List<String>?, ipFactory: (String) -> T) : Boolean {
    if (pools != null) {
        return pools.isBlankList() || poolsToRanges(pools.trimList(), ipFactory).none { this in it }
    } else {
        return true
    }
}

fun <T: IP<T>> T.getNetworkAddress(prefixLen: Int) : T =
    this and (highestBinary shl (maxPrefix - prefixLen))

fun <T: IP<T>> T.getNetworkBroadcastAddress(prefixLen: Int) : T =
    this or (highestBinary ushr prefixLen)

fun <T : IP<T>> poolToRange(pool: String, ipFactory: (String) -> T) : IpRange<T>? {
    val parts = pool.split('-')
    val start = ipFactory(parts[0])
    val end = ipFactory(parts[1])
    if (start > end) return null
    return IpRange(start, end)
}

fun <T : IP<T>> poolsToRanges(pools: List<String>, ipFactory: (String) -> T) : List<IpRange<T>> =
    pools.map { poolToRange(it, ipFactory) ?: return emptyList() }

fun <T : IP<T>> subnetRange(cidr: String, ipFactory: (String) -> T) : IpRange<T> {
    val parts = cidr.trim().split('/')
    val prefixLen = parts[1].toInt()
    val ip = ipFactory(parts[0])
    val netAddr = ip.getNetworkAddress(prefixLen)
    val broadcast = ip.getNetworkBroadcastAddress(prefixLen)
    return IpRange(netAddr, broadcast)
}

fun <T : IP<T>> parsePools(cidr: String, pools: List<String>, ipFactory: (String) -> T) : Boolean {
    if (pools.isBlankList()) return false
    val subnet = subnetRange(cidr.trim(), ipFactory)
    val ranges = poolsToRanges(pools.trimList(), ipFactory)
    if (ranges.isEmpty()) return false
    return ranges.all { it.isValidInSubnet(subnet, ranges) }
}

fun List<String>?.isBlankList(): Boolean =
    this?.all { it.isBlank() } ?: true

fun List<String>.trimList(): List<String> =
    map { it.trim() }.filter { it.isNotBlank() }

