${editWarning}
package ${packageName}

/* ktlint-disable no-wildcard-imports */
import java.io.File
import java.lang.reflect.Method
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

private val renamePolicy = object : MethodRenamePolicy {
    override fun rename(m: Method): String =
        m.name.toPluginMethodName
}

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
        singleton(Constants::class.java)
        singleton(ConnectionManager::class.java)
    }

    private fun doWrapping() {
        wrap(Connection::class.java)
           .andFind()
           .using(ConnectionFinder::class.java)
           .withIcon("controller.png")

        <#list findableClasses as klass>
        wrap(${klass.simpleName}::class.java)
          .`as`("${klass.pluginName}")
          .rename(renamePolicy)
          .hiding(*methodsToHide)
          .andFind()
          .`as`("${klass.pluginName}")
          .using(${klass.simpleName}Finder::class.java)
          .hiding(*propertiesToHide)
          .withIcon(findItemIcon<${klass.simpleName}>())
        </#list>

        <#list propertyClasses as klass>
        wrap(${klass.simpleName}::class.java)
        <#if klass.propertyAsObject>
          .rename(renamePolicy)
          .andFind()
          .using(${klass.simpleName}Finder::class.java)
          .withIcon(findItemIcon<${klass.simpleName}>())
        </#if>
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

        relate(NetworkIpam::class.java)
            .to(IpamSubnetType::class.java)
            .using(NetworkIpamHasSubnet::class.java)
            .`as`("NetworkIpamHasSubnet")

        <#list rootClasses as rootClass>
        relate(Connection::class.java)
            .to(${rootClass.simpleName}::class.java)
            .using(ConnectionHas${rootClass.simpleName}::class.java)
            .`as`("ConnectionHas${rootClass.simpleName}")
            .`in`(FolderDef(folderName("${rootClass.folderName}", "ROOT"), findFolderIcon<${rootClass.simpleName}>()))
        </#list>

        <#list relations as relation>
        relate(${relation.parentName}::class.java)
            .to(${relation.childName}::class.java)
            .using(${relation.parentName}Has${relation.childName}::class.java)
            .`as`("${relation.name}")
            <#if !relation.directChild >
            .`in`(FolderDef(folderName("${relation.folderName}", "${relation.parentName}", "${relation.childName}"), findFolderIcon<${relation.childName}>()))
            </#if>
        </#list>

        <#list forwardRelations as relation>
        relate(${relation.parentName}::class.java)
            .to(${relation.childName}::class.java)
            .using(${relation.parentName}To${relation.childName}::class.java)
            .`as`("${relation.parentName}To${relation.childName}")
            .`in`(FolderDef(folderName("${relation.folderName}", "${relation.parentName}", "${relation.getter}"), findFolderIcon<${relation.childName}>()))
        </#list>

        <#list nestedRelations as relation>
        relate(${relation.parentWrapperName}::class.java)
            .to(${relation.childWrapperName}::class.java)
            .using(${relation.parentWrapperName}Has${relation.childWrapperName}::class.java)
            .`as`("${relation.name}")
            <#if relation.toMany>.`in`(FolderDef(folderName("${relation.folderName}", "${relation.parentWrapperName}", "${relation.getter}"), findFolderIcon<${relation.childName}>()))</#if>
        </#list>
    }
}
