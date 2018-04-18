package net.juniper.contrail.vro.tests.workflows

import com.vmware.o11n.plugin.sdk.spring.platform.GlobalPluginNotificationHandler
import com.vmware.o11n.sdk.modeldriven.impl.DefaultCollectionFactory
import com.vmware.o11n.sdk.modeldriven.impl.DefaultInventoryService
import com.vmware.o11n.sdk.modeldriven.impl.DefaultModelClassResolver
import com.vmware.o11n.sdk.modeldriven.impl.DefaultObjectFactory
import com.vmware.o11n.sdk.modeldriven.impl.DefaultRuntimeConfiguration
import com.vmware.o11n.sdk.modeldriven.impl.PolicyService
import net.juniper.contrail.vro.ContrailPluginAdaptor
import net.juniper.contrail.vro.ContrailPluginFactory
import net.juniper.contrail.vro.base.OneConnectionRepository
import net.juniper.contrail.vro.base.RepositoryInitializer
import net.juniper.contrail.vro.model.Connection
import net.juniper.contrail.vro.model.ConnectionInfo
import org.springframework.beans.factory.config.PropertiesFactoryBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Scope
import org.springframework.core.io.ClassPathResource

@Configuration
@ComponentScan("net.juniper.contrail.vro.base")
class WorkflowTestConfig {

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
