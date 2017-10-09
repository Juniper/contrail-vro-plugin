/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro;

import com.vmware.o11n.sdk.modeldrivengen.mapping.AbstractMapping;
import net.juniper.contrail.api.types.Project;
import net.juniper.contrail.vro.config.ConnectionManager;
import net.juniper.contrail.vro.model.*;

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

        relateRoot()
            .to(Connection.class)
            .using(RootHasConnections.class)
            .as("root-to-connection");

        relate(Connection.class)
            .to(Project.class)
            .using(ConnectionHasProjects.class)
            .as("connection-to-project");
    }
}