/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.actions

import net.juniper.contrail.vro.tests.ScriptTestEngine
import spock.lang.Specification
import static net.juniper.contrail.vro.workflows.custom.CustomActionsKt.loadCustomActions
import static net.juniper.contrail.vro.tests.JsTesterKt.utilsName

abstract class ActionSpec extends Specification {
    static def dummyVersion = "1.0"
    static def dummyPackage = "contrail"
    static def actions = loadCustomActions(dummyVersion, dummyPackage)
    def engine = new ScriptTestEngine()

    def setup() {
        engine.addToContext(utilsName)
    }
}
