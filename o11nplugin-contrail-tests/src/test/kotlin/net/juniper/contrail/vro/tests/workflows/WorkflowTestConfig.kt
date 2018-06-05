/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import com.vmware.o11n.plugin.sdk.spring.platform.GlobalPluginNotificationHandler
import com.vmware.o11n.sdk.modeldriven.ObjectFactory
import com.vmware.o11n.sdk.modeldriven.impl.DefaultObjectFactory
import net.juniper.contrail.vro.base.RepositoryInitializer
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.vro.model.ConnectionInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
@ComponentScan("net.juniper.contrail.vro.base")
class WorkflowTestConfig {

    @Autowired lateinit var factoryDelegate: DefaultObjectFactory

    @Bean
    @Primary
    fun objectFactory() : ObjectFactory =
        TestObjectFactory(factoryDelegate)

    @Bean
    @Primary
    fun globalPluginNotificationHandler() : GlobalPluginNotificationHandler =
        globalPluginNotificationHandlerMock

    @Bean
    @Primary
    fun repositoryInitializer() : RepositoryInitializer =
        repositoryInitializerMock

    @Bean
    fun connection() : Connection {
        val info = ConnectionInfo("connection name", "host", 8080, "user", "secret")
        return Connection(info, apiConnectorMock)
    }

    @Bean
    @Primary
    fun connectionRepository() = OneConnectionRepository(connection())
}
