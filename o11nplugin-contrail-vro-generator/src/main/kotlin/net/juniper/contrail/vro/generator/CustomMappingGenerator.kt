/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import com.vmware.o11n.sdk.modeldrivengen.code.CodeGeneratorConfig
import com.vmware.o11n.sdk.modeldrivengen.mapping.AbstractMapping
import com.vmware.o11n.sdk.modeldrivengen.template.TemplateEngine
import java.io.File
import java.io.IOException
import javax.inject.Inject

class CustomMappingGenerator @Inject constructor(private val cfg: CodeGeneratorConfig, private val te: TemplateEngine) {

    private val javaFileName = "CustomMapping.java"
    private val templateFileName = "customMapping.ftl"

    @Throws(IOException::class)
    fun generateJavaCode(mapping: AbstractMapping?, model: CustomMappingModel) {
        cfg.javaOutputDir.mkdirs()

        generate(model)
    }

    @Throws(IOException::class)
    private fun generate(t: CustomMappingModel) {
        createPackageStructure(generatedPackageName)
        if (cfg.isVerbose) {
            println("generating code for $javaFileName")
        }

        val current = java.io.File(".").canonicalPath
        println("Current dir:" + current)

        val template = te.getTemplate(templateFileName)
        template.render(t, javaFile())
    }

    private fun javaFile(): File {
        val path = generatedPackageName.replace('.', '/')
        val dir = File(cfg.javaOutputDir, path)
        return File(dir, javaFileName)
    }

    private fun createPackageStructure(packageName: String) {
        val path = packageName.replace('.', '/')
        File(cfg.javaOutputDir, path).mkdirs()
    }
}