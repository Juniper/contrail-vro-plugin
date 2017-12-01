${editWarning}
package net.juniper.contrail.vro.generated


<#list fieldInfos as fieldInfo>
<#list fieldInfo.second.simpleFields as simpleField>
// ${simpleField.name}
class ${fieldInfo.first}${simpleField.name} {

}

</#list>

<#list fieldInfo.second.listFields as listField>
// ${listField.name}
class ${fieldInfo.first}${listField.name} {

}

</#list>



</#list>
