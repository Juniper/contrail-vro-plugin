${editWarning}
package net.juniper.contrail.vro.generated

import com.vmware.o11n.sdk.modeldrivengen.mapping.AbstractMapping
import com.vmware.o11n.sdk.modeldrivengen.mapping.FolderDef
import net.juniper.contrail.api.types.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.vro.config.ConnectionManager
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.vro.model.ConnectionFinder
import net.juniper.contrail.vro.model.RootHasConnections

class CustomMapping: AbstractMapping() {

    override fun define() {
        convertWellKnownTypes()

        <#list nestedClasses as klass>
        wrap(${klass.nestedName}::class.java)
        </#list>

        val methodsToHide = arrayOf(
            "getObjectType",
            "getDefaultParentType",
            "getDefaultParent",
            "getDisplayName"
        )

        val propertiesToHide = arrayOf(
            "parentUuid",
            "parentType"
        )

        wrap(Executor::class.java)

        <#list findableClasses as klass>
        wrap(${klass.simpleName}::class.java)
          .hiding(*methodsToHide)
          .andFind()
          .using(${klass.simpleName}Finder::class.java)
          .hiding(*propertiesToHide)
          .withIcon("item-16x16.png")
        </#list>

        wrap(Connection::class.java)
           .andFind()
           .using(ConnectionFinder::class.java)
           .withIcon("default-16x16.png")

        singleton(ConnectionManager::class.java)

        relateRoot()
            .to(Connection::class.java)
            .using(RootHasConnections::class.java)
            .`as`("RootHasConnections")

        <#list rootClasses as rootClass>
        relate(Connection::class.java)
            .to(${rootClass.simpleName}::class.java)
            .using(ConnectionHas${rootClass.simpleName}::class.java)
            .`as`("ConnectionHas${rootClass.simpleName}")
            .`in`(FolderDef("${rootClass.folderName}", "folder.png"))
        </#list>

        <#list relations as relation>
        relate(${relation.parentClassName}::class.java)
            .to(${relation.childClassName}::class.java)
            .using(${relation.parentClassName}Has${relation.childClassName}::class.java)
            .`as`("${relation.name}")
            .`in`(FolderDef("${relation.folderName}", "folder.png"))
        </#list>
    }
}
