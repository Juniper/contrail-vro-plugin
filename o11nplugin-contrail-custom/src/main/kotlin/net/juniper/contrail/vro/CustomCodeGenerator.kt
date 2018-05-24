/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro

import com.vmware.o11n.sdk.modeldrivengen.code.CodeGeneratorConfig
import com.vmware.o11n.sdk.modeldrivengen.code.DefaultCodeGeneratorConfig
import com.vmware.o11n.sdk.modeldrivengen.code.DefaultPluginCodeGenerator
import com.vmware.o11n.sdk.modeldrivengen.code.DefaultRuntimeConfigurationGenerator
import com.vmware.o11n.sdk.modeldrivengen.code.PluginCodeGenerator
import com.vmware.o11n.sdk.modeldrivengen.code.RuntimeConfigurationGenerator
import com.vmware.o11n.sdk.modeldrivengen.mapping.AbstractMapping
import com.vmware.o11n.sdk.modeldrivengen.model.Plugin
import net.juniper.contrail.vro.config.packageToPath
import java.nio.file.Files
import java.nio.file.Paths
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomCodeGenerator @Inject constructor(
    private val codeDelegate: DefaultPluginCodeGenerator,
    private val configDelegate: DefaultRuntimeConfigurationGenerator,
    config: CodeGeneratorConfig,
    mapping: AbstractMapping
) : PluginCodeGenerator, RuntimeConfigurationGenerator {
    private val generate: Boolean = generationRequired(config, mapping)

    init {
        if (!generate && config is DefaultCodeGeneratorConfig)
            config.isVerbose = false
    }

    private fun generationRequired(config: CodeGeneratorConfig, mapping: AbstractMapping): Boolean {
        val vsoXmlPath = Paths.get(config.javaOutputDir.absolutePath, config.basePackage.packageToPath(), "runtime-config.properties")
        if (! Files.exists(vsoXmlPath)) return true
        val mappingPath = Paths.get(mapping.javaClass.protectionDomain.codeSource.location.toURI())
        val vsoTime = Files.getLastModifiedTime(vsoXmlPath)
        val mappingTime = Files.getLastModifiedTime(mappingPath)
        return mappingTime > vsoTime
    }

    override fun generateJavaCode(mapping: AbstractMapping, plugin: Plugin) {
        if (generate)
            codeDelegate.generateJavaCode(mapping, plugin)
        else
            println("[INFO] Skipped generation of wrapper classes.")
    }

    override fun generateRuntimeConfiguration(mapping: AbstractMapping, plugin: Plugin) {
        if (generate)
            configDelegate.generateRuntimeConfiguration(mapping, plugin)
        else
            println("[INFO] Skipped generation of runtime configuration.")
    }
}