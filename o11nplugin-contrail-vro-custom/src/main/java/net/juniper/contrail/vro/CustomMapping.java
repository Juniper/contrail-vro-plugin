/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro;

import com.vmware.o11n.sdk.modeldrivengen.mapping.AbstractMapping;

import java.math.RoundingMode;

public class CustomMapping extends AbstractMapping {
    @SuppressWarnings("unchecked")
    @Override
    public void define() {
        enumerate(RoundingMode.class);
    }
}