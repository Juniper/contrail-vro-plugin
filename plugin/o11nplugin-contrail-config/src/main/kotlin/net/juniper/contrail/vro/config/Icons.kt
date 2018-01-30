/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

val defaultFolderIconName = "folder.png"
val defaultItemIconName = "item.png"

inline fun <reified T> folderIconName() =
    T::class.java.simpleName.classToTypeName+".png"

inline fun <reified T> itemIconName() =
    defaultItemIconName