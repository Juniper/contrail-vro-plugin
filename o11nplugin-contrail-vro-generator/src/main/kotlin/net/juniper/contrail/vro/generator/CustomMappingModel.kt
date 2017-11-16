/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase

class CustomMappingModel (
    val canonicalNameClasses: List<Class<*>>,
    val unfindableClasses: List<Class<*>>,
    val findableClasses: List<Class<*>>,
    val rootClasses: List<ClassInfo>,
    val relations: List<Relation>
) : GenericModel()

fun Class<*>.toClassInfo() =
    ClassInfo(this.simpleName)

class ClassInfo(val simpleName: String) {
    val simpleNameSplitCamel = simpleName.splitCamel()
}

fun generateCustomMappingModel(
        propertyClasses: List<Class<*>>,
        objectClasses: List<Class<out ApiObjectBase>>,
        rootClasses: List<Class<out ApiObjectBase>>): CustomMappingModel {
    val relations = generateRelationStatements(objectClasses)
    val rootClassesInfo = rootClasses.map { it.toClassInfo() }

    // TODO: How to extract inner classes???
    return CustomMappingModel(listOf(), propertyClasses, objectClasses, rootClassesInfo, relations)
}
