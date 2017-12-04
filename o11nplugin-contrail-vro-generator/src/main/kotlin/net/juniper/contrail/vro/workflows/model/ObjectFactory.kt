/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import javax.xml.bind.JAXBElement
import javax.xml.bind.annotation.XmlElementDecl
import javax.xml.bind.annotation.XmlRegistry
import javax.xml.namespace.QName

@XmlRegistry
class ObjectFactory {

    fun createWorkflowType(): Workflow {
        return Workflow()
    }

    fun createPQualType(): ParameterQualifier {
        return ParameterQualifier()
    }

    fun createPositionType(): Position {
        return Position()
    }

    fun createPParamType(): PresentationParameter {
        return PresentationParameter()
    }

    fun createBindType(): Bind {
        return Bind()
    }

    fun createOutputType(): Output {
        return Output()
    }

    fun createInBindingType(): InBinding {
        return InBinding()
    }

    fun createParamType(): Parameter {
        return Parameter()
    }

    fun createWorkflowItemType(): WorkflowItem {
        return WorkflowItem()
    }

    fun createScriptType(): WorkflowScript {
        return WorkflowScript()
    }

    fun createOutBindingType(): OutBinding {
        return OutBinding()
    }

    fun createInputType(): Input {
        return Input()
    }

    fun createPStepType(): PresentationStep {
        return PresentationStep()
    }

    fun createPresentationType(): Presentation {
        return Presentation()
    }

    @XmlElementDecl(name = "workflow")
    fun createWorkflow(value: Workflow): JAXBElement<Workflow> {
        return JAXBElement(_Workflow_QNAME, Workflow::class.java, null, value)
    }

    companion object {
        private val _Workflow_QNAME = QName("http://vmware.com/vco/workflow", "workflow")
    }

}
