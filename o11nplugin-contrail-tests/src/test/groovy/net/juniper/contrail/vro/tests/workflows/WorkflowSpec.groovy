/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import com.vmware.o11n.sdk.modeldriven.AnonymousPluginContext
import com.vmware.o11n.sdk.modeldriven.ObjectFactory
import com.vmware.o11n.sdk.modeldriven.PluginContext
import com.vmware.o11n.sdk.modeldriven.impl.PluginContextImpl
import net.juniper.contrail.vro.gen.ConnectionManager_Wrapper
import net.juniper.contrail.vro.gen.InstanceIp_Wrapper
import net.juniper.contrail.vro.gen.PolicyEntriesType_Wrapper
import net.juniper.contrail.vro.gen.PolicyRuleType_Wrapper
import net.juniper.contrail.vro.gen.SecurityGroup_Wrapper
import net.juniper.contrail.vro.tests.ScriptTestEngine
import spock.lang.Specification

import java.lang.reflect.Method
import java.nio.file.Paths

import static net.juniper.contrail.vro.workflows.schema.SchemaKt.buildSchema
import static net.juniper.contrail.vro.tests.JsTesterKt.utilsName
import static net.juniper.contrail.vro.workflows.custom.CustomWorkflowsKt.loadCustomWorkflows
import static net.juniper.contrail.vro.config.ProjectInfoKt.globalProjectInfo

abstract class WorkflowSpec extends Specification {
    static def schema = buildSchema(Paths.get(globalProjectInfo.schemaFile))
    static def workflows = loadCustomWorkflows(schema)

    def getWorkflowFunctionOfName = { String name ->
        workflows.find { it.displayName == name }
    }

    def engine = new ScriptTestEngine()

    def connectionManager = Mock(ConnectionManager_Wrapper)
    def instanceIp = Mock(InstanceIp_Wrapper)

    def policyRule = Mock(PolicyRuleType_Wrapper)
    def securityGroup = Mock(SecurityGroup_Wrapper)
    def policyEntries = Mock(PolicyEntriesType_Wrapper)


    def setup() {
        instanceIp.getInternalId() >> "correct SID"
        engine.addToContext(utilsName)
        // add mocked ConnectionManager to the context!!!
        // what about constants ?
    }

    // One way is to mock the context, but it requires us to either:
    //      - mock all of its ctx.extractModelObject() methods, or
    //      - use some ObjectFactory with .extractModelObject() implemented.
    //
    // Another way is to create the context the same way our main app does it
    //      (but where can I find it?)
    def createContext() {
        def mockedContext = Mock(PluginContextImpl)
        def objectFactory = Mock(ObjectFactory)

        mockedContext.getObjectFactory() >> objectFactory

        def m = AnonymousPluginContext.class.getDeclaredMethod("init", PluginContext.class)
        m.setAccessible(true)
        m.invoke(null, mockedContext)
    }
}
