/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase

class CustomMappingModel (
    val importPaths: List<String>,
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

private val finderImportPrefix = "net.juniper.contrail.vro.model"

private fun generateImportStatements(propertyClasses: List<Class<*>>, objectClasses: List<Class<*>>): List<String> {
    val propertyClassImports: List<String> = propertyClasses.map { it.canonicalName }
    val objectClassImports: List<String> = objectClasses.map { it.canonicalName }
    val objectClassNames: List<String> = objectClasses.map { it.simpleName }

    val imports = propertyClassImports + objectClassImports
    val finderImports = objectClassNames.map { "$finderImportPrefix.${it}Finder" }

    return imports + finderImports
}

fun generateCustomMappingModel(
        propertyClasses: List<Class<*>>,
        objectClasses: List<Class<out ApiObjectBase>>,
        rootClasses: List<Class<out ApiObjectBase>>): CustomMappingModel {
    val imports = generateImportStatements(propertyClasses, objectClasses)
    val relations = generateRelationStatements(objectClasses)
    val rootClassesInfo = rootClasses.map { it.toClassInfo() }

    // TODO: How to extract inner classes???
    return CustomMappingModel(imports, listOf(), propertyClasses, objectClasses, rootClassesInfo, relations)
}
