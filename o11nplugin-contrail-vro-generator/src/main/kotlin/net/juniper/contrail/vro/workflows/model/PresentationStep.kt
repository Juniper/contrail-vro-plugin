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
    name = "p-stepType",
    propOrder = ["title", "presentationParameters"]
)
class PresentationStep (
    @XmlElement
    val title: String? = null
) {
    @XmlElement(name = "p-param")
    private val presentationParameters: MutableList<PresentationParameter> = mutableListOf()

    fun parameter(name: String, description: String, setup: PresentationParameter.() -> Unit) {
        val parameter = PresentationParameter(name, description)
        parameter.setup()
        presentationParameters.add(parameter)
    }
}
