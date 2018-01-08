/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows

import net.juniper.contrail.vro.generator.model.RefRelation
import net.juniper.contrail.vro.generator.workflows.model.Action
import net.juniper.contrail.vro.generator.workflows.model.ActionParameter
import net.juniper.contrail.vro.generator.workflows.model.Reference
import net.juniper.contrail.vro.generator.workflows.model.Script
import net.juniper.contrail.vro.generator.workflows.model.array

val RefRelation.getReferencesActionName get() =
    "get${childNamePluralized}Of$parentName"

val RefRelation.getReferencesActionScript
    get() =
"""
var parentId = parent.getInternalId();
var executor = ContrailConnectionManager.getExecutor(parentId.toString());
var elements = executor.$getReferencesActionName(parent);
for each (e in elements) {
    e.setInternalId(parentId.with("$childOriginalName", e.getUuid()));
}
return elements;
""".trimIndent()

fun RefRelation.findReferencesAction(version: String, packageName: String): Action {
    val name = getReferencesActionName
    val resultType = array(Reference(childOriginalName))
    val parameters = listOf(ActionParameter("parent", Reference(parentName)))
    return Action(
        name = name,
        packageName = packageName,
        id = generateID(packageName, name),
        version = version,
        resultType = resultType,
        parameters = parameters,
        script = Script(getReferencesActionScript)
    )
}