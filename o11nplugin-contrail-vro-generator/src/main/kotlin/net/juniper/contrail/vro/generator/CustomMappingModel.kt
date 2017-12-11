/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase

class CustomMappingModel (
    val findableClasses: List<Class<*>>,
    val rootClasses: List<ClassInfo>,
    val relations: List<Relation>,
    val referenceRelations: List<RefRelation>,
    val nestedRelations: List<NestedRelation>
) : GenericModel()

fun generateCustomMappingModel(
    objectClasses: List<Class<out ApiObjectBase>>,
    rootClasses: List<Class<out ApiObjectBase>>,
    relationsModel: RelationsModel
): CustomMappingModel {
    val rootClassesInfo = rootClasses.toClassInfo()

    return CustomMappingModel(
        objectClasses,
        rootClassesInfo,
        relationsModel.relations,
        relationsModel.referenceRelations,
        relationsModel.nestedRelations
    )
}
