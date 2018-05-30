/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import com.vmware.o11n.sdk.modeldriven.ObjectFactory
import com.vmware.o11n.sdk.modeldriven.PluginContext

/**
 * Class allows for customization of plugin objects created by default object factory.
 */
class TestObjectFactory(private val delegate: ObjectFactory) : ObjectFactory by delegate
{
    @Suppress("UNCHECKED_CAST")
    override fun <PluginType : Any?, ModelType : Any?>
    createPluginObject(modelType: ModelType, ctx: PluginContext?, clazz: Class<*>?): PluginType? {
        val result = delegate.createPluginObject<PluginType, ModelType>(modelType, ctx, clazz)
        return if (result is MutableList<*>)
            ListAsArray(result as MutableList<Any>) as PluginType
        else
            result
    }
}

/**
 * Class used to simulate JS Array inside test scripts.
 */
class ListAsArray<T : Any>(delegate: MutableList<T>) : MutableList<T> by delegate
{
    fun splice(index: Int, n: Int) {
        repeat(n) { removeAt(index) }
    }
}