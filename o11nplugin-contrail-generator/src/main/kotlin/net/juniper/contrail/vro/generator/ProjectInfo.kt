/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import java.util.Properties

fun readProjectInfo(): ProjectInfo {
    val props = Properties()
    props.load(ProjectInfo::class.java.getResourceAsStream("/maven.properties"))
    val generatorRoot = props["project.dir"] as String
    val generatorPattern = "-generator$".toRegex()
    val staticRoot = "$generatorRoot/src/main/static"
    val finalProjectRoot = generatorRoot.replace(generatorPattern, "")
    val coreRoot = generatorRoot.replace(generatorPattern, "-core")
    val customRoot = generatorRoot.replace(generatorPattern, "-custom")
    val packageRoot = generatorRoot.replace(generatorPattern, "-package")
    val version = props["project.version"] as String
    val buildNumber = props["build.number"] as String
    val workflowsPackageName = props["workflows.packageName"] as String
    val baseVersion = version.replace("-SNAPSHOT", "")

    return ProjectInfo(
        generatorRoot = generatorRoot,
        finalProjectRoot = finalProjectRoot,
        coreRoot = coreRoot,
        customRoot = customRoot,
        packageRoot = packageRoot,
        staticRoot = staticRoot,
        version = version,
        baseVersion = baseVersion,
        buildNumber = buildNumber,
        workflowsPackageName = workflowsPackageName)
}

data class ProjectInfo(
    val generatorRoot: String,
    val finalProjectRoot: String,
    val coreRoot: String,
    val customRoot: String,
    val packageRoot: String,
    val staticRoot: String,
    val version: String,
    val baseVersion: String,
    val buildNumber: String,
    val workflowsPackageName: String)