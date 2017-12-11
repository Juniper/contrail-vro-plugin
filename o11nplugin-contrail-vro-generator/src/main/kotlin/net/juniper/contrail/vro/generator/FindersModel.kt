/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase

class FindersModel(
    val classes: List<Class<*>>,
    val nestedClasses: List<Class<*>>,
    val referenceWrappers: List<ReferenceWrapper>,
    val nestedRelations: List<NestedRelation>
) : GenericModel()

fun generateFindersModel(
    objectClasses: List<Class<out ApiObjectBase>>,
    nestedClasses: List<Class<*>>,
    wrappersModel: WrappersModel,
    relationsModel: RelationsModel
): FindersModel =
    FindersModel(objectClasses, nestedClasses, wrappersModel.references, relationsModel.nestedRelations)
