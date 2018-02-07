/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

val actionPackage = globalProjectInfo.workflowPackage
val utilActionPackage = "$actionPackage.util"

val isCidrAction = "isValidCidr"
val propertyNotNull = "propertyNotNull"
val propertyValue = "propertyValue"
val isInCidrAction = "isInCidr"
val isFreeInCidrAction = "isFreeInCidr"
val isAllocPoolAction = "isValidAllocationPool"

/* extractListProperty
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
val extractListProperty = "getListPropertyValue"

/* getNetworkPolicyRules
arguments:
    netpolicy : Any (a network policy)
script:
    var actionResult = new Array();
    var rules = netpolicy.getEntries().getPolicyRule()

    rules.forEach(function (value, index) {
        actionResult.push(ContrailUtils.ruleToString(value, index));
    });
    return actionResult;
 */
val getNetworkPolicyRules = "getNetworkPolicyRules"
