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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException

@Suppress("UNCHECKED_CAST")
class Connection(public val info: ConnectionInfo, val connector: ApiConnector) : Findable {
    private val log: Logger = LoggerFactory.getLogger(Connection::class.java)

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

    @Throws(IOException::class, ConnectionException::class)
    fun create(obj: ApiObjectBase) =
        asserted("Create") { connector.create(obj) }

    @Throws(IOException::class, ConnectionException::class)
    fun read(obj: ApiObjectBase) =
        asserted("Read") { connector.read(obj) }

    @Throws(IOException::class, ConnectionException::class)
    fun update(obj: ApiObjectBase) =
        asserted("Update") { connector.update(obj) }

    @Throws(IOException::class, ConnectionException::class)
    fun sync(uri: String) =
        asserted("Sync") { connector.sync(uri) }

    @Throws(IOException::class)
    fun delete(obj: ApiObjectBase) =
        connector.delete(obj)

    @Throws(IOException::class)
    fun delete(clazz: Class<out ApiObjectBase>, objectId: String) =
        connector.delete(clazz, objectId)

    fun findByName(clazz: Class<out ApiObjectBase>, parent: ApiObjectBase, name: String): String? =
        safe { connector.findByName(clazz, parent, name) }

    fun findByName(clazz: Class<out ApiObjectBase>, ancestorNames: List<String>): String? =
        safe { connector.findByName(clazz, ancestorNames) }

    fun <T : ApiObjectBase> find(clazz: Class<T>, parent: ApiObjectBase, name: String): T? =
        safe { connector.find(clazz, parent, name) as T? }

    fun <T : ApiObjectBase> findById(clazz: Class<T>, objectId: String): T? =
        safe { connector.findById(clazz, objectId) as T? }

    inline fun <reified T : ApiObjectBase> findById(objectId: String): T? =
        findById(T::class.java, objectId)

    inline fun <reified T : ApiObjectBase> find(id: Sid): T? =
        findById(T::class.java, id.getString(T::class.java.simpleName))

    fun <T : ApiObjectBase> findByFQN(clazz: Class<T>, fqn: String): T? =
        safe { connector.findByFQN(clazz, fqn) as T? }

    fun <T : ApiObjectBase> list(clazz: Class<T>): List<T>? =
        safe { connector.list(clazz, null) as List<T>? }

    fun <T : ApiObjectBase, U : ApiPropertyBase> getObjects(
        clazz: Class<T>,
        references: List<ObjectReference<U>>?
    ): List<T>? = safe {
        connector.getObjects(clazz, references ?: return@safe null) as List<T>?
    }

    private fun <T> safe(unsafeOperation: () -> T?): T? {
        return try {
            unsafeOperation()
        } catch (e: IOException) {
            e.log()
            null
        }
    }

    @Throws(ConnectionException::class)
    private fun asserted(operationName: String, operation: () -> Boolean) {
        val success = operation()
        if (!success) {
            throw ConnectionException("$operationName operation failed.")
        }
    }

    private fun IOException.log() =
        log.error("IO error in Contrail API: {}", message)
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

class ConnectionException(override var message: String) : Exception()