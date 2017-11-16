/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import freemarker.cache.ClassTemplateLoader
import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

data class GeneratorConfig(val outputDir: Path, val verbose: Boolean = true) {
    constructor(baseDir: Path, packageName: String, verbose: Boolean = true) :
        this(baseDir.append(packageName.packageToPath()), verbose)
}

class GeneratorEngine(private val config: GeneratorConfig, private val freemarker: Configuration) {

    constructor(config: GeneratorConfig, templatePath: String): this(config, defaultFreemarkerConfig(templatePath))

    @Throws(IOException::class)
    fun generate(model: GenericModel, fileName: String, templateName: String) {
        if (config.verbose)
            println("[INFO] Generating code for $fileName using template $templateName")
        createPackageStructure()
        val template = freemarker.getTemplate(templateName)
        val filePath = config.outputDir.append(fileName)
        FileWriter(filePath.toFile()). use { template.process(model, it) }
    }

    @Throws(IOException::class)
    fun generate(model: GenericModel, fileName: String) =
        generate(model, fileName, fileName.toTemplateName())

    private fun String.toTemplateName(): String =
        decapitalize().replace("\\..+?$".toRegex(), ".ftl")

    private fun createPackageStructure() =
        Files.createDirectories(config.outputDir)
}

fun defaultFreemarkerConfig(templatePath: String): Configuration {
    val configuration = Configuration(Configuration.VERSION_2_3_21)
    configuration.defaultEncoding = "UTF-8"
    configuration.outputEncoding = "UTF-8"
    configuration.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
    val loader = ClassTemplateLoader(GeneratorConfig::class.java, templatePath)
    configuration.templateLoader = loader
    return configuration
}