/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

val generatedPackageName = "net.juniper.contrail.vro.generated"

val generatedSourcesRoot = "/target/generated-sources"
val generatedTestsRoot = "/target/generated-tests"
val templatesDirName = "templates"
val templatesInClassPath = "/$templatesDirName"
val templatesInResourcesPath = "src/main/resources/$templatesDirName"

val editWarningMessage: String =
"""
/***********************************************************
 *               GENERATED FILE - DO NOT EDIT              *
 ***********************************************************/
"""