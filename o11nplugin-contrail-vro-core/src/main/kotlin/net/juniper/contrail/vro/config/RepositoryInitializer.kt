/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import net.juniper.contrail.vro.model.Connection
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
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
class RepositoryInitializerImpl
@Lazy @Autowired constructor(
    private val persister: ConnectionPersister,
    private val repository: ConnectionRepository,
    private val factory: ConnectorFactory
) : RepositoryInitializer {

    override fun initRepository() {
        println("Initializing bean")
        persister.findAll().stream()
                .map { Connection(it, factory.create(it)) }
                .forEach{ repository.addConnection(it) }
    }
}