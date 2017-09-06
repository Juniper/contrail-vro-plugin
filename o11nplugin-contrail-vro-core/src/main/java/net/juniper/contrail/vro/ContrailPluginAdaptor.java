/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro;

import com.vmware.o11n.sdk.modeldriven.AbstractModelDrivenAdaptor;

public class ContrailPluginAdaptor extends AbstractModelDrivenAdaptor {
    private static final String[] CONFIG_LOCATIONS = { "classpath:net/juniper/contrail/vro/plugin.xml" };

    private static final String RUNTIME_PROPERTIES_LOCATION = "net/juniper/contrail/vro_gen/runtime-config.properties";

    @Override
    protected String[] getConfigLocations() {
        return CONFIG_LOCATIONS;
    }

    @Override
    protected String getRuntimeConfigurationPath() {
        return RUNTIME_PROPERTIES_LOCATION;
    }
}