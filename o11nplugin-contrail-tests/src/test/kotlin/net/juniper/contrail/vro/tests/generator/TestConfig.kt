/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.generator

import net.juniper.contrail.api.types.IdPermsType
import net.juniper.contrail.api.types.PermType2
import net.juniper.contrail.vro.config.ConfigContext
import net.juniper.contrail.vro.config.the

val defaultTestConfig = ConfigContext(
    modelClasses = setOf(
    ),

    inventoryProperties = setOf(
    ),

    customPropertyObjects = setOf(
    ),

    nonEssentialAttributes = setOf(
    ),

    ignoredInWorkflows = setOf(
        the<PermType2>(),
        the<IdPermsType>()
    ),

    nonEditableProperties = setOf(
        "displayName",
        "parentType",
        "defaultParentType",
        "objectType",
        "networkId"
    ),

    customPropertyValidation = mapOf(
    ),

    customCreateWorkflows = setOf(
    ),

    customEditWorkflows = setOf(
    ),

    customDeleteWorkflows = setOf(
    ),

    directChildren = setOf(
    ),

    mandatoryReference = setOf(
    ),

    nonEditableReference = setOf(
    ),

    customAddReference = setOf(
    ),

    customRemoveReference = setOf(
    ),

    hiddenRoots = setOf(
    ),

    hiddenRelations = setOf(
    ),

    tagRelations = setOf(
    ),

    relationAsProperty = setOf(
    ),

    reversedRelations = setOf(
    ),

    readUponQuery = setOf(
    ),

    validateSecurityScope = setOf(
    ),

    draftClasses = setOf(
    )
)