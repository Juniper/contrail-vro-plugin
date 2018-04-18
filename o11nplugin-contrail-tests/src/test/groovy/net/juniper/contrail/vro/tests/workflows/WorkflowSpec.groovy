/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import ch.dunes.vso.sdk.IServiceRegistry
import ch.dunes.vso.sdk.api.IPluginFactory
import ch.dunes.vso.sdk.api.IPluginPublisher
import ch.dunes.vso.sdk.endpoints.IEndpointConfiguration
import ch.dunes.vso.sdk.endpoints.IEndpointConfigurationService
import com.vmware.o11n.plugin.sdk.spring.platform.GlobalPluginNotificationHandler
import com.vmware.o11n.sdk.modeldriven.AbstractModelDrivenAdaptor
import com.vmware.o11n.sdk.modeldriven.AnonymousPluginContext
import com.vmware.o11n.sdk.modeldriven.ObjectFactory
import com.vmware.o11n.sdk.modeldriven.PluginContext
import com.vmware.o11n.sdk.modeldriven.impl.DefaultObjectFactory
import com.vmware.o11n.sdk.modeldriven.impl.PluginContextImpl
import com.vmware.o11n.sdk.modeldriven.impl.RuntimeBeanRegisterer
import net.juniper.contrail.api.ApiConnector
import net.juniper.contrail.api.Status
import net.juniper.contrail.vro.ContrailPluginAdaptor
import net.juniper.contrail.vro.base.OneConnectionRepository
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
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import org.springframework.stereotype.Component
import spock.lang.Specification

import java.lang.reflect.Method
import java.nio.file.Paths

import static net.juniper.contrail.vro.schema.SchemaKt.buildSchema
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
        def baseContext = new AnnotationConfigApplicationContext(WorkflowTestConfig.class)

        def configLocations = ["classpath:net/juniper/contrail/vro/test-plugin.xml"] as String[]
        def runtimeConfigPath = "net/juniper/contrail/vro/gen/runtime-config.properties"
        def context = new ClassPathXmlApplicationContext(configLocations, false, baseContext) {
            protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
                super.customizeBeanFactory(beanFactory)
                this.addBeanFactoryPostProcessor(new RuntimeBeanRegisterer(runtimeConfigPath))
            }
        }
        context.refresh()

        def aaaContext = new PluginContextImpl(context, null, null)

        def m = AnonymousPluginContext.class.getDeclaredMethod("init", PluginContext.class)
        m.setAccessible(true)
        m.invoke(null, aaaContext)
    }
}
