/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import com.vmware.o11n.sdk.modeldrivengen.mapping.AbstractMapping
import com.vmware.o11n.sdk.modeldrivengen.model.Plugin
import net.juniper.contrail.vro.generated.CustomMapping

class CustomModule : AbstractModule() {

    private val plugin: Plugin

    /**
     * Binds the CustomMapping class to the plugin instance
     */
    override fun configure() {
        bind(AbstractMapping::class.java).toInstance(CustomMapping())
        bind(Plugin::class.java).toInstance(plugin)
        bind(String::class.java)
            .annotatedWith(Names.named("codegen.templatePath"))
            .toInstance("/net/juniper/contrail/vro")
    }

    init {
        this.plugin = CustomPlugin()

        plugin.apiPrefix = "Contrail"
        plugin.description = "Contrail plug-in for vRealize Orchestrator"
        plugin.displayName = "Contrail"
        plugin.name = "Contrail"
        plugin.icon = "opencontrail-16x16.png"
        plugin.packages = listOf("o11nplugin-contrail-package-\${project.version}.package")
        plugin.setAdaptorClassName(ContrailPluginAdaptor::class.java)
    }
}