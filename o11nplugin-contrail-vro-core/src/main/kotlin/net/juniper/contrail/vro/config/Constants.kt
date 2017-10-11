/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

const val HOST = "host"
const val PORT = "port"
const val USER = "user"
const val PASSWORD = "password"
const val TENANT = "tenant"
const val AUTHTYPE = "keystone"
const val AUTHSERVER = "authserver"
const val NAME = "name"

const val ROOT = "Root"
const val CONNECTION = "Connection"

infix fun String.has(child: String) =
    "$this-to-$child".toLowerCase()

val ROOT_HAS_CONNECTIONS =
    ROOT has CONNECTION