/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import java.util.Properties

private val versionPattern = "(\\d+\\.\\d+)\\.\\d+".toRegex()

val globalProjectInfo = readProjectInfo()

private val String.baseVersion get() =
    replace("-SNAPSHOT", "").let { versionPattern.matchEntire(it)?.groupValues?.getOrNull(1) ?: it }

fun readProjectInfo(): ProjectInfo {
    val props = Properties()
    props.load(ProjectInfo::class.java.getResourceAsStream("/maven.properties"))
    val configRoot = props["project.dir"] as String
    val configPattern = "-config$".toRegex()
    val finalProjectRoot = configRoot.replace(configPattern, "")
    val coreRoot = configRoot.replace(configPattern, "-core")
    val customRoot = configRoot.replace(configPattern, "-custom")
    val packageRoot = configRoot.replace(configPattern, "-package")
    val generatorRoot = configRoot.replace(configPattern, "-generator")
    val genRoot = configRoot.replace(configPattern, "-gen")
    val version = props["dist.version"] as String
    val buildNumber = props["build.number"] as String
    val workflowPackage = props["workflow.package"] as String
    val schemaFile = props["schema.file"] as String
    val baseVersion = version.baseVersion

    return ProjectInfo(
        finalProjectRoot = finalProjectRoot,
        configRoot = configRoot,
        coreRoot = coreRoot,
        customRoot = customRoot,
        packageRoot = packageRoot,
        generatorRoot = generatorRoot,
        genRoot = genRoot,
        version = version,
        baseVersion = baseVersion,
        buildNumber = buildNumber,
        workflowPackage = workflowPackage,
        schemaFile = schemaFile)
}

data class ProjectInfo(
    val finalProjectRoot: String,
    val configRoot: String,
    val coreRoot: String,
    val customRoot: String,
    val packageRoot: String,
    val generatorRoot: String,
    val genRoot: String,
    val version: String,
    val baseVersion: String,
    val buildNumber: String,
    val workflowPackage: String,
    val schemaFile: String)