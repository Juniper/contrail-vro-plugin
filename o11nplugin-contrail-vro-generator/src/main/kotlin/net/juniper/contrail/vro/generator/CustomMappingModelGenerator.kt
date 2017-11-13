/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.vro.relation.extractRelations
import net.juniper.contrail.vro.relation.nonAbstractSubclassesIn

private val finderImportPrefix = "net.juniper.contrail.vro.model"
private fun generateImportStatements(propertyClasses: List<Class<*>>, objectClasses: List<Class<*>>): List<String> {
    val propertyClassImports: List<String> = propertyClasses.map { it.canonicalName }
    val objectClassImports: List<String> = objectClasses.map { it.canonicalName }
    val objectClassNames: List<String> = objectClasses.map { it.simpleName }

    val imports = propertyClassImports + objectClassImports
    val finderImports = objectClassNames.map { "$finderImportPrefix.${it}Finder" }

    return imports + finderImports
}

private fun generateRelationStatements(): List<CustomMappingModel.Relation> {
    // TODO: What about Domain and ConfigRoot classes?
    // TODO: (Their relations are extracted but we do not represent them in VRO)
    val relationsGraph = extractRelations()
    return relationsGraph.map { relationsNode ->
        relationsNode.second.map {
            CustomMappingModel.Relation(
                dashedClassNameToCamelCase(relationsNode.first),
                dashedClassNameToCamelCase(it.childTypeName),
                dashedClassNameToCamelCase(it.name)
            )
        }
    }.flatten()
}

private fun dashedClassNameToCamelCase(name: String): String {
    // DNS -> Dns because reasons
    return name.split("-").map { it.capitalize() }.joinToString("").replace("DNS", "Dns")
}

fun generateCustomMappingModel(): CustomMappingModel {
    val propertyClasses = ApiPropertyBase::class.java.nonAbstractSubclassesIn("net.juniper.contrail.api")
    val objectClasses = ApiObjectBase::class.java.nonAbstractSubclassesIn("net.juniper.contrail.api")
    val imports = generateImportStatements(propertyClasses, objectClasses)
    val relations = generateRelationStatements()

    // TODO: How to extract inner classes???
    return CustomMappingModel(imports, listOf(), propertyClasses, objectClasses, relations)
}
