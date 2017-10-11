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

    val host: String get() =
        info.hostname

    val port: Int get() =
        info.port
}

data class ConnectionInfo(val uuid: UUID, val hostname: String, val port: Int, val username: String?, val password: String?,
                          val authServer: String?, val tenant: String?) : Serializable {
    @JvmOverloads constructor(hostname: String, port: Int, username: String, password: String, authServer: String? = null, tenant: String? = null) :
        this(UUID.randomUUID(), hostname, port, username, password, authServer, tenant)

    val id: String get() =
        uuid.toString()

    override fun toString(): String =
        "$username@$hostname:$port"
}