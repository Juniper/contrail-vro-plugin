${editWarning}
package ${packageName}

import net.juniper.contrail.api.types.* // ktlint-disable no-wildcard-imports
import java.util.Date

<#macro constructor properties>
  <@compress single_line=true>
  <#if properties?has_content>@JvmOverloads </#if>
  constructor(
  <#list properties as property>
    ${property.propertyName}:${property.wrapperName}? = null,
  </#list> listIdx: Int? = null)
  </@compress>
</#macro>

// Convenience functions to simplify generation
private fun String?.String() = this
private fun Boolean?.Boolean() = this ?: false
private fun Int?.Int() = this
private fun Long?.Long() = this
private fun Date?.Date() = this

private fun String?.String(lidx: Int?) = this
private fun Boolean?.Boolean(lidx: Int?) = this ?: false
private fun Int?.Int(lidx: Int?) = this
private fun Long?.Long(lidx: Int?) = this
private fun Date?.Date(lidx: Int?) = this

<#list wrappers as wrapper>
class ${wrapper.name} {
    var listIdx: Int? = null
    <#list wrapper.simpleProperties as property>
    var ${property.propertyName}: ${property.wrapperName}? = null
        set(value) { field = value }
    </#list>
    <#list wrapper.listProperties as property>
    var ${property.propertyName}: MutableList<${property.wrapperName}>? = null
        set(value) { field = value }
    </#list>
    <@constructor wrapper.simpleProperties/> {
        this.listIdx = listIdx
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

fun ${wrapper.unwrappedName}?.${wrapper.name}(listIdx: Int?) : ${wrapper.name} {
    val wrapper = ${packageName}.${wrapper.name}()
    if (this == null) return wrapper

    wrapper.listIdx = listIdx

    <#list wrapper.simpleProperties as property>
    wrapper.${property.propertyName} = ${property.propertyName}.${property.wrapperName}(null)
    </#list>
    <#list wrapper.listProperties as property>
    if(${property.propertyName} != null){
        for ((index, value) in ${property.propertyName}.withIndex()) {
            wrapper.add${property.componentName}(value.${property.wrapperName}(index))
        }
    }
    </#list>
    return wrapper
}

fun ${wrapper.name}?.${wrapper.unwrappedLabel}() : ${wrapper.unwrappedName} {
    val model = ${juniPackageName}.${wrapper.unwrappedName}()
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

