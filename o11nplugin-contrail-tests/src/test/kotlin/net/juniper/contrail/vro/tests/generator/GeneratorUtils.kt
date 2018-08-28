/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.generator

import net.juniper.contrail.api.types.NetworkPolicy
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.config.pair

// this method is necessary because Groovy Tuple2 is ignored
fun getVirtualNetworkPolicyPair() =
    pair<VirtualNetwork, NetworkPolicy>()