/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro;

import org.apache.commons.lang.WordUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

public class CustomRefsField {
    private String name;
    private String methodName;
    private String returnTypeName;
    private String refObjectType;

    public static CustomRefsField wrapField(Field field) {
        CustomRefsField customField = new CustomRefsField();
        customField.name = field.getName();
        ParameterizedType fieldGenericType = (ParameterizedType) field.getGenericType();
        customField.returnTypeName = fieldGenericType.getActualTypeArguments()[0].toString();
        customField.methodName = generateMethodName(customField.name);
        customField.refObjectType = customField.methodName.replace("BackRefs", "");
        return customField;
    }
    private static String generateMethodName(String name) {
        String notSeparatedName = name;
        if (!name.endsWith("back_refs")) {
            notSeparatedName = name.replace("_refs", "");
        }

        String separatedName = notSeparatedName.replace("_", " ").toLowerCase();
        return WordUtils.capitalize(separatedName).replace(" ", "");
    }

    public String getName() {
        return name;
    }

    public String getReturnTypeName() {
        return returnTypeName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getRefObjectType() {
        return refObjectType;
    }
}
