/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.workflows.xsd

class IdlCommentsFactory {
    fun buildFromComment(comment: String): List<IdlComment> =
        comment.split(";").mapNotNull { buildCommentObject(it) }

    private fun buildCommentObject(comment: String): IdlComment? =
        when (extractType(comment)) {
            "Link" -> Link(comment)
            "Property" -> Property(comment)
            else -> null
        }
}