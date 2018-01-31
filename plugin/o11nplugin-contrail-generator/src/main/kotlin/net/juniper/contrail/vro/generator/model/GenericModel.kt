/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.vro.config.constants.apiTypesPackageName
import net.juniper.contrail.vro.generator.editWarningMessage
import net.juniper.contrail.vro.generator.generatedPackageName

open class GenericModel {
    val editWarning: String = editWarningMessage
    val packageName: String = generatedPackageName
    val contrailPackageName: String = apiTypesPackageName
}
