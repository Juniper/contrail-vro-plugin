${editWarning}
package ${packageName}

/* ktlint-disable no-wildcard-imports */
import com.vmware.o11n.sdk.modeldrivengen.mapping.*
import net.juniper.contrail.api.types.*
import net.juniper.contrail.vro.config.*
import net.juniper.contrail.vro.model.*
/* ktlint-enable no-wildcard-imports */

class CustomMapping: AbstractMapping() {

    override fun define() {
        convertWellKnownTypes()

        <#list nestedClasses as klass>
        wrap(${klass.canonicalName}::class.java)
            .`as`("${klass.simpleName}")
            // ANDFIND
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
        relate(${relation.parentName}::class.java)
            .to(${relation.childName}::class.java)
            .using(${relation.parentName}Has${relation.childName}::class.java)
            .`as`("${relation.name}")
            .`in`(FolderDef("${relation.folderName}", "folder.png"))
        </#list>
    }
}
