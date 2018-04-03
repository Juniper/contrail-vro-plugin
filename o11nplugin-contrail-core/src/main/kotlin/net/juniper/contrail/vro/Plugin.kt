/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import com.vmware.o11n.plugin.sdk.spring.InventoryRef
import com.vmware.o11n.sdk.modeldriven.AbstractModelDrivenAdaptor
import com.vmware.o11n.sdk.modeldriven.AbstractModelDrivenFactory
import com.vmware.o11n.sdk.modeldriven.Folder

class ContrailPluginAdaptor : AbstractModelDrivenAdaptor() {
    init {
        pluginName = "Contrail"
    }

    override fun getConfigLocations(): Array<String> =
        arrayOf("classpath:net/juniper/contrail/vro/plugin.xml")

    override fun getRuntimeConfigurationPath(): String =
        "net/juniper/contrail/vro/gen/runtime-config.properties"
}

class ContrailPluginFactory : AbstractModelDrivenFactory()
{
    override fun findChildrenInRootRelation(type: String?, relName: String?): List<*>? =
        super.findChildrenInRootRelation(type, relName)?.maybeConvertFolders()

    override fun findChildrenInRelation(ref: InventoryRef?, relName: String?): List<*>? =
        super.findChildrenInRelation(ref, relName)?.maybeConvertFolders()

    private fun List<*>.maybeConvertFolders(): List<*> {
        return if (size == 1) {
            val element = get(0)
            if (element is Folder)
                listOf(PrettyFolder(element))
            else
                this
        } else {
            this
        }
    }
}

private val folderNamePattern = "(.+)__in__.+".toRegex()

private fun String.toPrettyName() =
    folderNamePattern.matchEntire(this)?.groupValues?.get(1) ?: this

open class PrettyFolder(folder: Folder) : Folder(folder.parent, folder.name) {
    private val prettyName: String? = super.getName()?.toPrettyName()

    override fun getDisplayName(): String? =
        prettyName

    override fun getName(): String? =
        prettyName
}
