package net.juniper.contrail.vro.tests.workflows

import com.vmware.o11n.plugin.sdk.spring.platform.GlobalPluginNotificationHandler
import net.juniper.contrail.api.ApiConnector
import net.juniper.contrail.vro.base.RepositoryInitializer
import net.juniper.contrail.vro.gen.Connection_Wrapper
import spock.mock.DetachedMockFactory


internal val d = DetachedMockFactory()

val globalPluginNotificationHandlerMock : GlobalPluginNotificationHandler = d.Mock(GlobalPluginNotificationHandler::class.java)

val repositoryInitializerMock : RepositoryInitializer = d.Mock(RepositoryInitializer::class.java)

val apiConnectorMock : ApiConnector = d.Mock(ApiConnector::class.java)