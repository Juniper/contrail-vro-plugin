/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import net.juniper.contrail.vro.config.CDATA
import net.juniper.contrail.vro.config.withoutCDATA
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlType

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "presentationType",
    propOrder = ["description", "presentationParameters", "presentationSteps"]
)
class Presentation(
    presentationParameters: List<PresentationParameter> = emptyList(),
    presentationSteps: List<PresentationStep> = emptyList(),
    description: String? = null
) {
    @XmlElement(name = "desc")
    val description: String? = description.CDATA

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
    propOrder = ["title", "description", "qualifiers", "presentationParameters", "presentationGroups"]
)
class PresentationStep private constructor(
    title: String,
    presentationParameters: List<PresentationParameter>?,
    presentationGroups: List<PresentationGroup>?,
    description: String?,
    qualifiers: List<ParameterQualifier>?
) {
    companion object {
        fun fromParameters(
            title: String,
            presentationParameters: List<PresentationParameter>,
            description: String? = null,
            qualifiers: List<ParameterQualifier>?
        ) = PresentationStep(title, presentationParameters, null, description, qualifiers)

        fun fromGroups(
            title: String,
            presentationGroups: List<PresentationGroup>,
            description: String? = null,
            qualifiers: List<ParameterQualifier>?
        ) = PresentationStep(title, null, presentationGroups, description, qualifiers)
    }

    @XmlElement
    val title: String = title

    @XmlElement(name = "desc")
    val description: String? = description.CDATA

    @XmlElement(name = "p-qual")
    val qualifiers: List<ParameterQualifier> =
            qualifiers?.toMutableList() ?: mutableListOf()

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
    propOrder = ["title", "description", "qualifiers", "presentationParameters"]
)
class PresentationGroup(
    title: String,
    presentationParameters: List<PresentationParameter>,
    description: String?,
    qualifiers: List<ParameterQualifier>? = null
) {
    @XmlElement
    val title: String = title

    @XmlElement(name = "desc")
    val description: String? = description.CDATA

    @XmlElement(name = "p-qual")
    val qualifiers: List<ParameterQualifier> =
        qualifiers?.toMutableList() ?: mutableListOf()

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

fun Presentation.filterNot(names: List<String>): Presentation =
    Presentation(presentationParameters.filterNot { names.contains(it.name) }, presentationSteps.map { it.filterNot(names) }, description?.withoutCDATA)

fun PresentationStep.filterNot(names: List<String>): PresentationStep =
    if (presentationParameters == null) {
        PresentationStep.fromGroups(title, presentationGroups?.map { it.filterNot(names) } ?: listOf(), description?.withoutCDATA, qualifiers)
    } else {
        PresentationStep.fromParameters(title, presentationParameters.filterNot { names.contains(it.name) }, description?.withoutCDATA, qualifiers)
    }

fun PresentationGroup.filterNot(names: List<String>): PresentationGroup =
    PresentationGroup(title, presentationParameters.filterNot { names.contains(it.name) }, description?.withoutCDATA, qualifiers)

