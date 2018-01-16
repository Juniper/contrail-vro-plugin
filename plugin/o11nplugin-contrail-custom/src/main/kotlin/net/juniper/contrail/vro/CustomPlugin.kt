/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import com.vmware.o11n.sdk.modeldrivengen.model.ManagedFinder
import com.vmware.o11n.sdk.modeldrivengen.model.ManagedType
import com.vmware.o11n.sdk.modeldrivengen.model.Plugin
import net.juniper.contrail.vro.config.isApiObjectClass

class CustomPlugin : Plugin() {

    override fun getTypes(): List<ManagedType> {
        // Generator adds default managed types, which we want wrapped in our extended class.
        // Method is used to add to and view the list of ManagedTypes
        // It gives us more info while generating the wrappers.
        val list = super.getTypes()
        maybeWrap(list)
        return list
    }

    override fun getFinders(): MutableList<ManagedFinder> {
        val finders = super.getFinders()
        cleanFinders(finders)
        return finders
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

    private fun cleanFinders(finders: MutableList<ManagedFinder>) {
        finders.asSequence()
            .filter { it.managedType?.modelClass?.isApiObjectClass ?: false }
            .map { it.attributes.asSequence() }
            .flatten()
            .filter { it.displayName?.endsWith(displayedPropertySuffix) ?: false }
            .forEach { it.displayName = it.displayName.replace(displayedPropertyPattern, "") }
    }
}
