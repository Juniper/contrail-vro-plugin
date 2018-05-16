/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import com.vmware.o11n.plugin.sdk.spring.platform.GlobalPluginNotificationHandler
import net.juniper.contrail.api.ApiConnector
import net.juniper.contrail.vro.base.RepositoryInitializer
import spock.mock.DetachedMockFactory

internal val detachedMockFactory = DetachedMockFactory()

val globalPluginNotificationHandlerMock : GlobalPluginNotificationHandler = detachedMockFactory.Mock(GlobalPluginNotificationHandler::class.java)

val repositoryInitializerMock : RepositoryInitializer = detachedMockFactory.Mock(RepositoryInitializer::class.java)

val apiConnectorMock : ApiConnector = detachedMockFactory.Mock(ApiConnector::class.java)