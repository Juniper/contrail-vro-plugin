/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator.model

import net.juniper.contrail.api.types.PolicyManagement
import net.juniper.contrail.api.types.Project
import net.juniper.contrail.vro.config.CategoryType
import net.juniper.contrail.vro.config.category
import net.juniper.contrail.vro.config.pluginName
import net.juniper.contrail.vro.config.folderName

data class Category(val type: CategoryType, val parent: Class<*>) {
    val name = type.name
    val parentName = parent.simpleName
    val parentPluginName = parent.pluginName
}

class CategoryRelation(val type: CategoryType, parent: Class<*>, child: Class<*>) {
    val categoryName = type.name
    val parentName = parent.simpleName
    val parentPluginName = parent.pluginName
    val childName = child.simpleName
    val childPluginName = child.pluginName
    val folderName = child.folderName
}

fun Relation.toCategory(): Category? =
    childClass.category?.let { Category(it, parentClass) }

fun Relation.toCategoryRelation(): CategoryRelation? =
    childClass.category?.let { CategoryRelation(it, parentClass, childClass) }

val Relation.isCategoryRelation get() =
    parentClass == Project::class.java && childClass.category != null

fun List<Relation>.toCategories() = asSequence()
    .filter { it.isCategoryRelation }
    .mapNotNull { it.toCategory() }
    .distinct()

fun List<Relation>.toCategoryRelations() = asSequence()
    .filter { it.isCategoryRelation }
    .mapNotNull { it.toCategoryRelation() }

fun List<Relation>.toSecurityClasses() = asSequence()
    .filter { it.parentClass == PolicyManagement::class.java }
    .map { it.childClass.toClassInfoModel() }

