/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import com.vmware.o11n.sdk.modeldrivengen.code.CodeGeneratorConfig
import com.vmware.o11n.sdk.modeldrivengen.mapping.AbstractMapping
import com.vmware.o11n.sdk.modeldrivengen.template.TemplateEngine
import java.io.File
import java.io.IOException

abstract class AbstractGenerator(private val cfg: CodeGeneratorConfig, private val te: TemplateEngine) {

    abstract val javaFileName: String
    abstract val templateFileName: String

    @Throws(IOException::class)
    protected fun generateJavaCode(mapping: AbstractMapping?, model: GenericModel) {
        cfg.javaOutputDir.mkdirs()

        generate(model)
    }

    @Throws(IOException::class)
    private fun generate(t: GenericModel) {
        createPackageStructure(generatedPackageName)
        if (cfg.isVerbose) {
            println("generating code for $javaFileName")
        }

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
