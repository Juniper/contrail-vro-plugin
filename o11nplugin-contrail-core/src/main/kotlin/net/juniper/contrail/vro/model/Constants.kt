/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import net.juniper.contrail.vro.config.constants.supportedInterfaceNames

class Constants {
    val serviceInterfaceNames get() = supportedInterfaceNames.toList()
}