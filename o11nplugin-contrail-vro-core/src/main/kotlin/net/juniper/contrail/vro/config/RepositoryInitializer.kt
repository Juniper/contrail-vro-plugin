/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import net.juniper.contrail.vro.model.Connection
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
 * Service performing initialization of the plugin state.
 */
interface RepositoryInitializer {
    fun initRepository()
}

/**
 * Bean used to automatically read connection information
 * from persistent storage and initialize. This bean should
 * be called after all components of the plugin were configured.
 */
@Lazy @Component
@Profile("default")
class RepositoryInitializerImpl
@Lazy @Autowired constructor(
    private val persister: ConnectionPersister,
    private val repository: ConnectionRepository,
    private val factory: ConnectorFactory
) : RepositoryInitializer {
    companion object {
        @JvmStatic val log: Logger = LoggerFactory.getLogger(RepositoryInitializerImpl::class.java)
    }

    override fun initRepository() {
        log.debug("Updating connection repository from persistence service.")
        persister.findAll().stream()
            .map { Connection(it, factory.create(it)) }
            .forEach { repository.addConnection(it) }
    }
}