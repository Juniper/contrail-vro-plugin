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

@Suppress("UNCHECKED_CAST")
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

    val displayName: String get() =
        "$name (${info.username ?: "anonymous"}@$host:$port)"

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
    fun findByName(clazz: Class<out ApiObjectBase>, parent: ApiObjectBase, name: String): String? =
        connector.findByName(clazz, parent, name)

    @Throws(IOException::class)
    fun findByName(clazz: Class<out ApiObjectBase>, ancestorNames: List<String>): String? =
        connector.findByName(clazz, ancestorNames)

    @Throws(IOException::class)
    fun <T : ApiObjectBase> find(clazz: Class<T>, parent: ApiObjectBase, name: String): T? =
        connector.find(clazz, parent, name) as T?

    @Throws(IOException::class)
    fun <T : ApiObjectBase> findById(clazz: Class<T>, objectId: String): T? =
        connector.findById(clazz, objectId) as T?

    @Throws(IOException::class)
    fun <T : ApiObjectBase> findByFQN(clazz: Class<T>, fqn: String): T? =
        connector.findByFQN(clazz, fqn) as T?

    @Throws(IOException::class)
    fun <T : ApiObjectBase> list(clazz: Class<T>): List<T>? =
        connector.list(clazz, null) as List<T>?

    @Throws(IOException::class)
    fun sync(uri: String): Boolean =
        connector.sync(uri)

    @Throws(IOException::class)
    fun <T : ApiObjectBase, U : ApiPropertyBase> getObjects(
        clazz: Class<T>,
        references: List<ObjectReference<U>>?
    ): List<T>? {

        return connector.getObjects(clazz, references ?: return null) as List<T>?
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