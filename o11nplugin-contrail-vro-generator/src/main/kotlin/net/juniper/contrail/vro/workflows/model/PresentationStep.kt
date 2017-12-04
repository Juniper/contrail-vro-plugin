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
    propOrder = arrayOf("title", "presentationParameters")
)
class PresentationStep {

    @XmlElement(required = true)
    var title: String? = null

    @XmlElement(name = "p-param")
    var presentationParameters: MutableList<PresentationParameter> = mutableListOf()

}
