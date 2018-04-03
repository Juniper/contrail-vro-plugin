/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

interface Element {
    val id: String
    val outputName: String
    val elementType: ElementType
}

enum class ElementType {
    Workflow,
    ScriptModule
}
