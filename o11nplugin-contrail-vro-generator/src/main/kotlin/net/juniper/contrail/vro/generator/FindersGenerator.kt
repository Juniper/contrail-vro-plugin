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

class FindersGenerator @Inject constructor(cfg: CodeGeneratorConfig, te: TemplateEngine) :
        AbstractGenerator(cfg, te) {
    override val javaFileName = "Finders.kt"
    override val templateFileName = "finders.ftl"

    fun generateJavaCode(mapping: AbstractMapping?, model: FindersModel) = super.generateJavaCode(mapping, model)
}
