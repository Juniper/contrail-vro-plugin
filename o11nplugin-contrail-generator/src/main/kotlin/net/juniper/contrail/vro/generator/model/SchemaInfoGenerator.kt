package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.vro.config.ProjectInfo
import net.juniper.contrail.vro.config.div
import net.juniper.contrail.vro.generator.generatedPackageName
import net.juniper.contrail.vro.generator.generatedSourcesRoot
import net.juniper.contrail.vro.generator.templatesInClassPath
import net.juniper.contrail.vro.generator.templatesInResourcesPath
import net.juniper.contrail.vro.schema.Schema
import net.juniper.contrail.vro.schema.Property

fun generateSchemaInfo(projectInfo: ProjectInfo, schema: Schema) {
    val templateDir = projectInfo.generatorRoot / templatesInResourcesPath
    val propertyNameModel = generatePropertyNameModel(schema)

    val coreGeneratorConfig = GeneratorConfig(
        baseDir = projectInfo.genRoot / generatedSourcesRoot,
        templateDir = templateDir,
        packageName = generatedPackageName)

    val coreGenerator = GeneratorEngine(coreGeneratorConfig, templatesInClassPath)
    coreGenerator.generate(propertyNameModel, "schemaInfo.kt")
}

fun generatePropertyNameModel(schema: Schema): PropertyNameModel {
    val readOnlyProperties = schema.propertyComments.filter { it.crud.isReadOnly }
    return PropertyNameModel(readOnlyProperties.asReadOnlyPropertyNames)
}

data class PropertyName(val parentClassName: String, val elementName: String)

data class PropertyNameModel(val propertyNames: List<PropertyName>) : GenericModel()

val Sequence<Property>.asReadOnlyPropertyNames get() =
    map { PropertyName(it.parentClassName, it.elementName) }.toList()