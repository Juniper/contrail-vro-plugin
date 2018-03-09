/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.PropertyClass
import net.juniper.contrail.vro.config.rootClassFilter
import net.juniper.contrail.vro.config.div
import net.juniper.contrail.vro.config.ProjectInfo
import net.juniper.contrail.vro.generator.generatedPackageName
import net.juniper.contrail.vro.generator.generatedSourcesRoot
import net.juniper.contrail.vro.generator.templatePath

fun generateModel(
    info: ProjectInfo,
    definition: RelationDefinition,
    objectClasses: List<ObjectClass>,
    propertyClasses: List<PropertyClass>
) {
    val relations = definition.relations
    val forwardRelations = definition.forwardRelations
    val nestedRelations = definition.nestedRelations
    val rootClasses = objectClasses.filter(rootClassFilter)

    val relationsModel = generateRelationsModel(relations, forwardRelations, nestedRelations, rootClasses)
    val customMappingModel = generateCustomMappingModel(info, objectClasses, rootClasses, propertyClasses, relations, forwardRelations, nestedRelations)
    val wrappersModel = generateWrappersModel(nestedRelations)
    val findersModel = generateFindersModel(objectClasses, nestedRelations)
    val executorModel = generateExecutorModel(objectClasses)

    val customMappingConfig = GeneratorConfig(
        baseDir = info.customRoot / generatedSourcesRoot,
        packageName = generatedPackageName)
    val customMappingGenerator = GeneratorEngine(customMappingConfig, templatePath)
    customMappingGenerator.generate(customMappingModel, "CustomMapping.kt")

    val coreGeneratorConfig = GeneratorConfig(
        baseDir = info.coreRoot / generatedSourcesRoot,
        packageName = generatedPackageName)

    val coreGenerator = GeneratorEngine(coreGeneratorConfig, templatePath)
    coreGenerator.generate(relationsModel, "Relations.kt")
    coreGenerator.generate(findersModel, "Finders.kt")
    coreGenerator.generate(executorModel, "Executor.kt")
    coreGenerator.generate(wrappersModel, "Wrappers.kt")
}