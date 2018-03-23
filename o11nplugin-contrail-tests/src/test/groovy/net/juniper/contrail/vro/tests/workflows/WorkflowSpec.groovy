/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import ch.dunes.vso.sdk.IServiceRegistry
import ch.dunes.vso.sdk.api.IPluginPublisher
import ch.dunes.vso.sdk.endpoints.IEndpointConfiguration
import ch.dunes.vso.sdk.endpoints.IEndpointConfigurationService
import com.vmware.o11n.sdk.modeldriven.AnonymousPluginContext
import com.vmware.o11n.sdk.modeldriven.ObjectFactory
import com.vmware.o11n.sdk.modeldriven.PluginContext
import com.vmware.o11n.sdk.modeldriven.impl.PluginContextImpl
import net.juniper.contrail.vro.ContrailPluginAdaptor
import net.juniper.contrail.vro.gen.ConnectionManager_Wrapper
import net.juniper.contrail.vro.gen.InstanceIp_Wrapper
import net.juniper.contrail.vro.gen.PolicyEntriesType_Wrapper
import net.juniper.contrail.vro.gen.PolicyRuleType_Wrapper
import net.juniper.contrail.vro.gen.SecurityGroup_Wrapper
import net.juniper.contrail.vro.model.ConnectionInfo
import net.juniper.contrail.vro.tests.ScriptTestEngine
import spock.lang.Specification

import java.lang.reflect.Method
import java.nio.file.Paths

import static net.juniper.contrail.vro.base.ConstantsKt.getHOST
import static net.juniper.contrail.vro.base.ConstantsKt.getNAME
import static net.juniper.contrail.vro.base.ConstantsKt.getPASSWORD
import static net.juniper.contrail.vro.base.ConstantsKt.getPORT
import static net.juniper.contrail.vro.base.ConstantsKt.getUSER
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

    def setup() {
        engine.addToContext(utilsName)
    }

    // Copied from SpringContextSpec
    def createContext() {
        def pluginAdaptor = new ContrailPluginAdaptor()
        def pluginPublisher = Mock(IPluginPublisher)
        def configService = mockConfigurationService()
        def registry = mockServiceRegistry(configService)
        pluginAdaptor.setServiceRegistry(registry)
        pluginAdaptor.setPluginPublisher(pluginPublisher)

        def context = pluginAdaptor.getApplicationContext()

        def aaaContext = new PluginContextImpl(context, null, null)

        def m = AnonymousPluginContext.class.getDeclaredMethod("init", PluginContext.class)
        m.setAccessible(true)
        m.invoke(null, aaaContext)
    }



    def mockServiceRegistry(configurationService) {
        def serviceRegistry = Mock(IServiceRegistry)
        serviceRegistry.getService("ch.dunes.vso.sdk.endpoints.IEndpointConfigurationService") >> configurationService

        return serviceRegistry
    }

    static def info = new ConnectionInfo("connection name", "host", 8080, "user", "secret")

    def mockConfigurationService() {
        def configurationService = Mock(IEndpointConfigurationService)
        def config = Mock(IEndpointConfiguration)

        config.getString(NAME) >> "other name"
        config.getString(HOST) >> info.hostname
        config.getAsInteger(PORT) >> info.port
        config.getString(USER) >> info.username
        config.getPassword(PASSWORD) >> info.password

        configurationService.getEndpointConfiguration(info.name) >> config
        configurationService.endpointConfigurations >> [config]
        configurationService.newEndpointConfiguration(_) >> config

        return configurationService
    }
}
