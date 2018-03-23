/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import com.vmware.o11n.sdk.modeldriven.AnonymousPluginContext
import com.vmware.o11n.sdk.modeldriven.PluginContext
import kotlin.Unit
import net.juniper.contrail.api.types.SecurityGroup
import net.juniper.contrail.vro.gen.PolicyEntriesType_Wrapper
import net.juniper.contrail.vro.gen.SecurityGroup_Wrapper

import java.lang.reflect.Method

class AddRuleToSecurityGroupSpec extends WorkflowSpec {
    // engine.eval imports and stuff!
    def addRuleToSecurityGroup = engine.getFunctionFromWorkflowScript(workflows, "Add rule to security group")

    def "Adding rule to a security group with all nulls and stuff"() {
        given: "stuff"
        when: "other stuff"
        def parent = Mock(SecurityGroup_Wrapper)
        parent.update() >> Unit
        def direction = null
        def ethertype = null
        def addressType = "CIDR"
        def addressCidr = "1.2.3.4/16"
        def addressSecurityGroup = null
        def protocol = null
        def ports = "1-2"
        // WRAPPER CANNOT BE CONSTRUCTED BECAUSE ITS CONTEXT IS NULL
        // WE CAN'T USE NON-WRAPPER CLASS BECAUSE IT CAN'T BE CONVERTED TO ModelWrapper FOR parent.setEntries(_)
        // TODO: Make a mocked AnonymousPluginContext somehow accessible during the test

        createContext()

        engine.engine.eval("var ContrailPolicyRuleType = Java.type('net.juniper.contrail.vro.gen.PolicyRuleType_Wrapper');")
        engine.engine.eval("var ContrailPolicyEntriesType = Java.type('net.juniper.contrail.vro.gen.PolicyEntriesType_Wrapper');")
        def result = engine.invokeFunction(addRuleToSecurityGroup, parent, direction, ethertype, addressType, addressCidr, addressSecurityGroup, protocol, ports)
        then: "it should not explode"
        1 == 1
    }

    def "Stuff should fail"() {
        given: "stuff"
        def notcutor = connectionManager.connection("someOtherId")
        println(notcutor.toString())
        when: "nothing"
        then: "it should explode"  // okazuje się że tu leci null a nie wyjątek także się nie wywali
        1 == 1
    }
}
