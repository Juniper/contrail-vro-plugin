/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.util

fun <T> MutableList<T>.addIfAbsent(element: T) {
    if (!contains(element))
        add(element)
}