/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

val NAME = "name"
val HOST = "host"
val PORT = "port"
val USER = "user"
val PASSWORD = "password"
val TENANT = "tenant"
val AUTHTYPE = "keystone"
val AUTHSERVER = "authserver"

val ROOT = "Root"
val CONNECTION = "Connection"
val PROJECT = "Project"
val VIRTUAL_NETWORK = "VirtualNetwork"
val NETWORK_POLICY = "NetworkPolicy"

infix fun String.has(child: String) =
    "$this-to-$child".toLowerCase()

val ROOT_HAS_CONNECTIONS =
    ROOT has CONNECTION

val CONNECTION_HAS_PROJECTS =
    CONNECTION has PROJECT

val PROJECT_HAS_VIRTUAL_NETWORKS =
    PROJECT has VIRTUAL_NETWORK

val PROJECT_HAS_NETWORK_POLICYS =
    PROJECT has NETWORK_POLICY