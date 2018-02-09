/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.base

import com.vmware.o11n.sdk.modeldriven.impl.DefaultCollectionFactory
import com.vmware.o11n.sdk.modeldriven.impl.DefaultInventoryService
import com.vmware.o11n.sdk.modeldriven.impl.DefaultModelClassResolver
import com.vmware.o11n.sdk.modeldriven.impl.DefaultObjectFactory
import com.vmware.o11n.sdk.modeldriven.impl.DefaultRuntimeConfiguration
import com.vmware.o11n.sdk.modeldriven.impl.PolicyService
import net.juniper.contrail.vro.ContrailPluginAdaptor
import net.juniper.contrail.vro.ContrailPluginFactory
import org.springframework.beans.factory.config.PropertiesFactoryBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportResource
import org.springframework.context.annotation.Scope
import org.springframework.core.io.ClassPathResource

@Configuration
@ComponentScan("net.juniper.contrail.vro.base")
@ImportResource("classpath:com/vmware/o11n/plugin/sdk/spring/pluginEnv.xml")
class SpringConfig {
    @Bean
    fun policyService() = PolicyService()

    @Bean
    fun defaultInventoryService() = DefaultInventoryService()

    @Bean
    fun defaultObjectFactory() = DefaultObjectFactory()

    @Bean
    fun defaultCollectionFactory() = DefaultCollectionFactory()

    @Bean
    fun defaultModelClassResolver() = DefaultModelClassResolver()

    @Bean(name = ["pluginFactory"])
    @Scope("prototype")
    fun contrailPluginFactory() = ContrailPluginFactory()

    @Bean(name = ["pluginAdaptor"])
    fun contrailPluginAdaptor() = ContrailPluginAdaptor()

    @Bean
    fun propertiesFactoryBean(): PropertiesFactoryBean {
        val bean = PropertiesFactoryBean()
        bean.setLocation(ClassPathResource("net/juniper/contrail/vro/gen/runtime-config.properties"))
        return bean
    }

    @Bean
    fun defaultRuntimeConfiguration(): DefaultRuntimeConfiguration {
        val properties = propertiesFactoryBean().`object`
        val runtimeConfiguration = DefaultRuntimeConfiguration()
        runtimeConfiguration.setProperties(properties)
        return runtimeConfiguration
    }
}
