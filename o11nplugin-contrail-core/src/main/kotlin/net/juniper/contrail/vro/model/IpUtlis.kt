/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import java.net.InetAddress

const val MAX_IPv4_PREFIX = 32
const val HIGHEST_BINARY = -1

class IpRange(val start: Ip, val end: Ip) {

    operator fun contains(ip: Ip): Boolean =
        ip <= end && ip >= start

    operator fun contains(range: IpRange): Boolean =
        range.start <= range.end && range.end <= end && range.start >= start

    fun overlaps(other: IpRange): Boolean =
        other.start in this || other.end in this
}

class Ip(val ip: Int) : Comparable<Ip> {
    constructor(address: String): this(address.ipToInt())

    override fun compareTo(other: Ip) = Integer.compareUnsigned(ip, other.ip)
}

fun Int.ip() : Ip = Ip(this)

fun IpRange.isValidInSubnet(subnet: IpRange, allPools: List<IpRange>) : Boolean =
    this in subnet && allPools.minus(this).none { it.overlaps(this) }

fun ipToByte(address: String) : ByteArray =
    InetAddress.getByName(address).address

fun String.ipToInt() : Int {
    val bytes = ipToByte(trim())
    var result = 0
    for (byte in bytes) {
        result = result shl 8
        result = result or (byte.toInt() and 0xff)
    }
    return result
}

fun String?.equalsIp(ip : Ip) =
    this?.ipToInt()?.ip() == ip

fun String?.ipNotInPools(ip : Ip) : Boolean {
    if (this != null) {
        val cleanedPools = trimMultiline()
        return cleanedPools.isNotBlank() && poolsToRanges(cleanedPools).none { ip in it }
    } else {
        return true
    }
}

fun getNetworkAddress(ip: Int, prefixLen: Int) : Int =
    ip and (HIGHEST_BINARY shl (MAX_IPv4_PREFIX - prefixLen))

fun getNetworkBroadcastAddress(ip: Int, prefixLen: Int) : Int =
    ip or (HIGHEST_BINARY ushr prefixLen)

fun getSubnetRange(cidr: String) : IpRange {
    val parts = cidr.trim().split('/')
    val prefixLen = parts[1].toInt()
    val ip = parts[0].ipToInt()
    val netAddr = getNetworkAddress(ip, prefixLen)
    val broadcast = getNetworkBroadcastAddress(ip, prefixLen)
    return IpRange(Ip(netAddr), Ip(broadcast))
}

fun poolToRange(pool: String) : IpRange? {
    val parts = pool.split('-')
    val start = Ip(parts[0])
    val end = Ip(parts[1])
    if (start > end) return null
    return IpRange(start, end)
}

fun poolsToRanges(pools: String) : List<IpRange> {
    val lines = pools.split('\n')
    return lines.map { poolToRange(it) ?: return emptyList() }
}

fun parseIpv4Pools(cidr: String, pools: String) : Boolean {
    val subnet = getSubnetRange(cidr.trim())
    val ranges = poolsToRanges(pools.trimMultiline())
    if (ranges.isEmpty()) return false
    return ranges.all { it.isValidInSubnet(subnet, ranges) }
}

fun String.trimMultiline(): String =
    splitMultiline().joinToString( "\n" )

fun String.splitMultiline(): List<String> =
    split('\n').map { it.trim() }.filter { it.isNotBlank() }