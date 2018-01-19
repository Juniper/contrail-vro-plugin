/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows.xsd

import net.juniper.contrail.vro.config.camelChunks


val String.propertyToXsd get() =
    camelChunks.joinToString(separator = "-") { it.decapitalize() }

val String.stripSmi get() =
    if (startsWith("smi:")) substring(4) else this
