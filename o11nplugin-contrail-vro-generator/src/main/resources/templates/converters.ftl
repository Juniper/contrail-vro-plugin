${editWarning}
package ${packageName}

/* ktlint-disable no-wildcard-imports */
import com.vmware.o11n.sdk.modeldriven.Converter
import net.juniper.contrail.api.types.*
import java.util.Date
/* ktlint-enable no-wildcard-imports */

<#macro constructorArgs properties>
  <@compress single_line=true>
  <#list properties as property>
    ${property.propertyName}:${property.className}? = null<#if property_has_next>, </#if>
  </#list>
  </@compress>
</#macro>

<#macro toModel property>
  <@compress single_line=true>
    <#if property.className == 'Boolean'>?: false<#elseif isProxy(property.className)>.as${property.collapsedName}()</#if>
  </@compress>
</#macro>

<#macro toProxy property>
  <@compress single_line=true>
    <#if isProxy(property.className)>.asProxy()</#if>
  </@compress>
</#macro>

<#list proxies as proxy>
class ${proxy.name} {
    <#list proxy.simpleProperties as property>
    var ${property.propertyName}: ${property.className}? = null
        set(value) { field = value }
    </#list>
    <#list proxy.listProperties as property>
    var ${property.propertyName}: MutableList<${property.className}>? = null
        set(value) { field = value }
    </#list>
    @JvmOverloads constructor(<@constructorArgs proxy.simpleProperties/>) {
        <#list proxy.simpleProperties as property>
        this.${property.propertyName} = ${property.propertyName}
        </#list>
    }
    <#list proxy.listProperties as property>
    fun add${property.componentName}(obj: ${property.className}) {
        if (${property.propertyName} == null) {
            ${property.propertyName} = mutableListOf()
        }
        ${property.propertyName}!!.add(obj)
    }

    fun clear${property.componentName}() {
        ${property.propertyName} = null
    }

    </#list>
}

</#list>

<#list converters as converter>
@Suppress("UNCHECKED_CAST")
class ${converter.proxyName}To${converter.targetCollapsedName}Converter : Converter<${converter.proxyName}> {
    override fun <ModelType : Any?> convert(modelType: ModelType): ${converter.proxyName} {
        modelType as ${converter.targetName}?
        return modelType.asProxy()
    }

    override fun <ModelType : Any?> extract(pluginType: ${converter.proxyName}?, p1: Class<out ModelType>?): ModelType {
        return pluginType.as${converter.targetCollapsedName}() as ModelType
    }
}

private fun ${converter.targetName}?.asProxy() : ${converter.proxyName} {
    val pluginType = ${converter.proxyName}()
    if (this == null) return pluginType

    <#list converter.simpleProperties as property>
    pluginType.${property.propertyName} = ${property.propertyName}<@toProxy property/>
    </#list>
    <#list converter.listProperties as property>
    ${property.propertyName}?.forEach { pluginType.add${property.componentName}(it<@toProxy property/>) }
    </#list>
    return pluginType
}

private fun ${converter.proxyName}?.as${converter.targetCollapsedName}() : ${converter.targetName} {
    val modelType = ${converter.targetName}()
    if (this == null) return modelType

    <#list converter.simpleProperties as property>
    modelType.${property.propertyName} = ${property.propertyName}<@toModel property/>
    </#list>
    <#list converter.listProperties as property>
    ${property.propertyName}?.forEach { modelType.add${property.componentName}(it<@toModel property/>) }
    </#list>

    return modelType
}

</#list>