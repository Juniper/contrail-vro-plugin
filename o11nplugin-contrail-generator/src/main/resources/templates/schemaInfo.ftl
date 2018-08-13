${editWarning}
package ${packageName}

val schemaReadOnlyPropertyNames = listOf(
<#list propertyNames as property>
Pair("${property.parentClassName}", "${property.elementName}")<#sep>,</#sep>
</#list>
)