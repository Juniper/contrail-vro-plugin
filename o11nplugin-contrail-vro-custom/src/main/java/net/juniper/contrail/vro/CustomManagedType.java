/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro;

import com.vmware.o11n.sdk.modeldriven.extension.Interceptor;
import com.vmware.o11n.sdk.modeldrivengen.model.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CustomManagedType extends ManagedType {
    private final ManagedType delegate;

    public CustomManagedType(ManagedType type) {
        this.delegate = type;

        generateRefsMethods();
    }

    public static CustomManagedType wrap(ManagedType type) {
        return new CustomManagedType(type);
    }

    public List<CustomRefsField> getRefsFields() {
        List<Field> fields= Arrays.asList(delegate.getModelClass().getDeclaredFields());
        return fields.stream()
                .filter( field -> field.getName().endsWith("_refs") )
                .map(CustomRefsField::wrapField)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isGenerate() {
        return delegate.isGenerate();
    }

    @Override
    public void setGenerate(boolean generate) {
        delegate.setGenerate(generate);
    }

    @Override
    public boolean isFindable() {
        return delegate.isFindable();
    }

    @Override
    public void setFindable(boolean findable) {
        delegate.setFindable(findable);
    }

    @Override
    public boolean isRootIdPropagated() {
        return delegate.isRootIdPropagated();
    }

    @Override
    public void setRootIdPropagated(boolean rootIdPropagated) {
        delegate.setRootIdPropagated(rootIdPropagated);
    }

    @Override
    public String getFullSuperClassName() {
        return delegate.getFullSuperClassName();
    }

    @Override
    public void setFullSuperClassName(String fullSuperClassName) {
        delegate.setFullSuperClassName(fullSuperClassName);
    }

    @Override
    public String getPackageName() {
        return delegate.getPackageName();
    }

    @Override
    public String getClassName() {
        return delegate.getClassName();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public boolean isSingleton() {
        return delegate.isSingleton();
    }

    @Override
    public void setSingleton(boolean singeton) {
        delegate.setSingleton(singeton);
    }

    @Override
    public boolean isCreateable() {
        return delegate.isCreateable();
    }

    @Override
    public void setCreateable(boolean createable) {
        delegate.setCreateable(createable);
    }

    @Override
    public String getFullClassName() {
        return delegate.getFullClassName();
    }

    @Override
    public void setFullClassName(String className) {
        delegate.setFullClassName(className);
    }

    @Override
    public List<ManagedMethod> getMethods() {
        return delegate.getMethods();
    }

    @Override
    public void setMethods(List<ManagedMethod> methods) {
        delegate.setMethods(methods);
    }

    @Override
    public String getDoc() {
        return delegate.getDoc();
    }

    @Override
    public void setDoc(String doc) {
        delegate.setDoc(doc);
    }

    @Override
    public List<ManagedConstructor> getConstructors() {
        return delegate.getConstructors();
    }

    @Override
    public void setConstructors(List<ManagedConstructor> constructors) {
        delegate.setConstructors(constructors);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public void setName(String name) {
        delegate.setName(name);
    }

    @Override
    public Class<?> getModelClass() {
        return delegate.getModelClass();
    }

    @Override
    public void setModelClass(Class<?> wrappedClass) {
        delegate.setModelClass(wrappedClass);
    }

    @Override
    public Class<? extends Interceptor> getInterceptor() {
        return delegate.getInterceptor();
    }

    @Override
    public void setInterceptor(Class<? extends Interceptor> interceptorClass) {
        delegate.setInterceptor(interceptorClass);
    }

    private void generateRefsMethods() {
        List methods = this.getMethods();

        for (CustomRefsField customField : this.getRefsFields()) {
            String name = "get" + customField.getMethodName();
            String propertyName = Character.toLowerCase(customField.getMethodName().charAt(0)) + customField.getMethodName().substring(1);
            FormalParameter returnParameter = createReturnsFormalParameter();

            ManagedMethod method = new ManagedMethod();
            method.setName(name, name);
            method.setOriginalPropertyName(propertyName);
            method.setPropertyName(propertyName);
            method.setParams(new ArrayList<>());
            method.setIsInheritedWrapperMethod(true);
            method.setPropertyReadOnly(true);
            method.setReturns(returnParameter);

            methods.add(method);
        }
    }

    private FormalParameter createReturnsFormalParameter() {
        FormalParameter returnParameter = new FormalParameter();
        returnParameter.setName("_result");
        returnParameter.setModelType(String.class);
        returnParameter.setFullClassName(String.class.getName());
        returnParameter.setTypeName("String");
        returnParameter.setWrapped(false);
        return returnParameter;
    }
}
