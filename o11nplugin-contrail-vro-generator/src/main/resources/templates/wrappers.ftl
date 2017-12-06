${editWarning}
package net.juniper.contrail.vro.generated

import net.juniper.contrail.api.types.* // ktlint-disable no-wildcard-imports
import java.util.Date

<#macro constructor properties>
  <@compress single_line=true>
  <#if properties?has_content>@JvmOverloads </#if>
  constructor(
  <#list properties as property>
    ${property.propertyName}:${property.wrapperName}? = null<#if property_has_next>, </#if>
  </#list>)
  </@compress>
</#macro>

// Convenience functions to simplify generation
private fun String?.String() = this
private fun Boolean?.Boolean() = this ?: false
private fun Int?.Int() = this
private fun Long?.Long() = this
private fun Date?.Date() = this

<#list wrappers as wrapper>
class ${wrapper.name} {
    <#list wrapper.simpleProperties as property>
    var ${property.propertyName}: ${property.wrapperName}? = null
        set(value) { field = value }
    </#list>
    <#list wrapper.listProperties as property>
    var ${property.propertyName}: MutableList<${property.wrapperName}>? = null
        set(value) { field = value }
    </#list>
    <@constructor wrapper.simpleProperties/> {
        <#list wrapper.simpleProperties as property>
        this.${property.propertyName} = ${property.propertyName}
        </#list>
    }
    <#list wrapper.listProperties as property>
    fun add${property.componentName}(obj: ${property.wrapperName}?) {
        if (obj == null) return
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

fun ${wrapper.unwrappedName}?.${wrapper.name}() : ${wrapper.name} {
    val wrapper = ${wrapper.name}()
    if (this == null) return wrapper

    <#list wrapper.simpleProperties as property>
    wrapper.${property.propertyName} = ${property.propertyName}.${property.wrapperName}()
    </#list>
    <#list wrapper.listProperties as property>
    ${property.propertyName}?.forEach { wrapper.add${property.componentName}(it.${property.wrapperName}()) }
    </#list>
    return wrapper
}

fun ${wrapper.name}?.${wrapper.unwrappedLabel}() : ${wrapper.unwrappedName} {
    val model = ${wrapper.unwrappedName}()
    if (this == null) return model

    <#list wrapper.simpleProperties as property>
    model.${property.propertyName} = ${property.propertyName}.${property.classLabel}()
    </#list>
    <#list wrapper.listProperties as property>
    ${property.propertyName}?.forEach { model.add${property.componentName}(it.${property.classLabel}()) }
    </#list>

    return model
}

</#list>

