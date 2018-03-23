package net.juniper.contrail.vro.tests.workflows;

import com.vmware.o11n.plugin.sdk.spring.platform.GlobalPluginNotificationHandler;
import net.juniper.contrail.vro.base.ConnectionRepository;
import net.juniper.contrail.vro.base.OneConnectionRepository;
import net.juniper.contrail.vro.base.RepositoryInitializer;
import net.juniper.contrail.vro.gen.Connection_Wrapper;
import net.juniper.contrail.vro.model.Connection;
import net.juniper.contrail.vro.model.ConnectionFinder;
import net.juniper.contrail.vro.model.ConnectionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Configuration
@ComponentScan("net.juniper.contrail.vro.base")
@ComponentScan("net.juniper.contrail.vro.model")
public class WorkflowTestConfig {

    @Autowired()
    ConnectionFinder connectionFinder;

    @Bean
    @Primary
    public GlobalPluginNotificationHandler globalPluginNotificationHandler() {
        return DetachedMocksKt.getGlobalPluginNotificationHandlerMock();
    }

    @Bean
    @Primary
    public RepositoryInitializer repositoryInitializer() {
        return DetachedMocksKt.getRepositoryInitializerMock();
    }

    @Bean
    Connection connection() {
        ConnectionInfo info = new ConnectionInfo("connection name", "host", 8080, "user", "secret");
        return new Connection(info, DetachedMocksKt.getApiConnectorMock());
    }

    @Bean
    @Primary
    public ConnectionRepository connectionRepository() {
        return new OneConnectionRepository(connection());
    }
}
