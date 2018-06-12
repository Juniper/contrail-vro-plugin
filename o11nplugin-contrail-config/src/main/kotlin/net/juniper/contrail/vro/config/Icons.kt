/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import net.juniper.contrail.api.types.PolicyManagement
import net.juniper.contrail.api.types.QuotaType

val defaultFolderIconName = "folder.png"
val defaultItemIconName = "item.png"

inline fun <reified T> iconName() =
    T::class.java.simpleName.classToTypeName + ".png"

inline fun <reified T> folderIconName() =
    iconName<T>()

inline fun <reified T> itemIconName() = when (T::class.java) {
    PolicyManagement::class.java,
    QuotaType::class.java -> iconName<T>()
    else -> defaultItemIconName
}