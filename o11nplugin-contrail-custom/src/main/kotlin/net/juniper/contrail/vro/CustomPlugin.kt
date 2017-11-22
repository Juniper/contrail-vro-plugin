/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import com.vmware.o11n.sdk.modeldrivengen.model.ManagedType
import com.vmware.o11n.sdk.modeldrivengen.model.Plugin

class CustomPlugin : Plugin() {

    override fun getTypes(): List<ManagedType> {
        // Generator adds default managed types, which we want wrapped in our extended class.
        // Method is used to add to and view the list of ManagedTypes
        // It gives us more info while generating the wrappers.
        val list = super.getTypes()
        maybeWrap(list)
        return list
    }

    private fun maybeWrap(types: MutableList<ManagedType>) {
        val iterator = types.listIterator()
        while (iterator.hasNext()) {
            val type = iterator.next()
            if (type !is CustomManagedType) {
                iterator.set(CustomManagedType.wrap(type))
            }
        }
    }
}
