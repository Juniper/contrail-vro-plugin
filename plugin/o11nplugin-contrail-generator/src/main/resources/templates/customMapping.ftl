${editWarning}
package ${packageName}

/* ktlint-disable no-wildcard-imports */
import java.io.File
import com.vmware.o11n.sdk.modeldrivengen.mapping.*
import net.juniper.contrail.api.*
import net.juniper.contrail.api.types.*
import net.juniper.contrail.vro.base.*
import net.juniper.contrail.vro.config.*
import net.juniper.contrail.vro.model.*
/* ktlint-enable no-wildcard-imports */

private val iconDir = "${iconRootDir}/src/main/dar/resources/images"

private fun findIcon(customName: String, defaultName: String): String {
    if (File(iconDir, customName).isFile)
        return customName

    println("WARNING: Custom icon $customName was not found. Using default icon $defaultName.")

    return defaultName
}

inline private fun <reified T> findItemIcon() =
    findIcon(itemIconName<T>(), defaultItemIconName)

inline private fun <reified T> findFolderIcon() =
    findIcon(folderIconName<T>(), defaultFolderIconName)

class CustomMapping: AbstractMapping() {

    val methodsToHide = hiddenMethods.toTypedArray()

    val propertiesToHide = hiddenProperties.toTypedArray()

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
           .withIcon("controller.png")

        wrap(Executor::class.java)

        <#list findableClasses as klass>
        wrap(${klass.simpleName}::class.java)
          .hiding(*methodsToHide)
          .`as`("${klass.pluginName}")
          .andFind()
          .using(${klass.simpleName}Finder::class.java)
          .hiding(*propertiesToHide)
          .withIcon(findItemIcon<${klass.simpleName}>())
        </#list>

        <#list propertyClassNames as klass>
        wrap(${klass}::class.java)
        </#list>

        <#list nestedRelations as relation>
        wrap(${relation.childWrapperName}::class.java)
          .andFind()
          .using(${relation.childWrapperName}Finder::class.java)
          .hiding("listIdx")
          .withIcon(<#if relation.toMany>findItemIcon<#else>findFolderIcon</#if><${relation.childName}>())
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
            .`in`(FolderDef("Subnets__in__VirtualNetwork_Subnets", findFolderIcon<Subnet>()))

        <#list rootClasses as rootClass>
        relate(Connection::class.java)
            .to(${rootClass.simpleName}::class.java)
            .using(ConnectionHas${rootClass.simpleName}::class.java)
            .`as`("ConnectionHas${rootClass.simpleName}")
            .`in`(FolderDef("${rootClass.folderName}__in__ROOT", findFolderIcon<${rootClass.simpleName}>()))
        </#list>

        <#list relations as relation>
        relate(${relation.parentName}::class.java)
            .to(${relation.childName}::class.java)
            .using(${relation.parentName}Has${relation.childName}::class.java)
            .`as`("${relation.name}")
            <#if !relation.directChild >
            .`in`(FolderDef("${relation.folderName}__in__${relation.parentName}_${relation.childName}s", findFolderIcon<${relation.childName}>()))
            </#if>
        </#list>

        <#list forwardRelations as relation>
        relate(${relation.parentName}::class.java)
            .to(${relation.childName}::class.java)
            .using(${relation.parentName}Has${relation.childName}::class.java)
            .`as`("${relation.parentName}To${relation.childName}")
            .`in`(FolderDef("${relation.folderName}__in__${relation.parentName}_${relation.getter}", findFolderIcon<${relation.childName}>()))
        </#list>

        <#list nestedRelations as relation>
        relate(${relation.parentWrapperName}::class.java)
            .to(${relation.childWrapperName}::class.java)
            .using(${relation.parentWrapperName}Has${relation.childWrapperName}::class.java)
            .`as`("${relation.name}")
            <#if relation.toMany>.`in`(FolderDef("${relation.folderName}__in__${relation.parentWrapperName}_${relation.getter}", findFolderIcon<${relation.childName}>()))</#if>
        </#list>
    }
}
