/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import com.vmware.o11n.sdk.modeldriven.extension.Interceptor
import com.vmware.o11n.sdk.modeldrivengen.model.FormalParameter
import com.vmware.o11n.sdk.modeldrivengen.model.ManagedConstructor
import com.vmware.o11n.sdk.modeldrivengen.model.ManagedMethod
import com.vmware.o11n.sdk.modeldrivengen.model.ManagedType
import net.juniper.contrail.vro.config.Config
import net.juniper.contrail.vro.config.backReferencePattern
import net.juniper.contrail.vro.config.constants.apiTypesPackageName
import net.juniper.contrail.vro.config.pluginName
import net.juniper.contrail.vro.config.toPluginName
import net.juniper.contrail.vro.config.isApiObjectClass
import net.juniper.contrail.vro.config.isApiPropertyClass
import net.juniper.contrail.vro.config.isGetter
import net.juniper.contrail.vro.config.isHiddenProperty
import net.juniper.contrail.vro.config.isPublic
import net.juniper.contrail.vro.config.parameterClass
import net.juniper.contrail.vro.config.returnsApiPropertyOrList
import net.juniper.contrail.vro.config.returnsObjectReferences
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.vro.model.Executor
import java.lang.reflect.Method
import java.lang.reflect.Parameter

private val listClass = List::class.java

private val executorTypes = Executor::class.java.declaredMethods.asSequence()
    .filter { it.isPublic }
    // only methods with at least one parameter should be in executor
    .groupBy { it.parameterTypes[0] }

private fun ManagedType.createExecutorMethods(config: Config): List<ManagedMethod> {
    val methods = executorTypes[modelClass] ?: emptyList()
    return methods.map { it.toExecutorMethod(config) }
}

private fun Method.toExecutorMethod(config: Config) = ManagedMethod().apply {
    val methodName = this@toExecutorMethod.name
    setName(methodName, null)
    params = executorMethodParameters(config)
    // trick to avoid generating standard wrapper method
    setIsInheritedWrapperMethod(true)
    isPropertyReadOnly = true
    returns = executorFormalParameter(returnType, "_result", genericReturnType.parameterClass, config)
}

private fun Method.executorMethodParameters(config: Config) =
    parameters.asSequence().drop(1)
        .map { it.toExecutorFormalParameter(config) }
        .toList()

private fun Parameter.toExecutorFormalParameter(config: Config) =
    executorFormalParameter(type, name, parameterizedType.parameterClass, config)

/**
 * Formal parameter created by this function has the following content:
 * typeName - always not null, type of plugin class
 * componentTypeName - not null if parameter is wrapped model class
 * componentClassName - not null if parameter is list
 */
private fun executorFormalParameter(clazz: Class<*>, parameterName: String, component: Class<*>?, config: Config) : FormalParameter {
    val parameter = commonFormalParameter(clazz, parameterName, config)
    return when {
        listClass.isAssignableFrom(clazz) -> listParamConfig(parameter, component!!, config)
        config.isModelClass(clazz) -> clazz.modelParamConfig(parameter)
        else -> clazz.simpleParamConfig(parameter)
    }
}

private fun commonFormalParameter(clazz: Class<*>, parameterName: String, config: Config) = FormalParameter().apply {
    name = parameterName
    modelType = clazz
    isWrapped = config.isModelClass(clazz)
}

private fun listParamConfig(parameter: FormalParameter, component: Class<*>, config: Config) = parameter.apply {
    componentClassName = component.canonicalName
    val className = component.simpleName
    fullClassName = "List<$className>"
    if (config.isModelClass(component)) {
        typeName = "List<${className}_Wrapper>"
        componentTypeName = component.pluginName
    } else {
        typeName = "List<$className>"
    }
}

private fun Class<*>.modelParamConfig(parameter: FormalParameter) = parameter.apply {
    fullClassName = canonicalName
    typeName = "${simpleName}_Wrapper"
    componentTypeName = pluginName
}

private fun Class<*>.simpleParamConfig(parameter: FormalParameter) = parameter.apply {
    fullClassName = canonicalName
    typeName = canonicalName
}

class ClassInfo(val simpleName: String) {
    val pluginName = simpleName.toPluginName
}

class CustomManagedType(private val delegate: ManagedType, val config: Config) : ManagedType() {

    val isObjectClass get() =
        delegate.modelClass?.isApiObjectClass ?: false

    val isConnectionClass get() =
        delegate.modelClass == Connection::class.java

    val isNodeClass get() =
        delegate.modelClass?.let { config.isNodeClass(it) } ?: false

    val isDraftClass get() =
        delegate.modelClass?.let { config.isDraftClass(it) } ?: false

    val pluginName = delegate.modelClass?.pluginName

    val parents = if (delegate.modelClass == null) emptyList() else config.parentsInPlugin(delegate.modelClass)
                                                                    .toList().let { it + Connection::class.java }

    val executorMethods = delegate.createExecutorMethods(config)

    val references: List<CustomReference> = delegate.modelClass?.run {
        methods.asSequence()
            .map { it.toCustomReference(config) }.filterNotNull()
            .toList()
    } ?: emptyList()

    val backrefs: List<CustomReference> = delegate.modelClass?.run {
        references.asSequence()
            .filter { it.methodName.matches(backReferencePattern) }.toList()
    } ?: emptyList()

    val referenceProperties: List<CustomReferenceProperty> = delegate.modelClass?.run {
        if (isApiObjectClass)
        methods.asSequence()
            .filter { it.returnsObjectReferences }
            .filter { it.isReferenceProperty }
            .filter { config.isModelClass(it.referencePropertyClass) }
            .map { it.toCustomReferenceProperty() }
            .toList()
        else
            null
    } ?: emptyList()

    val connectionFindClasses: List<ClassInfo> = if (isConnectionClass) {
        config.context.modelClasses.map { ClassInfo(it) }
    } else {
        emptyList()
    }

    val customProperties: List<AdditionalProperty> = delegate.modelClass?.run {
        if (config.isCustomPropertyObject(this) || config.isInventoryProperty(this))
            propertyAsObjectNewProperties
        else
            null
    } ?: emptyList()

    val propertyViews: List<CustomProperty> = delegate.modelClass?.run {
        if (isApiObjectClass)
        methods.asSequence()
            .filter { it.isGetter }
            .filter { it.returnsApiPropertyOrList }
            .filter { ! config.isInventoryProperty(it.returnType) }
            .map { it.toCustomProperty() }
            .filter { ! it.propertyName.isHiddenProperty }
            .toList()
        else
            null
    } ?: emptyList()

    init {
        generateDescription()
        removeDuplicateMethods()
        generateReferenceMethods()
        generatePropertyMethods()
        generateReferencePropertiesMethods()
        generateCustomProperties()
    }

    private fun generateDescription() {
        delegate.doc = delegate.modelClass?.description
    }

    private fun removeDuplicateMethods() {
        delegate.methods.asSequence()
            .filter { it.isPropertyEditor }
            .groupBy { it.name }
            .filter { it.value.size > 1 }
            .values
            .forEach { it.removeDuplicates() }
    }

    private fun List<ManagedMethod>.removeDuplicates() {
        val remaining = methodToRetain
        asSequence()
            .filter { it != remaining }
            .forEach { delegate.methods.remove(it) }
    }

    private val List<ManagedMethod>.methodToRetain get() =
        first { it.params.size == 1 && it.params[0].modelType.isApiPropertyClass }

    private val ManagedMethod.isPropertyEditor get() =
        returns.modelType == Void.TYPE && params.let {
            it.size >= 1 && ! it[0].modelType.isApiObjectClass
        }

    private fun generateReferenceMethods() =
        references.forEach { methods.add(it.toManagedMethod()) }

    private fun generateCustomProperties() =
        customProperties.forEach { methods.add(it.toManagedMethod()) }

    private fun generateReferencePropertiesMethods() =
        referenceProperties.forEach { methods.add(it.toManagedMethod()) }

    private fun generatePropertyMethods() =
        propertyViews.forEach { methods.add(it.toManagedMethod()) }

    private fun CustomReference.toManagedMethod() = ManagedMethod().apply {
        setName(pluginMethodName, pluginMethodName)
        params = emptyList()
        // trick to avoid generating standard wrapper method
        setIsInheritedWrapperMethod(true)
        isPropertyReadOnly = true
        returns = collectionReturnFormalParameter()
    }

    private fun CustomReferenceProperty.toManagedMethod() = ManagedMethod().apply {
        val originalProperty = this@toManagedMethod.propertyName

        setName(wrapperMethodName, wrapperMethodName)
        originalPropertyName = originalProperty
        propertyName = originalProperty
        params = emptyList()
        setIsInheritedWrapperMethod(true)
        isPropertyReadOnly = true
        returns = stringReturnFormalParameter()
    }

    private fun CustomProperty.toManagedMethod() = ManagedMethod().apply {
        setName(viewMethodName, viewMethodName)
        propertyName = viewPropertyName
        originalPropertyName = viewPropertyName
        params = emptyList()
        setIsInheritedWrapperMethod(true)
        isPropertyReadOnly = true
        returns = stringReturnFormalParameter()
    }

    private fun CustomReference.collectionReturnFormalParameter() = FormalParameter().apply {
        name = "_result"
        modelType = List::class.java
        fullClassName = List::class.java.name
        typeName = "[Contrail$className"
        componentTypeName = "Contrail$className"
        componentClassName = "$apiTypesPackageName.$className"
        isWrapped = true
    }

    private fun stringReturnFormalParameter() = FormalParameter().apply {
        name = "_result"
        modelType = String::class.java
        fullClassName = String::class.java.name
        typeName = "String"
        isWrapped = false
    }

    private fun AdditionalProperty.toManagedMethod() = ManagedMethod().apply {
        setName(methodName, methodName)
        propertyName = this@toManagedMethod.propertyName
        originalPropertyName = this@toManagedMethod.propertyName
        params = emptyList()
        setIsInheritedWrapperMethod(true)
        isPropertyReadOnly = true
        returns = stringReturnFormalParameter()
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
