/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.workflows.model

import net.juniper.contrail.vro.generator.util.CDATA
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "workflow-itemType",
    propOrder = ["displayName", "script", "inBinding", "outBinding", "position"]
)
class WorkflowItem(

    @XmlAttribute(name = "name")
    var name: String? = null,

    @XmlAttribute(name = "type")
    var type: String? = null,

    @XmlElement(required = true)
    var position: Position? = null,

    @XmlAttribute(name = "end-mode")
    var endMode: String? = null,

    @XmlAttribute(name = "out-name")
    var outName: String? = null,

    displayName: String? = null
) {

    @XmlElement(name = "script")
    private var script: WorkflowScript? = null

    var body: String?
        get() = script?.value
        set(value) {
            script = WorkflowScript(value = value, encoded = false)
        }

    @XmlElement(name = "display-name")
    var displayName: String? = displayName.CDATA
        set(value) {
            field = value.CDATA
        }

    @XmlElement(name = "in-binding")
    var inBinding: Binding? = null

    @XmlElement(name = "out-binding")
    var outBinding: Binding? = null

    fun inBinding(name: String, type: ParameterType, exportName: String = name) {
        inBinding = inBinding.safeBind(name, type, exportName)
    }

    fun outBinding(name: String, type: ParameterType, exportName: String = name) {
        outBinding = outBinding.safeBind(name, type, exportName)
    }

    private fun Binding?.safeBind(name: String, type: ParameterType, exportName: String): Binding =
        (this ?: Binding()).apply { bind(name, type, exportName) }
}
