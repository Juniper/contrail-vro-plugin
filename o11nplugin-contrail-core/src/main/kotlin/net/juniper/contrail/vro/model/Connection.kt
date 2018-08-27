/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.model

import com.google.common.cache.CacheBuilder
import com.google.common.cache.Cache
import com.vmware.o11n.sdk.modeldriven.Sid
import net.juniper.contrail.api.ApiConnector
import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.api.ObjectReference
import net.juniper.contrail.api.Status
import net.juniper.contrail.vro.base.Description
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

private typealias Key = Pair<Class<out ApiObjectBase>, String>
private typealias Value = ApiObjectBase
private typealias ObjectCache = Cache<Key, Value>

private val ApiObjectBase.key: Key? get() =
    uuid?.let { Key(javaClass, it) }

private fun ObjectCache.add(obj: ApiObjectBase) =
    obj.key?.let { put(it, obj) }

private fun ObjectCache.remove(obj: ApiObjectBase) =
    obj.key?.let { invalidate(it) }

private fun ObjectCache.remove(clazz: Class<out ApiObjectBase>, id: String?) =
    id?.let { invalidate(Key(clazz, it)) }

@Suppress("UNCHECKED_CAST")
@Description("Object representing an instance of Contrail controller.")
class Connection(val info: ConnectionInfo, val connector: ApiConnector) {
    private val log: Logger = LoggerFactory.getLogger(Connection::class.java)

    val id: Sid get() =
        info.sid

    val name: String get() =
        info.name

    val host: String get() =
        info.hostname

    val port: Int get() =
        info.port

    val displayName: String get() =
        "$name (${info.username ?: "anonymous"}@$host:$port)"

    private val cache: ObjectCache = CacheBuilder.newBuilder()
        .maximumSize(100)
        .expireAfterWrite(2, TimeUnit.SECONDS)
        .build()

    @Throws(IOException::class, ConnectionException::class)
    fun commitDrafts(obj: ApiObjectBase) =
        connector.commitDrafts(obj).checkStatus("Commit Drafts").apply { cache.add(obj) }

    @Throws(IOException::class, ConnectionException::class)
    fun discardDrafts(obj: ApiObjectBase) =
        connector.discardDrafts(obj).checkStatus("Discard Drafts").apply { cache.add(obj) }

    @Throws(IOException::class, ConnectionException::class)
    fun create(obj: ApiObjectBase) =
        connector.create(obj).checkStatus("Create").apply { cache.add(obj) }

    @Throws(IOException::class, ConnectionException::class)
    fun read(obj: ApiObjectBase) =
        connector.read(obj).checkStatus("Read").apply { cache.add(obj) }

    @Throws(IOException::class, ConnectionException::class)
    fun update(obj: ApiObjectBase) =
        connector.update(obj).checkStatus("Update").apply { cache.add(obj) }

    @Throws(IOException::class, ConnectionException::class)
    fun sync(uri: String) =
        connector.sync(uri).checkStatus("Sync")

    @Throws(IOException::class)
    fun delete(obj: ApiObjectBase) =
        connector.delete(obj).checkStatus("Delete").apply { cache.remove(obj) }

    @Throws(IOException::class)
    fun delete(clazz: Class<out ApiObjectBase>, objectId: String) =
        connector.delete(clazz, objectId).checkStatus("Delete").apply { cache.remove(clazz, objectId) }

    fun findByName(clazz: Class<out ApiObjectBase>, parent: ApiObjectBase, name: String): String? =
        safe { connector.findByName(clazz, parent, name) }

    inline fun <reified T : ApiObjectBase> findByName(parent: ApiObjectBase, name: String): String? =
        findByName(T::class.java, parent, name)

    fun findByName(clazz: Class<out ApiObjectBase>, ancestorNames: List<String>): String? =
        safe { connector.findByName(clazz, ancestorNames) }

    fun <T : ApiObjectBase> find(clazz: Class<T>, parent: ApiObjectBase, name: String): T? =
        safe { connector.find(clazz, parent, name) as T? }?.also { cache.add(it) }

    fun <T : ApiObjectBase> findById(clazz: Class<T>, objectId: String): T? =
        cache.getIfPresent(Key(clazz, objectId)) as T? ?:
        safe { connector.findById(clazz, objectId) as T? }?.also { cache.add(it) }

    inline fun <reified T : ApiObjectBase> findById(objectId: String): T? =
        findById(T::class.java, objectId)

    inline fun <reified T : ApiObjectBase> find(id: Sid): T? =
        findById(T::class.java, id.getString(T::class.java.simpleName))

    fun <T : ApiObjectBase> findByFQN(clazz: Class<T>, fqn: String): T? =
        safe { connector.findByFQN(clazz, fqn) as T? }?.also { cache.add(it) }

    inline fun <reified T : ApiObjectBase> findByFQN(fqn: String): T? =
        findByFQN(T::class.java, fqn)

    fun <T : ApiObjectBase> list(clazz: Class<T>): List<T>? =
        safe { connector.list(clazz, null) as List<T>? }

    inline fun <reified T : ApiObjectBase> list(): List<T>? =
        list(T::class.java)

    fun <T : ApiObjectBase, U : ApiPropertyBase> getObjects(
        clazz: Class<T>,
        references: List<ObjectReference<U>>?
    ): List<T>? = safe {
        connector.getObjects(clazz, references ?: return@safe null) as List<T>?
    }

    fun dispose() =
        connector.dispose()

    private fun <T> safe(unsafeOperation: () -> T?): T? {
        return try {
            unsafeOperation()
        } catch (e: IOException) {
            e.log()
            null
        }
    }

    @Throws(ConnectionException::class)
    private fun Status.checkStatus(operation: String): Unit =
        ifFailure { throw ConnectionException("$operation failed: $it") }

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