/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import com.vmware.o11n.sdk.modeldrivengen.model.Attribute
import com.vmware.o11n.sdk.modeldrivengen.model.ManagedFinder
import com.vmware.o11n.sdk.modeldrivengen.model.ManagedType
import com.vmware.o11n.sdk.modeldrivengen.model.Plugin
import net.juniper.contrail.vro.config.backRefWrapperPropertyDisplayName
import net.juniper.contrail.vro.config.cleanedDisplayedProperty
import net.juniper.contrail.vro.config.displayedName
import net.juniper.contrail.vro.config.isApiObjectClass
import net.juniper.contrail.vro.config.isBackRefWrapperProperty
import net.juniper.contrail.vro.config.isCapitalized
import net.juniper.contrail.vro.config.isDisplayOnlyProperty
import net.juniper.contrail.vro.config.position

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
            .forEach { it.clean() }
    }

    private fun ManagedFinder.clean() {
        attributes.forEach { it.clean() }
        attributes.sortBy { it.accessor.position }
    }

    private fun Attribute.clean() {
        val displayedName = displayName
        if (displayedName != null && !displayedName.isCapitalized) {
            displayName = if (displayedName.isDisplayOnlyProperty)
                displayedName.cleanedDisplayedProperty.displayedName
            else if (displayedName.isBackRefWrapperProperty)
                displayName.backRefWrapperPropertyDisplayName
            else
                displayedName.displayedName
        }
    }
}
