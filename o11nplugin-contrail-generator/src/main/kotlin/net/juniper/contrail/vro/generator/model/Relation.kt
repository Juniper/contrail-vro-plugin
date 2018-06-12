/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.vro.config.BackRefs
import net.juniper.contrail.vro.config.ObjectClass
import net.juniper.contrail.vro.config.PropertyClass
import net.juniper.contrail.vro.config.PropertyClassFilter
import net.juniper.contrail.vro.config.asObjectClass
import net.juniper.contrail.vro.config.childClassName
import net.juniper.contrail.vro.config.folderName
import net.juniper.contrail.vro.config.isApiPropertyClass
import net.juniper.contrail.vro.config.isApiTypeClass
import net.juniper.contrail.vro.config.isBackRef
import net.juniper.contrail.vro.config.isChildReferenceGetter
import net.juniper.contrail.vro.config.isDisplayableChildOf
import net.juniper.contrail.vro.config.isGetter
import net.juniper.contrail.vro.config.isInReversedRelationTo
import net.juniper.contrail.vro.config.isModelClassName
import net.juniper.contrail.vro.config.nameWithoutGetAndBackRefs
import net.juniper.contrail.vro.config.objectReferenceAttributeClass
import net.juniper.contrail.vro.config.order
import net.juniper.contrail.vro.config.pluralize
import net.juniper.contrail.vro.config.propertyName
import net.juniper.contrail.vro.config.refPropertyName
import net.juniper.contrail.vro.config.referenceFolderName
import net.juniper.contrail.vro.config.returnTypeOrListType
import net.juniper.contrail.vro.config.returnsObjectReferences
import net.juniper.contrail.vro.config.toPluginName
import java.lang.reflect.Method

open class Relation (
    val parentClass: ObjectClass,
    val childClass: ObjectClass
) {
    val parentName: String = parentClass.simpleName
    val childName: String = childClass.simpleName
    val folderName = childClass.folderName
}

class ForwardRelation (
    val declaredParentClass: ObjectClass,
    method: Method
) {
    val declaredChildClass = method.nameWithoutGetAndBackRefs.asObjectClass!!
    val isReversed: Boolean = declaredParentClass.isInReversedRelationTo(declaredChildClass)
    val parentClass: ObjectClass = if (isReversed) declaredChildClass else declaredParentClass
    val childClass: ObjectClass = if (isReversed) declaredParentClass else declaredChildClass
    val parentName: String = parentClass.simpleName
    val childName: String = childClass.simpleName
    val parentPluginName: String = parentName.toPluginName
    val childPluginName: String = childName.toPluginName
    val childNamePluralized = childPluginName.pluralize()
    val pluginGetter = declaredChildClass.refPropertyName
    val getter: String = childName.decapitalize() + if (isReversed) BackRefs else ""
    val folderName = childClass.referenceFolderName
    val attribute = method.objectReferenceAttributeClassOrDefault
    val attributeSimpleName = attribute.simpleName
    val simpleReference = attribute.isSimpleReference
}

class PropertyRelation (
    val parentClass: Class<*>,
    val childClass: PropertyClass,
    val propertyName: String
) {
    val parentName = parentClass.simpleName
    val parentPluginName = parentName.toPluginName
    val childName = childClass.simpleName
    val childPluginName = childName.toPluginName
}

fun List<ObjectClass>.generateRelations() = asSequence()
    .sortedBy { it.order }
    .flatMap { it.relations() }
    .toList()

private fun ObjectClass.relations() = methods.asSequence()
    .filter { it.isChildReferenceGetter }
    .map { it.childClassName }.filterNotNull()
    .filter { it.isModelClassName }
    .map { it.asObjectClass }.filterNotNull()
    .filter { it isDisplayableChildOf this }
    .sortedBy { it.order }
    .map { Relation(this, it) }

fun List<ObjectClass>.generateReferenceRelations(): List<ForwardRelation> =
    asSequence()
        .sortedBy { it.order }
        .flatMap { it.refRelations }
        .filter { contains(it.childClass) }
        .toList()

fun List<ObjectClass>.generatePropertyRelations(propertyFilter: PropertyClassFilter): List<PropertyRelation> =
    asSequence()
        .flatMap { it.propertyRelations(propertyFilter) }
        .toList()

private fun Class<*>.propertyRelations(propertyFilter: PropertyClassFilter): Sequence<PropertyRelation> =
    methods.asSequence()
        .filter { it.isGetter and it.returnType.isApiPropertyClass }
        .filter { propertyFilter(it.returnType as PropertyClass) }
        .map { it.toPropertyRelation() }

private fun Method.toPropertyRelation() = PropertyRelation(
    declaringClass,
    returnTypeOrListType!! as PropertyClass,
    propertyName
)

private val ObjectClass.refRelations: Sequence<ForwardRelation> get() =
    referenceMethods
        .filter { ! it.isBackRef }
        .map { ForwardRelation(this, it) }
        .filter { it.childName isDisplayableChildOf it.parentName }
        .sortedBy { it.childClass.order }

private val ObjectClass.referenceMethods: Sequence<Method> get() =
    declaredMethods.asSequence()
        .filter { it.isRefMethod and it.returnsObjectReferences }

private val Method.objectReferenceAttributeClassOrDefault: Class<*> get() =
    objectReferenceAttributeClass ?: ApiPropertyBase::class.java

val Method.isRefMethod get() =
    isGetter and nameWithoutGetAndBackRefs.isApiTypeClass

private val <T> Class<T>.isSimpleReference: Boolean get() =
    this == ApiPropertyBase::class.java

