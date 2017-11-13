package net.juniper.contrail.vro.generated;

/* ************************
 *     GENERATED FILE     *
 *       DO NOT EDIT      *
 **************************/

import com.vmware.o11n.sdk.modeldrivengen.mapping.AbstractMapping;
import com.vmware.o11n.sdk.modeldrivengen.mapping.FolderDef;

import net.juniper.contrail.api.types.*; // ktlint-disable no-wildcard-imports
import net.juniper.contrail.vro.config.ConnectionManager;
import net.juniper.contrail.vro.model.Connection;
import net.juniper.contrail.vro.model.ConnectionFinder;

public class CustomMapping extends AbstractMapping {

    @Override
    public void define() {
        convertWellKnownTypes();

        <#list canonicalNameClasses as klass>
        wrap(${klass.canonicalName}.class);
        </#list>

        <#list unfindableClasses as klass>
        wrap(${klass.simpleName}.class);
        </#list>

        <#list findableClasses as klass>
        wrap(${klass.simpleName}.class)
          .andFind()
          .using(${klass.simpleName}Finder.class)
          .withIcon("folder.png");
        </#list>


        wrap(Connection.class)
           .andFind()
           .using(ConnectionFinder.class)
           .withIcon("default-16x16.png");

        singleton(ConnectionManager.class);

        relateRoot()
            .to(Connection.class)
            .using(RootHasConnections.class)
            .as("root-to-connection");

        relate(Connection.class)
            .to(Project.class)
            .using(ConnectionHasProjects.class)
            .as("connection-to-project")
            .in(new FolderDef("Projects", "folder.png"));

        <#list relations as relation>
        relate(${relation.parentClassName}.class)
            .to(${relation.childClassName}.class)
            .using(${relation.parentClassName}Has${relation.childClassName}.class)
            .as("${relation.name}")
            .in(new FolderDef("${relation.childClassName}s", "folder.png"));
        </#list>
    }
}
