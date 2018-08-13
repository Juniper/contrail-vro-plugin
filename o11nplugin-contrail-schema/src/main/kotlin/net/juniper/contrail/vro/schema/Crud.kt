/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.schema

val defaultCrud = CRUD("CRUD")

class CRUD(val crud: String) {
    val isCreate = crud.contains('C')
    val isRead = crud.contains('R')
    val isUpdate = crud.contains('U')
    val isDelete = crud.contains('D')

    val isCreateOnly = isCreate && !isUpdate

    val isReadOnly = isRead && !isCreate && !isUpdate
}

fun String?.toCrud() =
    if (this == null) defaultCrud else CRUD(this)