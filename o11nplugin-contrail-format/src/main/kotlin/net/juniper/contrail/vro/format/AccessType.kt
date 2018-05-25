/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.format

enum class AccessType(private val mask: Int) {
    Read(4),
    Write(2),
    Refer(1);

    fun allowed(access: Int) =
        access and mask > 0

    companion object {
        private const val empty = "-"
        private val values = values()

        fun format(access: Int?): String {
            if (access == null) return empty
            val formatted = values.asSequence().filter { it.allowed(access) }.joinToString { it.name }
            return if (formatted.isBlank()) empty else formatted
        }
    }
}