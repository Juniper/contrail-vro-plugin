/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */
package net.juniper.contrail.vro.tests

fun setField(obj: Any, fieldName: String, fieldValue: Any) {
    val f1 = obj.javaClass.getDeclaredField(fieldName)
    f1.isAccessible = true
    f1.set(obj, fieldValue)
}

// this method is necessary because Groovy Tuple2 is ignored
fun getPairOf(clazz1: Class<*>, clazz2: Class<*>) =
    Pair(clazz1.simpleName, clazz2.simpleName)