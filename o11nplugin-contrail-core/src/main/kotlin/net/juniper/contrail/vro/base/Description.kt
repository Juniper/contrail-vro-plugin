/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.base

/**
 * Annotation used for extracting model class description at runtime.
 * It should be used instead of JavaDoc for object exported to vRO.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Description(val value: String)