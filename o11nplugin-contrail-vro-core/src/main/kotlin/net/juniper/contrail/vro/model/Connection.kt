/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import com.vmware.o11n.sdk.modeldriven.Findable
import com.vmware.o11n.sdk.modeldriven.Sid
import net.juniper.contrail.api.ApiConnector
import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.api.ObjectReference
import java.io.IOException

class Connection(public val info: ConnectionInfo, val connector: ApiConnector) : Findable {
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

    @Throws(IOException::class)
    fun create(obj: ApiObjectBase): Boolean {
        return connector.create(obj)
    }

    @Throws(IOException::class)
    fun read(obj: ApiObjectBase): Boolean {
        return connector.read(obj)
    }

    @Throws(IOException::class)
    fun update(obj: ApiObjectBase): Boolean {
        return connector.update(obj)
    }

    @Throws(IOException::class)
    fun delete(obj: ApiObjectBase) {
        connector.delete(obj)
    }

    @Throws(IOException::class)
    fun delete(clazz: Class<out ApiObjectBase>, objectId: String) {
        connector.delete(clazz, objectId)
    }

    @Throws(IOException::class)
    fun findByName(clazz: Class<out ApiObjectBase>, parent: ApiObjectBase, name: String): String? {
        return connector.findByName(clazz, parent, name)
    }

    @Throws(IOException::class)
    fun findByName(clazz: Class<out ApiObjectBase>, ancestorNames: List<String>): String? {
        return connector.findByName(clazz, ancestorNames)
    }

    @Throws(IOException::class)
    fun find(clazz: Class<out ApiObjectBase>, parent: ApiObjectBase, name: String): ApiObjectBase? {
        return connector.find(clazz, parent, name)
    }

    @Throws(IOException::class)
    fun findById(clazz: Class<out ApiObjectBase>, objectId: String): ApiObjectBase? {
        return connector.findById(clazz, objectId)
    }

    @Throws(IOException::class)
    fun findByFQN(clazz: Class<out ApiObjectBase>, fqn: String): ApiObjectBase? {
        return connector.findByFQN(clazz, fqn)
    }

    @Throws(IOException::class)
    fun list(clazz: Class<out ApiObjectBase>): List<ApiObjectBase>? {
        return connector.list(clazz, null)
    }

    @Throws(IOException::class)
    fun <A : ApiPropertyBase> getObjects(
        clazz: Class<out ApiObjectBase>,
        references: List<ObjectReference<A>>
    ): List<ApiObjectBase>? {
        return connector.getObjects(clazz, references)
    }

    @Throws(IOException::class)
    fun sync(uri: String): Boolean {
        return connector.sync(uri)
    }
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