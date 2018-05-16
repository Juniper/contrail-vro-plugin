/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import com.vmware.o11n.sdk.modeldriven.AnonymousPluginContext
import com.vmware.o11n.sdk.modeldriven.PluginContext
import com.vmware.o11n.sdk.modeldriven.Sid
import com.vmware.o11n.sdk.modeldriven.impl.PluginContextImpl
import com.vmware.o11n.sdk.modeldriven.impl.RuntimeBeanRegisterer
import net.juniper.contrail.vro.gen.Connection_Wrapper
import net.juniper.contrail.vro.tests.ScriptTestEngine
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import spock.lang.Specification

import java.nio.file.Paths

import static net.juniper.contrail.vro.config.ProjectInfoKt.globalProjectInfo
import static net.juniper.contrail.vro.schema.SchemaKt.buildSchema
import static net.juniper.contrail.vro.tests.JsTesterKt.utilsName
import static net.juniper.contrail.vro.workflows.custom.CustomWorkflowsKt.loadCustomWorkflows

abstract class WorkflowSpec extends Specification {
    static def schema = buildSchema(Paths.get(globalProjectInfo.schemaFile))
    static def workflows = loadCustomWorkflows(schema)

    def engine = new ScriptTestEngine()

    def setup() {
        engine.addToContext(utilsName)
    }

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

        def pluginContext = new PluginContextImpl(context, null, null)

        def m = AnonymousPluginContext.class.getDeclaredMethod("init", PluginContext.class)
        m.setAccessible(true)
        m.invoke(null, pluginContext)
    }

    def createDependencies() {
        def connection = new WorkflowTestConfig().connection()
        def conn_wrap = new Connection_Wrapper()
        conn_wrap.__setTarget(connection)
        conn_wrap.setInternalId(Sid.empty().with("Connection", "theConnection"))
        return new Dependencies(conn_wrap)
    }
}
