/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.scripts

import jdk.nashorn.api.scripting.ScriptObjectMirror
import net.juniper.contrail.vro.config.Config
import net.juniper.contrail.vro.config.ConfigKt
import net.juniper.contrail.vro.config.DefaultConfigKt
import net.juniper.contrail.vro.tests.ScriptTestEngine
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Paths

import static net.juniper.contrail.vro.config.ProjectInfoKt.globalProjectInfo
import static net.juniper.contrail.vro.schema.SchemaKt.buildSchema
import static net.juniper.contrail.vro.workflows.custom.Custom.loadCustomActions
import static net.juniper.contrail.vro.workflows.custom.Custom.loadCustomWorkflows

abstract class ScriptSpec extends Specification {
    private static def dummyVersion = "1.0"
    private static def dummyPackage = "contrail"
    private static def actions = loadCustomActions(dummyVersion, dummyPackage)
    private static def schema = buildSchema(Paths.get(globalProjectInfo.schemaFile))
    private static def workflows = loadCustomWorkflows(schema, ConfigKt.defaultConfig)

    @Shared def engine = new ScriptTestEngine()

    def actionFromScript(String actionName) {
        return engine.getFunctionFromActionScript(actions, actionName)
    }

    def workflowFromScript(String scriptName) {
        return engine.getFunctionFromWorkflowScript(workflows, scriptName)
    }

    def invokeFunction(String name, Object... args) {
        engine.invokeFunction(name, args)
    }

    def invokeAction(String name, Object... args) {
        return convertResult(invokeFunction(name, args))
    }

    private static def convertResult(Object result) {
        if (result instanceof ScriptObjectMirror) {
            ScriptObjectMirror mirror = (ScriptObjectMirror) result
            if (mirror.isArray()) {
                List<Object> list = new ArrayList<>()
                for (Map.Entry<String, Object> entry : mirror.entrySet())
                    list.add(convertResult(entry.getValue()))
                return list
            } else {
                Map<String, Object> map = new HashMap<>()
                for (Map.Entry<String, Object> entry : mirror.entrySet())
                    map.put(entry.getKey(), convertResult(entry.getValue()))
                return map
            }
        } else {
            return result
        }
    }
}
