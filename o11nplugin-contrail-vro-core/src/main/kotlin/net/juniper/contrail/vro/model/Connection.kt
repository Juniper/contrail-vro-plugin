/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import com.vmware.o11n.sdk.modeldriven.Findable
import com.vmware.o11n.sdk.modeldriven.Sid
import net.juniper.contrail.api.ApiConnector

class Connection(val info: ConnectionInfo, val connector: ApiConnector) : Findable {
    override fun getInternalId(): Sid =
        info.sid

    // ignored since id is fixed and provided by info
    override fun setInternalId(id: Sid?) = Unit

    val name: String get() =
        info.name

    val host: String get() =
        info.hostname

    val port: Int get() =
        info.port
}

data class ConnectionInfo @JvmOverloads constructor(
    val name: String,
    val hostname: String,
    val port: Int,
    val username: String? = null,
    val password: String? = null,
    val authServer: String? = null,
    val tenant: String? = null
) {
    val sid: Sid = Sid.valueOf(name)

    override fun toString(): String =
        "$username@$hostname:$port"
}