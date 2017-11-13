/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import com.vmware.o11n.sdk.modeldrivengen.code.DefaultCodeGeneratorConfig
import com.vmware.o11n.sdk.modeldrivengen.template.fm.FreemarkerTemplateEngine
import java.io.File
import java.util.Properties

object Generator {
    @JvmStatic fun main(args: Array<String>) {
        val projectInfo = readProjectInfo()
        val templateEngine = FreemarkerTemplateEngine("/net/juniper/contrail/vro/templates")

        val customMappingGeneratorConfig = DefaultCodeGeneratorConfig()
        customMappingGeneratorConfig.isVerbose = true
        customMappingGeneratorConfig.javaOutputDir = File(projectInfo.coreRoot + "/src/main/kotlin/") // temporary

        val customMappingGenerator = CustomMappingGenerator(
                customMappingGeneratorConfig,
                templateEngine
        )

        val customMappingModel = generateCustomMappingModel()
        customMappingGenerator.generateJavaCode(null, customMappingModel)

        val findersGeneratorConfig = DefaultCodeGeneratorConfig()
        findersGeneratorConfig.isVerbose = true
        findersGeneratorConfig.javaOutputDir = File(projectInfo.coreRoot + "/src/main/kotlin/")

        val findersGenerator = FindersGenerator(
                findersGeneratorConfig,
                templateEngine
        )
        val findersModel = generateFindersModel()
        findersGenerator.generateJavaCode(null, findersModel)

        val relationsGeneratorConfig = DefaultCodeGeneratorConfig()
        relationsGeneratorConfig.isVerbose = true
        relationsGeneratorConfig.javaOutputDir = File(projectInfo.coreRoot + "/src/main/kotlin/")

        val relationsGenerator = RelationsGenerator(
                relationsGeneratorConfig,
                templateEngine
        )

        val relationsModel = generateRelationsModel()
        relationsGenerator.generateJavaCode(null, relationsModel)
    }
}

private fun readProjectInfo(): ProjectInfo {
    val props = Properties()
    props.load(Generator::class.java.getResourceAsStream("/maven.properties"))
    val generatorRoot = props["project.dir"] as String
    val generatorPattern = "-generator$".toRegex()
    val staticRoot = "$generatorRoot/src/main/static"
    val finalProjectRoot = generatorRoot.replace(generatorPattern, "")
    val coreRoot = generatorRoot.replace(generatorPattern, "-core")
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
