/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.vro.config.ProjectInfo
import net.juniper.contrail.vro.config.div
import net.juniper.contrail.vro.config.isApiTypeClass
import net.juniper.contrail.vro.config.isGetter
import net.juniper.contrail.vro.config.kotlinClassName
import net.juniper.contrail.vro.config.pluginPropertyName
import net.juniper.contrail.vro.config.propertyName
import net.juniper.contrail.vro.config.returnListGenericClass
import net.juniper.contrail.vro.config.underscoredNestedName
import net.juniper.contrail.vro.config.wrapperName
import net.juniper.contrail.vro.generator.generatedPackageName
import net.juniper.contrail.vro.generator.generatedSourcesRoot
import net.juniper.contrail.vro.generator.templatesInClassPath
import net.juniper.contrail.vro.generator.templatesInResourcesPath
import net.juniper.contrail.vro.schema.defaultSchema
import java.lang.reflect.Method

val readOnlyProperties = defaultSchema.propertyComments.filter { it.crud.isReadOnly }

class Property(
    val propertyName: String,
    val pluginPropertyName: String,
    val clazz: Class<*>,
    val parent: Class<*>,
    val declaringClass: Class<*>,
    val isList: Boolean,
    val wrapname: String? = null)
{
    val componentName get() = propertyName.replace("List$".toRegex(), "").capitalize()
    val classLabel get() = if (clazz.isApiTypeClass) clazz.underscoredNestedName else clazz.kotlinClassName
    val wrapperName get() = wrapname ?: if (clazz.isApiTypeClass) parent.wrapperName(propertyName) else clazz.kotlinClassName
}

open class ClassProperties(
    val properties: List<Property>
) {
    val simpleProperties: List<Property> = properties.filter { ! it.isList }
    val listProperties: List<Property> = properties.filter { it.isList }
}

val <T> Class<T>.properties: ClassProperties get() =
    declaredMethods.asSequence()
        .filter { it.isGetter }
        .map { it.toPropertyOf(this) }
        .filterNotNull()
        .toClassProperties()

private fun Sequence<Property>.toClassProperties() =
    ClassProperties(toList())

private fun Method.toPropertyOf(parent: Class<*>): Property? {
    val returnsList = returnType == List::class.java
    val type = if (returnsList)
        returnListGenericClass ?: return null
    else
        returnType

    return Property(propertyName, pluginPropertyName, type, parent, declaringClass, returnsList)
}

fun generateSchemaInfo(projectInfo: ProjectInfo) {
    val templateDir = projectInfo.generatorRoot / templatesInResourcesPath
    val propertyNameModel = generatePropertyNameModel()

    val coreGeneratorConfig = GeneratorConfig(
        baseDir = projectInfo.genRoot / generatedSourcesRoot,
        templateDir = templateDir,
        packageName = generatedPackageName)

    val coreGenerator = GeneratorEngine(coreGeneratorConfig, templatesInClassPath)
    coreGenerator.generate(propertyNameModel, "schemaReadOnlyPropertyNames.kt")
}

fun generatePropertyNameModel() =
    PropertyNameModel(readOnlyProperties.asReadOnlyPropertyNames)

data class PropertyName(val parentClassName: String, val elementName: String)

data class PropertyNameModel(val propertyNames: List<PropertyName>) : GenericModel()

val Sequence<net.juniper.contrail.vro.schema.Property>.asReadOnlyPropertyNames get() =
    map { PropertyName(it.parentClassName, it.elementName) }.toList()