/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import net.juniper.contrail.vro.base.Description
import net.juniper.contrail.vro.config.defaultConfig
import net.juniper.contrail.vro.schema.classDescription
import net.juniper.contrail.vro.schema.defaultSchema

val Class<*>.description: String? get() =
    extractCustomDescription() ?: extractSchemaDescription()

private fun Class<*>.extractSchemaDescription(): String? =
    defaultSchema.classDescription(this, defaultConfig)

private fun Class<*>.extractCustomDescription(): String? =
    getAnnotation(Description::class.java)?.value
