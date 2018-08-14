/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */
package net.juniper.contrail.vro.tests

fun setField(obj: Any, fieldName: String, fieldValue: Any) {
    val f1 = obj.javaClass.getDeclaredField(fieldName)
    f1.isAccessible = true
    f1.set(obj, fieldValue)
}