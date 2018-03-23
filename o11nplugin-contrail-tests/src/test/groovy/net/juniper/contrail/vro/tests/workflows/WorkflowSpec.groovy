/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import ch.dunes.vso.sdk.IServiceRegistry
import ch.dunes.vso.sdk.api.IPluginFactory
import ch.dunes.vso.sdk.api.IPluginPublisher
import ch.dunes.vso.sdk.endpoints.IEndpointConfiguration
import ch.dunes.vso.sdk.endpoints.IEndpointConfigurationService
import com.vmware.o11n.sdk.modeldriven.AnonymousPluginContext
import com.vmware.o11n.sdk.modeldriven.ObjectFactory
import com.vmware.o11n.sdk.modeldriven.PluginContext
import com.vmware.o11n.sdk.modeldriven.impl.DefaultObjectFactory
import com.vmware.o11n.sdk.modeldriven.impl.PluginContextImpl
import net.juniper.contrail.api.ApiConnector
import net.juniper.contrail.api.Status
import net.juniper.contrail.vro.ContrailPluginAdaptor
import net.juniper.contrail.vro.gen.ConnectionManager_Wrapper
import net.juniper.contrail.vro.gen.Connection_Wrapper
import net.juniper.contrail.vro.gen.InstanceIp_Wrapper
import net.juniper.contrail.vro.gen.PolicyEntriesType_Wrapper
import net.juniper.contrail.vro.gen.PolicyRuleType_Wrapper
import net.juniper.contrail.vro.gen.SecurityGroup_Wrapper
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.vro.model.ConnectionInfo
import net.juniper.contrail.vro.tests.ScriptTestEngine
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.context.annotation.AnnotationConfigApplicationContext
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


    static def info = new ConnectionInfo("connection name", "host", 8080, "user", "secret")

    def createContext() {
        def iplugin = Mock(IPluginFactory)
        def conn_w = Mock(Connection_Wrapper)
        def conn_r = Mock(ApiConnector)
        def conn = new Connection(info, conn_r)
        conn_r.create(_) >> Status.success()
        conn_r.read(_) >> Status.success()
        conn_r.update(_) >> Status.success()
        conn_r.delete(_) >> Status.success()
        conn_w.__getTarget() >> conn
        iplugin.find(_, _) >> conn_w

        def listableBeanFactory = new DefaultListableBeanFactory()
        listableBeanFactory.registerSingleton("ObjectFactory", new DefaultObjectFactory())
        listableBeanFactory.registerSingleton("IPluginFactory", iplugin)

        def context = new AnnotationConfigApplicationContext(listableBeanFactory)
        context.refresh()

        def aaaContext = new PluginContextImpl(context, null, null)

        def m = AnonymousPluginContext.class.getDeclaredMethod("init", PluginContext.class)
        m.setAccessible(true)
        m.invoke(null, aaaContext)
    }
}
