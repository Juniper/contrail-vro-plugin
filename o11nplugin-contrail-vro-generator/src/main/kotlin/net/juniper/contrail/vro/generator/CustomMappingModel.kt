/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase

class CustomMappingModel (
    val nestedClasses: List<NestedClassInfo>,
    val unfindableClasses: List<Class<*>>,
    val findableClasses: List<Class<*>>,
    val rootClasses: List<ClassInfo>,
    val relations: List<Relation>
) : GenericModel()

fun Class<*>.toClassInfo() =
    ClassInfo(this.simpleName)

fun Class<*>.toNestedClassInfo() =
    NestedClassInfo(this)

class ClassInfo(val simpleName: String) {
    val simpleNameSplitCamel = simpleName.splitCamel()
}

class NestedClassInfo(clazz: Class<*>) {
    val nestedName = clazz.canonicalName.replace("${clazz.`package`.name}.", "")
}

private fun Class<*>.innerClassTree(includeThis: Boolean = false): Sequence<Class<*>> {
    val root = if (includeThis) sequenceOf(this) else emptySequence()
    return root + declaredClasses.asSequence().map { it.innerClassTree(includeThis = true) }.flatten()
}

fun generateCustomMappingModel(
        propertyClasses: List<Class<*>>,
        objectClasses: List<Class<out ApiObjectBase>>,
        rootClasses: List<Class<out ApiObjectBase>>): CustomMappingModel {
    val relations = generateRelationStatements(objectClasses)
    val rootClassesInfo = rootClasses.map { it.toClassInfo() }
    val innerClasses = propertyClasses.asSequence()
        .map { it.innerClassTree() }.flatten()
        .map { it.toNestedClassInfo() }.toList()

    return CustomMappingModel(innerClasses, propertyClasses, objectClasses, rootClassesInfo, relations)
}
