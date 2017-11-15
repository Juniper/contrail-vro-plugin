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

class RelationsGenerator @Inject constructor(private val cfg: CodeGeneratorConfig, private val te: TemplateEngine) {

    @Throws(IOException::class)
    fun generateJavaCode(mapping: AbstractMapping?, model: RelationsModel) {
        this.cfg.javaOutputDir.mkdirs()

        this.generate(model)
    }

    @Throws(IOException::class)
    private fun generate(t: RelationsModel) {
        this.createPackageStructure(generatedPackageName)
        if (this.cfg.isVerbose) {
            println("generating code for Relations.kt")
        }

        val current = java.io.File(".").canonicalPath
        println("Current dir:" + current)

        val template = this.te.getTemplate("relations.ftl")
        template.render(t, this.javaFile())
    }

    private fun javaFile(): File {
        val path = generatedPackageName.packageToPath()
        val dir = File(this.cfg.javaOutputDir, path)
        return File(dir, "Relations.kt")
    }

    private fun createPackageStructure(packageName: String) {
        val path = packageName.packageToPath()
        File(this.cfg.javaOutputDir, path).mkdirs()
    }
}