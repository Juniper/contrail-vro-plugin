package net.juniper.contrail.vro.tests.format

import net.juniper.contrail.api.types.AddressType
import net.juniper.contrail.vro.format.PropertyFormatter

import spock.lang.Specification

class PropertyFormatterSpec extends Specification {
    def formatter = PropertyFormatter.INSTANCE

    def "Formatting an AddressType string"() {
        given: "An AddressType"
        def at = new AddressType()

        when: "Formatting to a String"
        formatter.format(at)

        then: "The string is correct"

    }
}
