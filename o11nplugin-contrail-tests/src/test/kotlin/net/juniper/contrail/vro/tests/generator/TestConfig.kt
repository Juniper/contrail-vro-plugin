/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.generator

import net.juniper.contrail.vro.config.ConfigContext

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
    )
)