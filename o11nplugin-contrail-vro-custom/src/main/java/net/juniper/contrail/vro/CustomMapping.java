/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro;

import com.vmware.o11n.sdk.modeldrivengen.mapping.AbstractMapping;
import com.vmware.o11n.sdk.modeldrivengen.mapping.FolderDef;
import net.juniper.contrail.api.types.NetworkPolicy;
import net.juniper.contrail.api.types.Project;
import net.juniper.contrail.api.types.VirtualNetwork;
import net.juniper.contrail.vro.config.ConnectionManager;
import net.juniper.contrail.vro.model.*;
import net.juniper.contrail.vro.config.ConstantsKt;

public class CustomMapping extends AbstractMapping {

    @Override
    public void define() {
        convertWellKnownTypes();

        singleton(ConnectionManager.class);

        wrap(Connection.class)
           .andFind()
           .using(ConnectionFinder.class)
           .withIcon("default-16x16.png");

        wrap(Project.class)
            .andFind()
            .using(ProjectFinder.class)
            .withIcon("folder.png");

        wrap(VirtualNetwork.class)
            .andFind()
            .using(VirtualNetworkFinder.class)
            .withIcon("folder.png");

        wrap(NetworkPolicy.class)
            .andFind()
            .using(NetworkPolicyFinder.class)
            .withIcon("folder.png");

        relateRoot()
            .to(Connection.class)
            .using(RootHasConnections.class)
            .as("root-to-connection");

        relate(Connection.class)
            .to(Project.class)
            .using(ConnectionHasProjects.class)
            .as("connection-to-project")
            .in(new FolderDef("Projects", "folder.png"));

        relate(Project.class)
            .to(VirtualNetwork.class)
            .using(ProjectHasVirtualNetworks.class)
            .as(ConstantsKt.getPROJECT_HAS_VIRTUAL_NETWORKS())
            .in(new FolderDef("VirtualNetworks", "folder.png"));

        relate(Project.class)
            .to(NetworkPolicy.class)
            .using(ProjectHasNetworkPolicys.class)
            .as(ConstantsKt.getPROJECT_HAS_NETWORK_POLICYS())
            .in(new FolderDef("NetworkPolicys", "folder.png"));
    }
}