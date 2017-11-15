/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.generator

import com.vmware.o11n.sdk.modeldrivengen.code.CodeGeneratorConfig
import com.vmware.o11n.sdk.modeldrivengen.mapping.AbstractMapping
import com.vmware.o11n.sdk.modeldrivengen.template.TemplateEngine
import javax.inject.Inject

class CustomMappingGenerator @Inject constructor(cfg: CodeGeneratorConfig, te: TemplateEngine) :
AbstractGenerator(cfg, te) {
    override val javaFileName = "CustomMapping.java"
    override val templateFileName = "customMapping.ftl"

    fun generateJavaCode(mapping: AbstractMapping?, model: CustomMappingModel) = super.generateJavaCode(mapping, model)
}
