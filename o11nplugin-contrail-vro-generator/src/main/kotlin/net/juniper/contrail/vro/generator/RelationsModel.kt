/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase

class RelationsModel(
    val rootClassNames: List<String>,
    val relations: List<Relation>,
    val nestedRelations: List<NestedRelation>
) : GenericModel()

fun generateRelationsModel(
    objectClasses: List<Class<out ApiObjectBase>>,
    propertyClasses: List<Class<*>>
): RelationsModel {
    val relations = generateRelations(objectClasses)
    // val nestedRelations = generateNestedRelations(objectClasses + propertyClasses)
    val nestedRelations = generateNestedRelations(objectClasses)
    /*
    class NestedRelation(
        parent: Class<*>,
        child: Class<*>,
        getter: String,
        simpleProperties: List<Property>,
        listProperties: List<Property>,
        getterChain: List<String>,
        val toMany: Boolean = false
    ) {
    */
    for (nestedRelation in nestedRelations) {
        println("#REL# -- NEXT RELATION --")
        println("#REL# " + nestedRelation.parentName)
        println("#REL# " + nestedRelation.childName)
        println("#REL# " + nestedRelation.name)
        println("#REL# " + nestedRelation.getter)
        println("#REL# " + nestedRelation.getterChain)
        println("#REL# " + nestedRelation.toMany)
    }
    val rootClassNames = objectClasses.rootClasses()
        .map { it.simpleName }

    return RelationsModel(rootClassNames, relations, nestedRelations)
}
