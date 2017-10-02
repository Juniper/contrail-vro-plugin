/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

fun startBuildingRelationTreeNode(objectName: String) = RelationTreeNode.Builder(objectName)

enum class Cardinality {
    ONE_TO_ONE,
    ONE_TO_MANY;

    val isOneToOne get() =
        ONE_TO_ONE == this
    val isOneToMany get() =
        ONE_TO_MANY == this
}

data class RelationInfo(val cardinality: Cardinality, val childNode: RelationTreeNode)

class Relation(val name: String, val childTypeName: String, val cardinality: Cardinality)

class RelationTreeNode private constructor(private val objectName: String, private val children: List<RelationInfo>) {
    fun getRelations(): List<Relation> {
        return children.map { child ->
            val relationName = generateRelationName(objectName, child.childNode.objectName, child.cardinality)
            Relation(relationName, child.childNode.objectName, child.cardinality)
        }
    }

    private fun generateRelationName(
        parentObject: String,
        childObject: String,
        cardinality: Cardinality
    ): String {
        val suffix = if (cardinality.isOneToMany) "s" else ""
        return "$parentObject-has-$childObject$suffix"
    }

    class Builder(private val objectName: String) {
        private val relationsInfo = mutableListOf<RelationInfo>()

        fun addNode(r: RelationInfo) {
            relationsInfo += r
        }

        fun build() =
            RelationTreeNode(objectName, relationsInfo.toList())
    }
}