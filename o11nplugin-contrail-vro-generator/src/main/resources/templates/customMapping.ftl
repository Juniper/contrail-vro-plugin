/***********************************************************
 *               GENERATED FILE - DO NOT EDIT              *
 ***********************************************************/
package net.juniper.contrail.vro.generated;

import com.vmware.o11n.sdk.modeldrivengen.mapping.AbstractMapping;
import com.vmware.o11n.sdk.modeldrivengen.mapping.FolderDef;
import net.juniper.contrail.api.types.*; // ktlint-disable no-wildcard-imports
import net.juniper.contrail.vro.config.ConnectionManager;
import net.juniper.contrail.vro.model.Connection;
import net.juniper.contrail.vro.model.ConnectionFinder;
import net.juniper.contrail.vro.model.RootHasConnections;

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

        String[] methodsToHide = {
            "getObjectType",
            "getDefaultParentType",
            "getDefaultParent",
            "getDisplayName",
        };

        String[] propertiesToHide = {
            "parentUuid",
            "parentType",
        };

        <#list findableClasses as klass>
        wrap(${klass.simpleName}.class)
          .hiding(methodsToHide)
          .andFind()
          .using(${klass.simpleName}Finder.class)
          .hiding(propertiesToHide)
          .withIcon("item-16x16.png");
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
            .in(new FolderDef("${rootClass.simpleNameSplitCamel}s", "folder.png"));
        </#list>

        <#list relations as relation>
        relate(${relation.parentClassName}.class)
            .to(${relation.childClassName}.class)
            .using(${relation.parentClassName}Has${relation.childClassName}.class)
            .as("${relation.name}")
            .in(new FolderDef("${relation.childClassNameSplitCamel}s", "folder.png"));
        </#list>
    }
}
