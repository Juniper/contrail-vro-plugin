/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import java.io.File
import java.util.Properties

object Generator {
    @JvmStatic fun main(args: Array<String>) {
        val info = readProjectInfo()
        copyVSOFile(info)
    }
}

private fun readProjectInfo(): ProjectInfo {
    val props = Properties()
    props.load(Generator::class.java.getResourceAsStream("/maven.properties"))
    val generatorRoot = props["project.dir"] as String
    val generatorPattern = "-generator$".toRegex()
    val staticRoot = "$generatorRoot/src/main/static"
    val finalProjectRoot = generatorRoot.replace(generatorPattern, "")
    val coreRoot = generatorRoot.replace(generatorPattern, "core")
    val version = props["project.version"] as String
    val baseVersion = version.replace("-SNAPSHOT", "")

    return ProjectInfo(
        generatorRoot = generatorRoot,
        finalProjectRoot = finalProjectRoot,
        coreRoot = coreRoot,
        staticRoot = staticRoot,
        version = version,
        baseVersion = baseVersion)
}

data class ProjectInfo(
    val generatorRoot: String,
    val finalProjectRoot: String,
    val coreRoot: String,
    val staticRoot: String,
    val version: String,
    val baseVersion: String)

fun copyVSOFile(info: ProjectInfo) {
    val vsoFilename = "vso.xml"

    val source = "${info.staticRoot}/$vsoFilename"
    val target = "${info.finalProjectRoot}/src/main/dar/VSO-INF/$vsoFilename"

    println("Copying vso.xml file.")
    File(source).copyTo(File(target), overwrite = true)
}
