/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

class WrappersModel(
    val fieldInfos: List<Pair<String, ClassUsageInfo>>)
: GenericModel()

fun generateWrappersModel(nestedClasses: List<Class<*>>): WrappersModel {
    val fieldInfos = nestedClasses.map { Pair(
            it.canonicalName.split(".").joinToString("") { it.capitalize() },
            it.classUsages(nestedClasses.toList())
    ) }
    return WrappersModel(fieldInfos)

    /* Use either
         -    1) canonical name instead of simple name
         - or 2) cascade of simple names
       to handle `X { B { C } }` vs `Y { B { C } }` cases
     */
}

