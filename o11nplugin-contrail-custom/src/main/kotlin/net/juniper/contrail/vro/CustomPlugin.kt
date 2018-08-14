/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import com.vmware.o11n.sdk.modeldrivengen.model.Attribute
import com.vmware.o11n.sdk.modeldrivengen.model.ManagedFinder
import com.vmware.o11n.sdk.modeldrivengen.model.ManagedType
import com.vmware.o11n.sdk.modeldrivengen.model.Plugin
import net.juniper.contrail.vro.config.isCapitalized
import net.juniper.contrail.vro.config.isDisplayOnlyProperty
import net.juniper.contrail.vro.config.position
import net.juniper.contrail.vro.config.cleanedDisplayedProperty
import net.juniper.contrail.vro.config.defaultConfig
import net.juniper.contrail.vro.config.isRefWrapperProperty
import net.juniper.contrail.vro.config.refWrapperPropertyDisplayName
import net.juniper.contrail.vro.config.displayedName

class CustomPlugin : Plugin() {

    override fun getTypes(): List<ManagedType> {
        // Generator adds default managed types, which we want wrapped in our extended class.
        // Method is used to add to and view the list of ManagedTypes
        // It gives us more info while generating the wrappers.
        return super.getTypes().also { maybeWrap(it) }
    }

    override fun getFinders(): MutableList<ManagedFinder> {
        return super.getFinders().also { customize(it) }
    }

    private fun maybeWrap(types: MutableList<ManagedType>) {
        val iterator = types.listIterator()
        while (iterator.hasNext()) {
            val type = iterator.next()
            if (type !is CustomManagedType) {
                val cmt = CustomManagedType(type, defaultConfig)
                iterator.set(cmt)
            }
        }
    }

    private fun customize(finders: MutableList<ManagedFinder>) {
        finders.asSequence()
            .forEach { it.customize() }
    }

    private fun ManagedFinder.customize() {
        if (doc == null)
            doc = managedType?.modelClass?.description
        attributes.forEach { it.customize() }
        attributes.sortBy { it.accessor.position }
    }

    private fun Attribute.customize() {
        val displayedName = displayName
        if (displayedName != null && !displayedName.isCapitalized) {
            displayName = if (displayedName.isDisplayOnlyProperty)
                displayedName.cleanedDisplayedProperty.displayedName
            else if (displayedName.isRefWrapperProperty)
                displayName.refWrapperPropertyDisplayName
            else
                displayedName.displayedName
        }
    }
}
