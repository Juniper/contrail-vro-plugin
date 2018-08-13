${editWarning}
package ${packageName}

import net.juniper.contrail.vro.generator.model.PropertyName

val schemaReadOnlyPropertyNames = listOf(
<#list propertyNames as property>
PropertyName("${property.parentClassName}", "${property.elementName}"),
</#list>
null)