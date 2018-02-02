/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

val actionPackage = globalProjectInfo.workflowPackage
val utilActionPackage = "$actionPackage.util"

val cidrCheckingAction = "isValidCidr"
val propertyNotNull = "propertyNotNull"
val propertyValue = "propertyValue"

/* extractListPropertyAction
arguments:
    parentItem : Any
    childItem : String (a human-readable representation of the object with it's index at the beginning)
    listAccessor: String
    propertyPath: String
script:
    var objectIndex = ContrailUtils.ruleStringToIndex(childItem);
    var child = eval("parentItem." + listAccessor + "[" + objectIndex + "]");
    return eval("child." + propertyPath);
 */
val extractListPropertyAction = "getListPropertyValue"