/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.actions

import static net.juniper.contrail.vro.config.Actions.isValidVxLANId
import static net.juniper.contrail.vro.config.constants.Constants.VxLANMaxID

class VxLanIdValidationSpec extends ActionSpec implements ValidationAsserts {
    def validateVxLANId = actionFromScript(isValidVxLANId)

    def "null passes validation" () {
        given:
        def id = null

        when:
        def result = engine.invokeFunction(validateVxLANId, id)

        then:
        validationSuccess(result)
    }

    def "negative number fails validation" () {
        given:
        def id = -1

        when:
        def result = engine.invokeFunction(validateVxLANId, id)

        then:
        validationFailure(result)
    }

    def "zero fails validation" () {
        given:
        def id = 0

        when:
        def result = engine.invokeFunction(validateVxLANId, id)

        then:
        validationFailure(result)
    }

    def "one passes validation" () {
        given:
        def id = 1

        when:
        def result = engine.invokeFunction(validateVxLANId, id)

        then:
        validationSuccess(result)
    }

    def "maximum ID passes validation" () {
        given:
        def id = VxLANMaxID

        when:
        def result = engine.invokeFunction(validateVxLANId, id)

        then:
        validationSuccess(result)
    }

    def "value exceeding maximum ID fails validation" () {
        given:
        def id = VxLANMaxID + 1

        when:
        def result = engine.invokeFunction(validateVxLANId, id)

        then:
        validationFailure(result)
    }
}
