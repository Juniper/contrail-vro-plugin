/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.schema

sealed class Constraint

object Required : Constraint()

class DefaultValue<out T : Any>(val value: T) : Constraint()

class Enumeration(elements: List<String>) : Constraint() {
    val elements = elements.toList()
}

class MinValue(val value: Long) : Constraint()
class MaxValue(val value: Long) : Constraint()

class MinLength(val value: Int) : Constraint()
class MaxLength(val value: Int) : Constraint()

class Regexp(val regexp: String) : Constraint()