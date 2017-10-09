/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro;

import com.google.inject.AbstractModule;
import com.vmware.o11n.sdk.modeldrivengen.mapping.AbstractMapping;
import com.vmware.o11n.sdk.modeldrivengen.model.Plugin;
import java.util.Collections;

public class CustomModule extends AbstractModule {

    private final Plugin plugin;

    /**
     * Binds the CustomMapping class to the plugin instance
     */
    @Override
    protected void configure() {
        bind(AbstractMapping.class).toInstance(new CustomMapping());
        bind(Plugin.class).toInstance(plugin);

    }

    public CustomModule() {
        this.plugin = new Plugin();

        plugin.setApiPrefix("Contrail");
        plugin.setDescription("Contrail plug-in for vRealize Orchestrator");
        plugin.setDisplayName("Contrail");
        plugin.setName("Contrail");
        plugin.setIcon("opencontrail-16x16.png");
        plugin.setPackages(Collections.singletonList("o11nplugin-contrail-vro-package-${project.version}.package"));
        plugin.setAdaptorClassName(ContrailPluginAdaptor.class);
    }
}