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
          .hiding("getObjectType", "getDefaultParentType", "getDefaultParent")
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
            .as("RootHasConnections");

        <#list rootClasses as rootClass>
        relate(Connection.class)
            .to(${rootClass.simpleName}.class)
            .using(ConnectionHas${rootClass.simpleName}.class)
            .as("ConnectionHas${rootClass.simpleName}")
            .in(new FolderDef("${rootClass.simpleName}s", "folder.png"));
        </#list>

        <#list relations as relation>
        relate(${relation.parentClassName}.class)
            .to(${relation.childClassName}.class)
            .using(${relation.parentClassName}Has${relation.childClassName}.class)
            .as("${relation.name}")
            .in(new FolderDef("${relation.childClassName}s", "folder.png"));
        </#list>
    }
}
