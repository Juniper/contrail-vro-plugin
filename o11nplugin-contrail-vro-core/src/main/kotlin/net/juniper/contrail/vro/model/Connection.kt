/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import net.juniper.contrail.api.ApiConnector
import java.io.Serializable

class Connection(val info: ConnectionInfo, val connector: ApiConnector) {

    val name: String
        get() =
            info.name

    val host: String get() =
        info.hostname

    val port: Int get() =
        info.port
}

data class ConnectionInfo(
    val name: String,
    val hostname: String,
    val port: Int,
    val username: String?,
    val password: String?,
    val authServer: String?,
    val tenant: String?
) : Serializable {

    constructor(name: String, hostname: String, port: Int, username: String, password: String) :
        this(name, hostname, port, username, password, null, null)

    override fun toString(): String =
        "$username@$hostname:$port"
}