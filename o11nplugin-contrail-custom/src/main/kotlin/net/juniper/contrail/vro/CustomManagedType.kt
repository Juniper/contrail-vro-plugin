/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import com.vmware.o11n.sdk.modeldriven.extension.Interceptor
import com.vmware.o11n.sdk.modeldrivengen.model.FormalParameter
import com.vmware.o11n.sdk.modeldrivengen.model.ManagedConstructor
import com.vmware.o11n.sdk.modeldrivengen.model.ManagedMethod
import com.vmware.o11n.sdk.modeldrivengen.model.ManagedType
import java.util.ArrayList

class CustomManagedType(private val delegate: ManagedType) : ManagedType() {

    val refsFields: List<CustomRefsField>
        get() {
            val fields = delegate.modelClass.declaredFields
            return fields
                .asSequence()
                .filter { it.name.endsWith("back_refs") }
                .map { CustomRefsField.wrapField(it) }
                .toList()
        }

    init {
        generateRefsMethods()
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

    override fun getMethods(): MutableList<ManagedMethod?> =
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

    private fun generateRefsMethods() {
        val methods = this.methods

        for (customField in this.refsFields) {
            val name = "get" + customField.wrapperMethodName
            val propertyName = customField.wrapperMethodName.decapitalize()
            val returnParameter = createReturnsFormalParameter()

            val method = ManagedMethod()
            method.setName(name, name)
            method.originalPropertyName = propertyName
            method.propertyName = propertyName
            method.params = ArrayList()
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
}
