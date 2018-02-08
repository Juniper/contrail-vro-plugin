/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

private val separator = "__in__"

fun folderName(actualName: String, parentName: String) =
    "$actualName$separator$parentName"

fun folderName(actualName: String, parentName: String, childName: String) =
    "$actualName$separator${parentName}_$childName"