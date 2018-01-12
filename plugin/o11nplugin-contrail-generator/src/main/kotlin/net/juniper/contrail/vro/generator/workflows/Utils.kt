/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows

import com.google.common.hash.Hashing

fun generateID(packageName: String, displayName: String) =
    Hashing.md5().newHasher()
        .putString("$packageName.$displayName", Charsets.UTF_8)
        .hash().toString()