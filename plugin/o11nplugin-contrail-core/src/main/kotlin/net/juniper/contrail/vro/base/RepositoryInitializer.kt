/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.base

import net.juniper.contrail.vro.model.Connection
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
 * Bean used to automatically read connection information
 * from persistent storage and initialize. This bean should
 * be called after all components of the plugin were configured.
 */
@Component
@Profile("default")
class RepositoryInitializer
@Autowired constructor(
    private val persister: ConnectionPersister,
    private val repository: ConnectionRepository,
    private val factory: ConnectorFactory
) : InitializingBean {
    companion object {
        @JvmStatic val log: Logger = LoggerFactory.getLogger(RepositoryInitializer::class.java)
    }

    override fun afterPropertiesSet() {
        log.debug("Updating connection repository from persistence service.")
        persister.findAll().stream()
            .map { Connection(it, factory.create(it)) }
            .forEach { repository.addConnection(it) }
    }
}