/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.PropertyClass
import net.juniper.contrail.vro.config.div
import net.juniper.contrail.vro.config.ProjectInfo
import net.juniper.contrail.vro.config.isRootClass
import net.juniper.contrail.vro.generator.generatedPackageName
import net.juniper.contrail.vro.generator.generatedSourcesRoot
import net.juniper.contrail.vro.generator.templatesInClassPath
import net.juniper.contrail.vro.generator.templatesInResourcesPath

fun generateModel(
    info: ProjectInfo,
    definition: RelationDefinition,
    pluginClasses: List<ObjectClass>,
    modelClasses: List<ObjectClass>,
    propertyClasses: List<PropertyClass>
) {
    val relations = definition.relations
    val forwardRelations = definition.forwardRelations
    val propertyRelations = definition.propertyRelations
    val rootClasses = modelClasses.filter { it.isRootClass }

    val relationsModel = generateRelationsModel(relations, forwardRelations, propertyRelations, rootClasses)
    val customMappingModel = generateCustomMappingModel(info, pluginClasses, rootClasses, propertyClasses, relations, forwardRelations, propertyRelations)
    val findersModel = generateFindersModel(pluginClasses, propertyRelations)

    val templateDir = info.generatorRoot / templatesInResourcesPath

    val customMappingConfig = GeneratorConfig(
        baseDir = info.customRoot / generatedSourcesRoot,
        templateDir = templateDir,
        packageName = generatedPackageName)
    val customMappingGenerator = GeneratorEngine(customMappingConfig, templatesInClassPath)
    customMappingGenerator.generate(customMappingModel, "CustomMapping.kt")

    val coreGeneratorConfig = GeneratorConfig(
        baseDir = info.coreRoot / generatedSourcesRoot,
        templateDir = templateDir,
        packageName = generatedPackageName)

    val coreGenerator = GeneratorEngine(coreGeneratorConfig, templatesInClassPath)
    coreGenerator.generate(relationsModel, "Relations.kt")
    coreGenerator.generate(findersModel, "Finders.kt")
}