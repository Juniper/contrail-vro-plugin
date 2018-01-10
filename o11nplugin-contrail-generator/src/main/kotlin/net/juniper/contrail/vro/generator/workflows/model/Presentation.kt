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
    name = "presentationType",
    propOrder = ["presentationParameters", "presentationSteps"]
)
class Presentation(
    presentationParameters: List<PresentationParameter> = emptyList(),
    presentationSteps: List<PresentationStep> = emptyList()
) {

    @XmlElement(name = "p-param")
    val presentationParameters: List<PresentationParameter> =
        presentationParameters.toList()

    @XmlElement(name = "p-step")
    val presentationSteps: List<PresentationStep> =
        presentationSteps.toList()
}

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "p-stepType",
    propOrder = ["title", "description", "presentationParameters", "presentationGroups"]
)
class PresentationStep private constructor(
    title: String,
    presentationParameters: List<PresentationParameter>?,
    presentationGroups: List<PresentationGroup>?,
    description: String?
) {
    companion object {
        fun fromParameters(
            title: String,
            presentationParameters: List<PresentationParameter>,
            description: String? = null
        ) = PresentationStep(title, presentationParameters, null, description)

        fun fromGroups(
            title: String,
            presentationGroups: List<PresentationGroup>,
            description: String? = null
        ) = PresentationStep(title, null, presentationGroups, description)
    }

    @XmlElement
    val title: String = title

    @XmlElement(name = "desc")
    val description: String? = description

    @XmlElement(name = "p-param")
    val presentationParameters: List<PresentationParameter>? =
        presentationParameters?.toList()

    @XmlElement(name = "p-group")
    val presentationGroups: List<PresentationGroup>? =
        presentationGroups?.toList()
}

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "p-groupType",
    propOrder = ["title", "description", "presentationParameters"]
)
class PresentationGroup(
    title: String,
    presentationParameters: List<PresentationParameter>,
    description: String?
) {
    @XmlElement
    val title: String = title

    @XmlElement(name = "desc")
    val description: String? = description

    @XmlElement(name = "p-param")
    val presentationParameters: List<PresentationParameter> =
        presentationParameters.toList()
}

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "p-paramType",
    propOrder = ["description", "parameterQualifiers"]
)
class PresentationParameter(
    name: String,
    description: String? = null,
    qualifiers: List<ParameterQualifier>? = null
) {
    @XmlAttribute(name = "name")
    val name: String = name

    @XmlElement(name = "desc", required = true)
    val description: String? = description.CDATA ?: name

    @XmlElement(name = "p-qual")
    val parameterQualifiers: List<ParameterQualifier> =
        qualifiers?.toMutableList() ?: mutableListOf()
}

