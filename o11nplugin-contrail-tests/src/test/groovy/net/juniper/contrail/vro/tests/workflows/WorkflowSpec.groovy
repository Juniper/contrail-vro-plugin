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
import net.juniper.contrail.vro.gen.Constants_Wrapper
import net.juniper.contrail.vro.gen.Utils_Wrapper
import net.juniper.contrail.vro.model.Constants
import net.juniper.contrail.vro.model.Utils
import net.juniper.contrail.vro.tests.ScriptTestEngine
import net.juniper.contrail.vro.tests.scripts.ScriptSpec
import org.spockframework.mock.MockUtil
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Paths

import static net.juniper.contrail.vro.config.ProjectInfoKt.globalProjectInfo
import static net.juniper.contrail.vro.schema.SchemaKt.buildSchema
import static net.juniper.contrail.vro.tests.JsTesterKt.constantsName
import static net.juniper.contrail.vro.tests.JsTesterKt.utilsName
import static net.juniper.contrail.vro.workflows.custom.Custom.loadCustomWorkflows

abstract class WorkflowSpec extends ScriptSpec {
    @Shared Dependencies dependencies
    def connectorMock = DetachedMocksKt.apiConnectorMock
    def mockUtil = new MockUtil()

    def setupSpec() {
        createContext()
        engine.engine.eval(setupScript)
        def ctx = AnonymousPluginContext.get()
        def utils = createUtils(ctx)
        def constants = createConstants(ctx)
        engine.addToContext(utilsName, utils)
        engine.addToContext(constantsName, constants)
        dependencies = createDependencies(utils, ctx)
    }

    def setup() {
        mockUtil.attachMock(connectorMock, this)
    }

    // We need the Spring Context to automatically load the converters for model- and plugin-objects
    private static def createContext() {
        def baseContext = new AnnotationConfigApplicationContext(WorkflowTestConfig.class)

        def configLocations = ["classpath:net/juniper/contrail/vro/test-plugin.xml"] as String[]
        def runtimeConfigPath = "net/juniper/contrail/vro/gen/runtime-config.properties"
        def context = new ClassPathXmlApplicationContext(configLocations, false, baseContext) {
            protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
                super.customizeBeanFactory(beanFactory)
                addBeanFactoryPostProcessor(new RuntimeBeanRegisterer(runtimeConfigPath))
            }
        }
        context.refresh()

        def pluginContext = new PluginContextImpl(context, null, null)

        // the init method is private, so we can only access it using reflection
        def m = AnonymousPluginContext.class.getDeclaredMethod("init", PluginContext.class)
        m.setAccessible(true)
        m.invoke(null, pluginContext)
    }

    private static def createUtils(ctx) {
        def utils = new Utils_Wrapper()
        def innerUtils = new Utils()
        utils.setContext(ctx)
        utils.__setTarget(innerUtils)
        return utils
    }

    private static def createConstants(ctx) {
        def constants = new Constants_Wrapper()
        def innerConstants = new Constants()
        constants.setContext(ctx)
        constants.__setTarget(innerConstants)
        return constants
    }

    private static def createDependencies(Utils_Wrapper utils, PluginContext ctx) {
        def connection = new WorkflowTestConfig().connection()
        def conn_wrap = new Connection_Wrapper()
        conn_wrap.__setTarget(connection)
        conn_wrap.setInternalId(Sid.empty().with("Connection", "theConnection"))
        conn_wrap.setContext(ctx)
        return new Dependencies(conn_wrap, utils)
    }

    private final static String setupScript = buildWrapperDefinition(
        "ConfigRoot",
        "ActionListType",
        "AllocationPoolType",
        "FloatingIp",
        "IpamSubnets",
        "IpamSubnetType",
        "PolicyEntriesType",
        "PolicyRuleType",
        "PortTuple",
        "SecurityGroup",
        "SequenceType",
        "ServiceInstance",
        "ServiceInstanceType",
        "ServiceInstanceInterfaceType",
        "ServiceInterfaceTag",
        "ServiceTemplate",
        "ServiceTemplateType",
        "ServiceTemplateInterfaceType",
        "SubnetType",
        "VirtualMachineInterfacePropertiesType",
        "VirtualNetworkPolicyType",
        "VnSubnetsType",
        "SubnetListType",
        "FirewallServiceGroupType",
        "FirewallServiceType",
        "FirewallRule",
        "FirewallRuleMatchTagsType",
        "FirewallSequence",
        "Tag",
        "TagType"
    )

    private static String buildWrapperDefinition(String... types) {
        return types.collect { toWrapperDefinitionScript(it) }.join("\n")
    }

    private static String toWrapperDefinitionScript(String type) {
        return "var Contrail$type = Java.type('net.juniper.contrail.vro.gen.${type}_Wrapper');"
    }

}
