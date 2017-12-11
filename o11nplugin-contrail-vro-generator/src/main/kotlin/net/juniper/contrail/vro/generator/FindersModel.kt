/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import net.juniper.contrail.api.ApiObjectBase

class FindersModel(
    val classes: List<Class<*>>,
    val nestedClasses: List<Class<*>>,
    val nestedRelations: List<NestedRelation>
) : GenericModel()

fun generateFindersModel(
        objectClasses: List<Class<out ApiObjectBase>>,
        nestedClasses: List<Class<*>>,
        relationsModel: RelationsModel
): FindersModel =
    FindersModel(objectClasses, nestedClasses, relationsModel.nestedRelations)
