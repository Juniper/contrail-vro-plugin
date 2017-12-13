/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.ObjectReference
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

open class Relation (
    val parentName: String,
    val childName: String
) {
    val name: String = relationName(parentName, childName)
    val childNameDecapitalized = childName.decapitalize()
    val folderName = childName.folderName()
}

class RefRelation (
    parentClass: Class<out ApiObjectBase>,
    method: Method
) {
    val parentName: String = parentClass.simpleName
    val childName: String = method.referenceName
    val childOriginalName: String = method.nameWithoutGetAndBackRefs
    val getter: String = method.propertyName
    val folderName = method.nameWithoutGetAndBackRefs.folderName()
}

class NestedRelation(
    val parent: Class<*>,
    val child: Class<*>,
    val simpleProperties: List<Property>,
    val listProperties: List<Property>,
    val getterChain: List<Getter>,
    val rootClass: Class<*>
) {
    val parentName: String = parent.nestedName
    val childName: String = child.nestedName
    val parentCollapsedName = parent.collapsedNestedName
    val childCollapsedName = child.collapsedNestedName
    val getter: String = getterChain.last().name
    val getterDecapitalized = getter.decapitalize()
    val getterSplitCamel = getter.splitCamel()
    val name: String = relationName(parentCollapsedName, getter)
    val childWrapperName = rootClass.simpleName + getterChain.joinToString("") { "_" + it.name }
    val parentWrapperName = rootClass.simpleName + getterChain.dropLast(1).joinToString("") { "_" + it.name }
    val folderName = child.simpleName.folderName()
    val toMany: Boolean = getterChain.last().toMany
}

class Getter(val name: String, val toMany: Boolean) {
    val nameDecapitalized: String = name.decapitalize()
}

private fun RelationGraphVertex.asRelationList(): List<Relation> {
    return second.map {
        Relation(
            it.name.typeToClassName(),
            first.typeToClassName(),
            it.childTypeName.typeToClassName()
        )
    }
}

typealias RelationGraphNode = Pair<String, List<String>>

fun generateRelations(classes: List<Class<out ApiObjectBase>>): List<Relation> {
    val parentToChildren = classes.groupBy { it.defaultParentType }
    return classes.asSequence()
        .filter { it.isRelatable }
        .map { createRelationGraphNode(it, parentToChildren) }
        .map { it.toRelationSequence() }
        .flatten().toList()
}

private fun createRelationGraphNode(
    parentClass: Class<out ApiObjectBase>,
    parentToChildren: Map<String?, List<Class<out ApiObjectBase>>>
): RelationGraphNode {
    val parentType = parentClass.objectType
    val children = parentToChildren.getOrElse(parentType) { listOf() }
    val childrenTypes = children.map { it.objectType.dashedToCamelCase() }
    return RelationGraphNode(parentType.dashedToCamelCase(), childrenTypes)
}

private fun relationName(parentType: String, childType: String) =
    "${parentType}To$childType"

private fun RelationGraphNode.toRelationSequence(): Sequence<Relation> =
    second.asSequence().map { Relation( first, it) }

fun generateReferenceRelations(classes: List<Class<out ApiObjectBase>>): List<RefRelation> =
    classes.asSequence()
        .map { it.refRelations }
        .flatten()
        .toList()

fun generateNestedRelations(classes: List<Class<*>>): List<NestedRelation> =
    classes.asSequence()
        .map { it.nestedRelations(classes, listOf(), it) }
        .flatten()
        .toList()


private fun Class<*>.nestedRelations(
    baseClasses: List<Class<*>>,
    chainSoFar: List<Getter>,
    rootClass: Class<*>
): Sequence<NestedRelation> =
    methods.asSequence()
        .filter { it.isGetter and it.returnsApiPropertyOrList }
        .map { it.recursiveRelations(baseClasses, chainSoFar, rootClass) }
        .flatten()

private fun Method.recursiveRelations(
    baseClasses: List<Class<*>>,
    chainSoFar: List<Getter>,
    rootClass: Class<*>
): Sequence<NestedRelation> {
    val wrapperChildName = nameWithoutGet

    val (childType, toMany) = when (returnType) {
        List::class.java -> Pair(returnListGenericClass!!, true)
        else -> Pair(returnType, false)
    }

    val newChain = chainSoFar + Getter(wrapperChildName, toMany)

    val relation = NestedRelation(
        declaringClass,
        childType,
        childType.properties.simpleProperties,
        childType.properties.listProperties,
        newChain,
        rootClass
    )

    return if (baseClasses.contains(childType))
        sequenceOf(relation)
    else
        childType.nestedRelations(baseClasses, newChain, rootClass) + relation
}

private val <T: ApiObjectBase> Class<T>.refRelations: Sequence<RefRelation> get() =
    referenceMethods.distinctBy { it.referenceName }.map { RefRelation(this, it) }

private val <T: ApiObjectBase> Class<T>.referenceMethods: Sequence<Method> get() =
    declaredMethods.asSequence()
        .filter { it.isRefMethod and it.returnsObjectReferences }

private val Type.parameterClass: Class<*>? get() =
    if (this is ParameterizedType) actualTypeArguments[0].unwrapped else null

private val Type.unwrapped: Class<*> get() =
    if (this is ParameterizedType) rawType as Class<*> else this as Class<*>

private val Method.isRefMethod get() =
    isGetter and nameWithoutGetAndBackRefs.isApiTypeClass

private val Method.returnListGenericClass: Class<*>? get() =
    if (returnType == List::class.java) genericReturnType.parameterClass else null

private val Method.returnsObjectReferences: Boolean get() =
    returnListGenericClass == ObjectReference::class.java

private val Method.returnsApiPropertyOrList: Boolean get() =
    if (returnType.isApiTypeClass) true
    else returnListGenericClass?.isApiTypeClass ?: false
