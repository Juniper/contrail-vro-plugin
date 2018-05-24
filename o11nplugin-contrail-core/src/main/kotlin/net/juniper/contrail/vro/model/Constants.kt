/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import net.juniper.contrail.vro.base.Description
import net.juniper.contrail.vro.config.constants.supportedInterfaceNames

@Description("Object containing constants used in workflows.")
class Constants {
    val serviceInterfaceNames get() = supportedInterfaceNames.toList()
}

val constants = Constants()