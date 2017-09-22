/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import net.juniper.contrail.api.ApiConnector
import java.io.Serializable
import java.util.*

class Connection(val info: ConnectionInfo, val connector: ApiConnector) {

    val id: String
        get() = info.id.toString()
}


data class ConnectionInfo(val id: UUID, val hostname: String, val port: Int, val username: String, val password: String) : Serializable {

    constructor(hostname: String, port: Int, username: String, password: String):
        this(UUID.randomUUID(), hostname, port, username, password)

    override fun toString(): String =
        "$username@$hostname:$port"
}
