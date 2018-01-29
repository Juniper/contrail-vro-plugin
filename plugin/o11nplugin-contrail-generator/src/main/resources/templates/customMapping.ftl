${editWarning}
package ${packageName}

/* ktlint-disable no-wildcard-imports */
import com.vmware.o11n.sdk.modeldrivengen.mapping.*
import net.juniper.contrail.api.*
import net.juniper.contrail.api.types.*
import net.juniper.contrail.vro.base.*
import net.juniper.contrail.vro.model.*
/* ktlint-enable no-wildcard-imports */

class CustomMapping: AbstractMapping() {

    val methodsToHide = arrayOf(
        "getObjectType",
        "getDefaultParentType",
        "getDefaultParent",
        "setDisplayName"
    )

    val propertiesToHide = arrayOf(
        "parentUuid",
        "parentType"
    )

    override fun define() {
        convertWellKnownTypes()

        doSingletons()
        doWrapping()
        doRelations()
    }

    private fun doSingletons() {
        singleton(Utils::class.java)
        singleton(ConnectionManager::class.java)
    }

    private fun doWrapping() {
        wrap(Connection::class.java)
           .andFind()
           .using(ConnectionFinder::class.java)
           .withIcon("default-16x16.png")

        wrap(Executor::class.java)

        <#list findableClassNames as klass>
        wrap(${klass}::class.java)
          .hiding(*methodsToHide)
          .andFind()
          .using(${klass}Finder::class.java)
          .hiding(*propertiesToHide)
          .withIcon("item.png")
        </#list>

        <#list propertyClassNames as klass>
        wrap(${klass}::class.java)
        </#list>

        <#list nestedRelations as relation>
        wrap(${relation.childWrapperName}::class.java)
          .andFind()
          .using(${relation.childWrapperName}Finder::class.java)
          .hiding("listIdx")
          .withIcon("item-prop.png")
        </#list>
    }

    private fun doRelations() {

        relateRoot()
            .to(Connection::class.java)
            .using(RootHasConnections::class.java)
            .`as`("RootHasConnections")

        relate(VirtualNetwork::class.java)
            .to(Subnet::class.java)
            .using(VirtualNetworkHasSubnet::class.java)
            .`as`("VirtualNetworkToSubnet")
            .`in`(FolderDef("Subnets__in__VirtualNetwork_Subnets", "folder.png"))

        <#list rootClasses as rootClass>
        relate(Connection::class.java)
            .to(${rootClass.simpleName}::class.java)
            .using(ConnectionHas${rootClass.simpleName}::class.java)
            .`as`("ConnectionHas${rootClass.simpleName}")
            .`in`(FolderDef("${rootClass.folderName}__in__ROOT", "folder.png"))
        </#list>

        <#list relations as relation>
        relate(${relation.parentName}::class.java)
            .to(${relation.childName}::class.java)
            .using(${relation.parentName}Has${relation.childName}::class.java)
            .`as`("${relation.name}")
            <#if !relation.directChild >
            .`in`(FolderDef("${relation.folderName}__in__${relation.parentName}_${relation.childName}s", "folder.png"))
            </#if>
        </#list>

        <#list forwardRelations as relation>
        relate(${relation.parentName}::class.java)
            .to(${relation.childName}::class.java)
            .using(${relation.parentName}Has${relation.childName}::class.java)
            .`as`("${relation.parentName}To${relation.childName}")
            .`in`(FolderDef("${relation.folderName}__in__${relation.parentName}_${relation.getter}", "folder-ref.png"))
        </#list>

        <#list nestedRelations as relation>
        relate(${relation.parentWrapperName}::class.java)
            .to(${relation.childWrapperName}::class.java)
            .using(${relation.parentWrapperName}Has${relation.childWrapperName}::class.java)
            .`as`("${relation.name}")
            <#if relation.toMany>.`in`(FolderDef("${relation.folderName}__in__${relation.parentWrapperName}_${relation.getter}", "folder.png"))</#if>
        </#list>
    }
}
