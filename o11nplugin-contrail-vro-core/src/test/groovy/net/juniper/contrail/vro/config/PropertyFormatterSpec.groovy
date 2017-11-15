/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.config

import net.juniper.contrail.api.ApiConnectorFactory
import net.juniper.contrail.api.ApiPropertyBase
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.ReferencePropertyFormatter
import spock.lang.Shared
import spock.lang.Specification

class PropertyFormatterSpec extends Specification{
    @Shared def connector = ApiConnectorFactory.build("10.10.1.237", 8082)
            .authServer("keystone", "http://10.10.1.237:5000/v2.0")
            .credentials("admin", "secret123")
            .tenantName("admin")
    @Shared  def formatter = new ReferencePropertyFormatter()

    def "Formatter return empty string for null object"() {
        when:
            def result = formatter.convert(null)
        then:
            result == ""
    }
}
