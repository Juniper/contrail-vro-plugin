/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import com.vmware.o11n.sdk.modeldriven.AbstractModelDrivenAdaptor
import com.vmware.o11n.sdk.modeldriven.AbstractModelDrivenFactory

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