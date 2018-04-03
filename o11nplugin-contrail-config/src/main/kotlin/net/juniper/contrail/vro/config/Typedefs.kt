/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.ApiPropertyBase

typealias PropertyClass = Class<out ApiPropertyBase>
typealias ObjectClass = Class<out ApiObjectBase>

typealias PropertyClassFilter = (PropertyClass) -> Boolean
typealias ObjectClassFilter = (ObjectClass) -> Boolean