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

    @Throws(IOException::class)
    fun generateJavaCode(mapping: AbstractMapping?, model: CustomMappingModel) {
        this.cfg.javaOutputDir.mkdirs()

        this.generate(model)
    }

    @Throws(IOException::class)
    private fun generate(t: CustomMappingModel) {
        this.createPackageStructure("net.juniper.contrail.vro.generated")
        if (this.cfg.isVerbose) {
            println("generating code for CustomMapping.java")
        }

        val current = java.io.File(".").canonicalPath
        println("Current dir:" + current)

        val template = this.te.getTemplate("customMapping.ftl")
        template.render(t, this.javaFile(t))
    }

    private fun javaFile(t: CustomMappingModel): File {
        val path = "net.juniper.contrail.vro.generated".replace('.', '/')
        val dir = File(this.cfg.javaOutputDir, path)
        return File(dir, "CustomMapping.java")
    }

    private fun createPackageStructure(packageName: String) {
        val path = packageName.replace('.', '/')
        File(this.cfg.javaOutputDir, path).mkdirs()
    }
}