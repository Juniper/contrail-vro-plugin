/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import net.juniper.contrail.api.ApiConnector
import java.io.Serializable
import java.util.UUID

class Connection(val info: ConnectionInfo, val connector: ApiConnector) {

    val id: String get() =
        info.id
}

data class ConnectionInfo(val uuid: UUID, val hostname: String, val port: Int, val username: String?, val password: String?,
                          val tenant: String?, val authServer: String?) : Serializable {
    constructor(hostname: String, port: Int, username: String, password: String):
        this(UUID.randomUUID(), hostname, port, username, password, null, null)

    val id: String get() =
        uuid.toString()

    override fun toString(): String =
        "$username@$hostname:$port"
}