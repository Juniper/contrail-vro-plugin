/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import net.juniper.contrail.vro.config.CDATA
import net.juniper.contrail.vro.workflows.dsl.workflowEndItemId
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "workflow",
    propOrder = ["displayName", "refTypes", "position", "input", "output", "attributes", "workflowItems", "presentation"]
)
@XmlRootElement(name = "workflow")
class Workflow(
    displayName: String,
    id: String,
    version: String,
    presentation: Presentation = Presentation(),
    workflowItems: List<WorkflowItem> = emptyList(),
    references: List<Reference>? = null,
    input: ParameterSet = ParameterSet(),
    output: ParameterSet = ParameterSet(),
    attributes: List<Attribute> = emptyList(),
    position: Position = Position(50.0f, 10.0f),
    rootItemId: Int = workflowEndItemId
) : Element {
    // this constructor is only necessary to satisfy marshaller
    constructor(): this("Workflow", "0", "0.0.0")

    @XmlElement(name = "display-name", required = true)
    val displayName: String = displayName

    @XmlElement(name = "ref-types")
    val refTypes: String? = references?.run {
        if (isEmpty()) null else joinToString(separator = ")(", prefix = "(", postfix = ")")
    }.CDATA

    @XmlAttribute(name = "root-name")
    val rootName: String = "item$rootItemId"

    @XmlAttribute(name = "id")
    override val id: String = id

    @XmlAttribute(name = "version")
    val version: String = version

    @XmlElement(required = true)
    val input: ParameterSet = input

    @XmlElement(required = true)
    val output: ParameterSet = output

    @XmlElement(name = "attrib")
    val attributes: List<Attribute> = attributes.toList()

    @XmlElement(name = "workflow-item")
    val workflowItems: List<WorkflowItem> = workflowItems.toList()

    @XmlElement(required = true)
    val presentation: Presentation = presentation

    @XmlElement(required = true)
    val position: Position = position

    @XmlAttribute(name = "object-name")
    val objectName: String = "workflow:name=generic"

    @XmlAttribute(name = "api-version")
    val apiVersion: String = "6.0.0"

    @XmlAttribute(name = "allowed-operations")
    val allowedOperations: String = "vef"

    @XmlAttribute(name = "restartMode")
    val restartMode: String = "1"

    @XmlAttribute(name = "resumeFromFailedMode")
    val resumeFromFailedMode: String = "0"

    override val outputName: String get() =
        displayName

    override val elementType: ElementType get() =
        ElementType.Workflow
}
