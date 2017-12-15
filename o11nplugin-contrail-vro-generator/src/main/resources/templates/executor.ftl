${editWarning}
package net.juniper.contrail.vro.generated

import net.juniper.contrail.api.types.* // ktlint-disable no-wildcard-imports
import net.juniper.contrail.vro.model.Connection
import java.io.IOException

class Executor(private val connection: Connection) {

    <#list relations as relation>
    @Throws(IOException::class)
    fun create${relation.childClassName}(obj: ${relation.childClassName}, parent: ${relation.parentClassName}) {
        obj.setParent(parent)
        connection.create(obj)
    }

    @Throws(IOException::class)
    fun delete${relation.childClassName}(obj: ${relation.childClassName}) {
        connection.delete(obj::class.java, obj.uuid)
    }
    </#list>

    <#list rootClasses as rootClass>
    @Throws(IOException::class)
    fun create${rootClass.simpleName}(obj: ${rootClass.simpleName}, parent: Connection) {
        connection.create(obj)
    }

    @Throws(IOException::class)
    fun delete${rootClass.simpleName}(obj: ${rootClass.simpleName}) {
        connection.delete(obj::class.java, obj.uuid)
    }
    </#list>
}