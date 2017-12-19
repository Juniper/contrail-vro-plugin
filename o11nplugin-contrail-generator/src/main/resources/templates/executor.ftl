${editWarning}
package net.juniper.contrail.vro.generated

import net.juniper.contrail.api.types.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.vro.model.Connection
import java.io.IOException

class Executor(private val connection: Connection) {

    <#list relations as relation>
    @Throws(IOException::class)
    fun create${relation.childName}(obj: ${relation.childName}, parent: ${relation.parentName}) {
        obj.setParent(parent)
        connection.create(obj)
    }
    </#list>

    <#list rootClasses as rootClass>
    @Throws(IOException::class)
    fun create${rootClass.simpleName}(obj: ${rootClass.simpleName}) {
        connection.create(obj)
    }
    </#list>

    <#list findableClassNames as klass>
    @Throws(IOException::class)
    fun update${klass}(obj: ${klass}) {
        connection.update(obj)
    }

    @Throws(IOException::class)
    fun delete${klass}(obj: ${klass}) {
        connection.delete(${klass}::class.java, obj.uuid)
    }
    </#list>

}