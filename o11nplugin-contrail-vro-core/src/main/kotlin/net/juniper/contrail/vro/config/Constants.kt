/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

val ID = "uuid"
val HOST = "host"
val PORT = "port"
val USER = "user"
val PASSWORD = "password"
val TENANT = "tenant"
val AUTHTYPE = "keystone"
val AUTHSERVER = "authserver"

val ROOT = "root"
val CONNECTION = "connection"
val CONNECTION_MANAGER = "connection-manager"

infix fun String.has(child: String) =
    "$this-to-$child"

val ROOT_HAS_CONNECTIONS =
    ROOT has CONNECTION