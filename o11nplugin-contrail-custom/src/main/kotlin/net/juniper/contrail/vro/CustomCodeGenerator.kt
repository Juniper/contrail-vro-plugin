package net.juniper.contrail.vro

import com.vmware.o11n.sdk.modeldrivengen.code.CodeGeneratorConfig
import com.vmware.o11n.sdk.modeldrivengen.code.DefaultPluginCodeGenerator
import com.vmware.o11n.sdk.modeldrivengen.code.DefaultRuntimeConfigurationGenerator
import com.vmware.o11n.sdk.modeldrivengen.code.DefaultVsoXmlGenerator
import com.vmware.o11n.sdk.modeldrivengen.code.PluginCodeGenerator
import com.vmware.o11n.sdk.modeldrivengen.code.RuntimeConfigurationGenerator
import com.vmware.o11n.sdk.modeldrivengen.code.VsoXmlGenerator
import com.vmware.o11n.sdk.modeldrivengen.mapping.AbstractMapping
import com.vmware.o11n.sdk.modeldrivengen.model.DefaultModelMerger
import com.vmware.o11n.sdk.modeldrivengen.model.ModelMerger
import com.vmware.o11n.sdk.modeldrivengen.model.Plugin
import java.nio.file.Files
import java.nio.file.Paths
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomCodeGenerator @Inject constructor(
    private val codeDelegate: DefaultPluginCodeGenerator,
    private val vsoXmlDelegate: DefaultVsoXmlGenerator,
    private val configDelegate: DefaultRuntimeConfigurationGenerator,
    private val mergerDelegate: DefaultModelMerger,
    config: CodeGeneratorConfig,
    mapping: AbstractMapping
) : PluginCodeGenerator, VsoXmlGenerator, RuntimeConfigurationGenerator, ModelMerger {
    private val generate: Boolean = generationRequired(config, mapping)

    private fun generationRequired(config: CodeGeneratorConfig, mapping: AbstractMapping): Boolean {
        val vsoXmlPath = config.vsoXmlFile.toPath()
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

    override fun generateVsoXml(mapping: AbstractMapping, plugin: Plugin) {
        if (generate)
            vsoXmlDelegate.generateVsoXml(mapping, plugin)
        else
            println("[INFO] Skipped generation of vso.xml.")
    }

    override fun generateRuntimeConfiguration(mapping: AbstractMapping, plugin: Plugin) {
        if (generate)
            configDelegate.generateRuntimeConfiguration(mapping, plugin)
        else
            println("[INFO] Skipped generation of runtime configuration.")
    }

    override fun merge(mapping: AbstractMapping, plugin: Plugin) {
        if (generate)
            mergerDelegate.merge(mapping, plugin)
        else
            println("[INFO] Skipped merging model.")
    }
}