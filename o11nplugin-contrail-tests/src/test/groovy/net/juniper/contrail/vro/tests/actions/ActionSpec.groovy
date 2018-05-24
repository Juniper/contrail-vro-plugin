/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.actions

import net.juniper.contrail.vro.tests.scripts.ScriptSpec

import static net.juniper.contrail.vro.tests.JsTesterKt.constantsName
import static net.juniper.contrail.vro.tests.JsTesterKt.utilsName

abstract class ActionSpec extends ScriptSpec {

    def setup() {
        engine.addToContext(utilsName)
        engine.addToContext(constantsName)
    }
}

trait ValidationAsserts {
    def validationSuccess(String result) {
        return result == null
    }

    def validationFailure(String result) {
        return result != null
    }

    def validationFailureWith(String result, String expected) {
        return result == expected
    }
}
