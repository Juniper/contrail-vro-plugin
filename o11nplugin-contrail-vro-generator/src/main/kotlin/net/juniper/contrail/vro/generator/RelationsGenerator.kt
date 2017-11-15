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

class RelationsGenerator @Inject constructor(cfg: CodeGeneratorConfig, te: TemplateEngine) :
        AbstractGenerator(cfg, te) {
    override val javaFileName = "Relations.kt"
    override val templateFileName = "relations.ftl"

    fun generateJavaCode(mapping: AbstractMapping?, model: RelationsModel) = super.generateJavaCode(mapping, model)
}
