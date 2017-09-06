/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro;



import com.vmware.o11n.sdk.modeldrivengen.mapping.*;

import com.vmware.o11n.sdk.modeldrivengen.model.*;
import com.google.inject.*;
import java.util.*;

public class CustomModule extends AbstractModule {

    private final Plugin plugin;

    @Override
    protected void configure() {
        bind(AbstractMapping.class).toInstance(new CustomMapping());
        bind(Plugin.class).toInstance(plugin);
    }

    public CustomModule() {
        this.plugin = new Plugin();

        plugin.setApiPrefix("Contrail");
        plugin.setIcon("default-32x32.png");
        plugin.setDescription("Contrail plug-in for vRealize Orchestrator");
        plugin.setDisplayName("Contrail");
        plugin.setName("Contrail");
        plugin.setPackages(Collections.singletonList("o11nplugin-example-package-${project.version}.package"));
        plugin.setAdaptorClassName(net.juniper.contrail.vro.ContrailPluginAdaptor.class);
    }
}