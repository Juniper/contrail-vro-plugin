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
import net.juniper.contrail.vro.config.constants.Connection as ConnectionName

class WrapperUtil(val ctx: WrapperContext, val factory: IPluginFactory) {

    private fun <M> defaultList(): List<M> =
        mutableListOf()

    fun <T : ApiObjectBase, U : ApiPropertyBase, M: Findable>
        references(sid: Sid?, clazz: Class<T>, references: List<ObjectReference<U>>?): List<M> {
        if (sid == null || references == null) return defaultList()
        val connectionWrapper = factory.find(ConnectionName, sid.toString()) as ModelWrapper? ?: return defaultList()
        val connection: Connection = connectionWrapper.__getTarget() as Connection
        val objects: List<T> = connection.getObjects(clazz, references) ?: return defaultList()
        return objects.map { it.toWrapper<T, M>(clazz, sid) }
    }

    private fun <T : ApiObjectBase, M : Findable> T.toWrapper(clazz: Class<T>, sid: Sid): M {
        val wrapper: M = ctx.createPluginObject(this, clazz)
        wrapper.internalId = sid.with(clazz.pluginName, uuid)
        return wrapper
    }
}