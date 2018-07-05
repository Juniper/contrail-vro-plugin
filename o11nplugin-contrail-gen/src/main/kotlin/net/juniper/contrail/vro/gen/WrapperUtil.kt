/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.gen

import ch.dunes.vso.sdk.api.IPluginFactory
import com.vmware.o11n.sdk.modeldriven.Findable
import com.vmware.o11n.sdk.modeldriven.ModelWrapper
import com.vmware.o11n.sdk.modeldriven.Sid
import com.vmware.o11n.sdk.modeldriven.WrapperContext
import net.juniper.contrail.api.ApiObjectBase
import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.api.ObjectReference
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.vro.config.pluginName
import net.juniper.contrail.vro.model.Executor
import net.juniper.contrail.vro.config.constants.Connection as ConnectionName

class WrapperUtil(val ctx: WrapperContext, val factory: IPluginFactory) {

    private fun <M> defaultList(): List<M> =
        mutableListOf()

    fun findConnectionWrapper(id: Sid): ModelWrapper? =
        factory.find(ConnectionName, id.toString()) as ModelWrapper?

    private fun maybeFindConnection(sid: Sid): Connection? =
        findConnectionWrapper(sid)?.__getTarget() as Connection?

    private fun findConnection(sid: Sid): Connection =
        maybeFindConnection(sid) ?: raiseNoConnection(sid)

    private fun raiseNoConnection(sid: Sid): Nothing =
        throw IllegalStateException("Did not find connection with id: ${sid.id}")

    fun executor(sid: Sid) =
        Executor(findConnection(sid))

    private inline fun <T : ApiObjectBase> crud(obj: T, sid: Sid, operation: Connection.(T) -> Unit) =
        findConnection(sid).operation(obj)

    fun <T : ApiObjectBase> create(sid: Sid, obj: T) =
        crud(obj, sid) { create(it) }

    fun <T : ApiObjectBase> update(sid: Sid, obj: T) =
        crud(obj, sid) { update(it) }

    fun <T : ApiObjectBase> delete(sid: Sid, obj: T) =
        crud(obj, sid) { delete(it) }

    fun <T : ApiObjectBase> read(sid: Sid, obj: T) =
        crud(obj, sid) { read(it) }

    fun <T : ApiObjectBase, U : ApiPropertyBase, M: Findable>
        references(sid: Sid?, clazz: Class<T>, references: List<ObjectReference<U>>?): List<M> {
        if (sid == null || references == null) return defaultList()
        return maybeFindConnection(sid)?.getObjects(clazz, references)?.map { it.toWrapper<T, M>(sid, clazz) }
            ?: defaultList()
    }

    fun <T : ApiObjectBase, M: Findable> find(connection: Connection, clazz: Class<T>, id: String): M? =
        connection.findById(clazz, id)?.also { connection.read(it) }?.toWrapper(connection.id, clazz)

    fun <T : ApiObjectBase, M: Findable> findByFQName(connection: Connection, clazz: Class<T>, fqName: String): M? =
        connection.findByFQN(clazz, fqName)?.also { connection.read(it) }?.toWrapper(connection.id, clazz)

    private fun <T : ApiObjectBase, M : Findable> T.toWrapper(sid: Sid, clazz: Class<T>): M {
        val wrapper: M = ctx.createPluginObject(this, clazz)
        wrapper.internalId = sid.with(clazz.pluginName, uuid)
        return wrapper
    }
}