/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import com.vmware.o11n.sdk.modeldriven.extension.Interceptor
import com.vmware.o11n.sdk.modeldrivengen.model.FormalParameter
import com.vmware.o11n.sdk.modeldrivengen.model.ManagedConstructor
import com.vmware.o11n.sdk.modeldrivengen.model.ManagedMethod
import com.vmware.o11n.sdk.modeldrivengen.model.ManagedType
import net.juniper.contrail.vro.config.backRefTypeName
import net.juniper.contrail.vro.config.isApiObjectClass
import net.juniper.contrail.vro.config.isHiddenProperty
import net.juniper.contrail.vro.config.isInventoryProperty
import net.juniper.contrail.vro.config.isModelClassName
import net.juniper.contrail.vro.config.returnTypeOrListType
import net.juniper.contrail.vro.config.returnsApiPropertyOrList
import java.lang.reflect.Method

class CustomManagedType(private val delegate: ManagedType) : ManagedType() {

    val refsFields: List<CustomRefsField> = delegate.modelClass?.run {
        declaredFields
            .asSequence()
            .filter { it.name.endsWith("back_refs") }
            .filter { it.backRefTypeName.isModelClassName }
            .map { CustomRefsField.wrapField(it) }
            .toList()
    } ?: emptyList()

    val propertyViews: List<CustomProperty> = delegate.modelClass?.run {
        if (isApiObjectClass)
        methods.asSequence()
            .filter { it.name.startsWith("get") }
            .filter { it.returnsApiPropertyOrList }
            .filter { ! it.returnType.isInventoryProperty }
            .map { it.toCustomProperty() }
            .filter { ! it.propertyName.isHiddenProperty }
            .toList()
        else
            null
    } ?: emptyList()

    val isObjectClass get() =
        delegate.modelClass?.isApiObjectClass ?: false

    private fun Method.toCustomProperty() =
        CustomProperty(returnTypeOrListType!!.simpleName, name)

    init {
        generatePropertyMethods()
        generateRefsMethods()
    }

    private fun generateRefsMethods() {
        for (customField in this.refsFields) {
            val name = customField.wrapperMethodName
            val originalProperty = customField.propertyName
            val returnParameter = createReturnsFormalParameter()

            val method = ManagedMethod()
            method.setName(name, name)
            method.originalPropertyName = originalProperty
            method.propertyName = originalProperty
            method.params = emptyList()
            method.setIsInheritedWrapperMethod(true)
            method.isPropertyReadOnly = true
            method.returns = returnParameter

            methods.add(method)
        }
    }

    private fun generatePropertyMethods() {
        for (property in propertyViews) {
            val returnParameter = createReturnsFormalParameter()

            val method = ManagedMethod()
            method.setName(property.viewMethodName, property.viewMethodName)
            method.propertyName = property.viewPropertyName
            method.originalPropertyName = property.viewPropertyName
            method.params = emptyList()
            method.setIsInheritedWrapperMethod(true)
            method.isPropertyReadOnly = true
            method.returns = returnParameter

            methods.add(method)
        }
    }

    private fun createReturnsFormalParameter(): FormalParameter {
        val returnParameter = FormalParameter()
        returnParameter.name = "_result"
        returnParameter.modelType = String::class.java
        returnParameter.fullClassName = String::class.java.name
        returnParameter.typeName = "String"
        returnParameter.isWrapped = false
        return returnParameter
    }

    companion object {

        fun wrap(type: ManagedType): CustomManagedType {
            return CustomManagedType(type)
        }
    }

    override fun isGenerate(): Boolean =
        delegate.isGenerate

    override fun setGenerate(generate: Boolean) {
        delegate.isGenerate = generate
    }

    override fun isFindable(): Boolean
        = delegate.isFindable

    override fun setFindable(findable: Boolean) {
        delegate.isFindable = findable
    }

    override fun isRootIdPropagated(): Boolean =
        delegate.isRootIdPropagated

    override fun setRootIdPropagated(rootIdPropagated: Boolean) {
        delegate.isRootIdPropagated = rootIdPropagated
    }

    override fun getFullSuperClassName(): String? =
        delegate.fullSuperClassName

    override fun setFullSuperClassName(fullSuperClassName: String) {
        delegate.fullSuperClassName = fullSuperClassName
    }

    override fun getPackageName(): String? =
        delegate.packageName

    override fun getClassName(): String? =
        delegate.className

    override fun getName(): String? =
        delegate.name

    override fun isSingleton(): Boolean =
        delegate.isSingleton

    override fun setSingleton(singeton: Boolean) {
        delegate.isSingleton = singeton
    }

    override fun isCreateable(): Boolean =
        delegate.isCreateable

    override fun setCreateable(createable: Boolean) {
        delegate.isCreateable = createable
    }

    override fun getFullClassName(): String? =
        delegate.fullClassName

    override fun setFullClassName(className: String) {
        delegate.fullClassName = className
    }

    override fun getMethods(): MutableList<ManagedMethod> =
        delegate.methods

    override fun setMethods(methods: List<ManagedMethod>) {
        delegate.methods = methods
    }

    override fun getDoc(): String? =
        delegate.doc

    override fun setDoc(doc: String) {
        delegate.doc = doc
    }

    override fun getConstructors(): List<ManagedConstructor?>? =
        delegate.constructors

    override fun setConstructors(constructors: List<ManagedConstructor>) {
        delegate.constructors = constructors
    }

    override fun toString(): String =
        delegate.toString()

    override fun setName(name: String) {
        delegate.name = name
    }

    override fun getModelClass(): Class<*>? =
        delegate.modelClass

    override fun setModelClass(wrappedClass: Class<*>) {
        delegate.modelClass = wrappedClass
    }

    override fun getInterceptor(): Class<out Interceptor>? =
        delegate.interceptor

    override fun setInterceptor(interceptorClass: Class<out Interceptor>) {
        delegate.interceptor = interceptorClass
    }
}
