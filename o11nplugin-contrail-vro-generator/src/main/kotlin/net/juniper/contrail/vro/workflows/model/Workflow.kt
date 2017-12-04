/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "workflow",
    propOrder = arrayOf("displayName", "position", "input", "output", "workflowItems", "presentation")
)
@XmlRootElement(name = "workflow")
class Workflow {

    @XmlElement(name = "display-name", required = true)
    var displayName: String? = null

    @XmlElement(required = true)
    var position: Position? = null

    @XmlElement(required = true)
    var input: Input? = null

    @XmlElement(required = true)
    var output: Output? = null

    @XmlElement(name = "workflow-item")
    var workflowItems: MutableList<WorkflowItem> = mutableListOf()

    @XmlElement(required = true)
    var presentation: Presentation? = null

    @XmlAttribute(name = "root-name")
    var rootName: String? = null

    @XmlAttribute(name = "object-name")
    var objectName: String? = null

    @XmlAttribute(name = "id")
    var id: String? = null

    @XmlAttribute(name = "version")
    var version: String? = null

    @XmlAttribute(name = "api-version")
    var apiVersion: String? = null

    @XmlAttribute(name = "allowed-operations")
    var allowedOperations: String? = null

    @XmlAttribute(name = "restartMode")
    var restartMode: String? = null

    @XmlAttribute(name = "resumeFromFailedMode")
    var resumeFromFailedMode: String? = null

}
