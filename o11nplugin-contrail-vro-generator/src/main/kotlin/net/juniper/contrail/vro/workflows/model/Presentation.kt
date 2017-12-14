/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "presentationType",
    propOrder = ["presentationSteps"]
)
class Presentation {
    @XmlElement(name = "p-step")
    private val presentationSteps: MutableList<PresentationStep> = mutableListOf()

    fun step(title: String? = null, setup: PresentationStep.() -> Unit) {
        val step = PresentationStep(title)
        step.setup()
        presentationSteps.add(step)
    }
}
