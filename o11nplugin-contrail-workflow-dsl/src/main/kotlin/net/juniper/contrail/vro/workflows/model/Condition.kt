/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.model

import net.juniper.contrail.vro.workflows.dsl.workflowEndItemId
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlType
import javax.xml.bind.annotation.XmlValue

val equalsComparator = "0"
val defaultComparator = "6"

abstract class ConditionDefinition (
    val name: String,
    val type: ParameterType<Any>,
    val comparator: String,
    val label: String,
    val value: String
) {
    abstract fun toCondition(): Condition
    abstract fun withNewTargetId(newId: String): ConditionDefinition
    fun isConnectedToEnd() = label == workflowEndItemId.toFullItemId
}

val List<ConditionDefinition>.toConditions get() =
    map { it.toCondition() }

val List<ConditionDefinition>.existsConnectedToEnd get() =
    any { it.isConnectedToEnd() }

class EqualsConditionDefinition(val _name: String, val _value: String, val _type: ParameterType<Any>, val _targetId: String) : ConditionDefinition(
    _name,
    _type,
    equalsComparator,
    _targetId,
    _value
) {
    override fun toCondition() = EqualsCondition(
        name,
        value,
        type,
        label
    )

    override fun withNewTargetId(newId: String) = EqualsConditionDefinition(
        _name,
        _value,
        _type,
        newId
    )
}

class DefaultConditionDefinition(val _targetId: String) : ConditionDefinition(
    "",
    boolean,
    defaultComparator,
    _targetId,
    ""
) {
    override fun toCondition() = DefaultCondition(
        _targetId
    )

    override fun withNewTargetId(newId: String) = DefaultConditionDefinition(
        newId
    )
}

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "conditionType",
    propOrder = ["value"]
)
open class Condition (
    name: String,
    type: ParameterType<Any>,
    comparator: String,
    label: String,
    value: String
) {
    @XmlAttribute(name = "name")
    val name: String = name

    @XmlAttribute(name = "type")
    val type: String = type.name

    @XmlAttribute(name = "comparator")
    val comparator: String = comparator

    @XmlAttribute(name = "label")
    val label: String = label

    @XmlValue
    val value: String = value
}

class EqualsCondition(name: String, value: String, type: ParameterType<Any>, targetId: String) : Condition(
    name,
    type,
    equalsComparator,
    targetId,
    value
)

class DefaultCondition(targetId: String) : Condition(
    "",
    boolean,
    defaultComparator,
    targetId,
    ""
)

fun generateSwitchScript(conditions: List<Condition>): Script =
    Script(conditions.joinToString(" else ") { it.toJS() })

fun Condition.toJS(): String {
    val conditionValue: String = when (type) {
        string.name -> "\"$value\""
        else -> value
    }
    val conditionLabel: String = "\"$label\""
    return when (this) {
        is EqualsCondition ->
            """if ($name == $conditionValue) {
                |    return $conditionLabel;
                |}
            """.trimMargin()
        is DefaultCondition ->
            """if (true) {
                |    return $conditionLabel;
                |}
            """.trimMargin()
        else -> ""
    }
}