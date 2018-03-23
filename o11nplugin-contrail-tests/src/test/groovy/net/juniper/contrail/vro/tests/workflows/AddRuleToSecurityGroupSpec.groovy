/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import com.vmware.o11n.sdk.modeldriven.AnonymousPluginContext
import com.vmware.o11n.sdk.modeldriven.PluginContext
import kotlin.Unit
import net.juniper.contrail.api.types.SecurityGroup
import net.juniper.contrail.vro.gen.PolicyEntriesType_Wrapper
import net.juniper.contrail.vro.gen.PolicyRuleType_Wrapper
import net.juniper.contrail.vro.gen.SecurityGroup_Wrapper

import java.lang.reflect.Method

class AddRuleToSecurityGroupSpec extends WorkflowSpec {
    // engine.eval imports and stuff!
    def addRuleToSecurityGroup = engine.getFunctionFromWorkflowScript(workflows, "Add rule to security group")
    def loadWrapperTypes = {
        engine.engine.eval("var ContrailPolicyRuleType = Java.type('net.juniper.contrail.vro.gen.PolicyRuleType_Wrapper');")
        engine.engine.eval("var ContrailPolicyEntriesType = Java.type('net.juniper.contrail.vro.gen.PolicyEntriesType_Wrapper');")
    }

    def mockParent = Mock(SecurityGroup_Wrapper)
    def mockEntries = Mock(PolicyEntriesType_Wrapper)

    def someDirection = "ingress"
    def someEthertype = "IPv4"
    def someAddressType = "CIDR"
    def someAddressCidr = "1.2.3.4/16"
    def someAddressSecurityGroup = null
    def someProtocol = "tcp"
    def somePorts = "1-2"

    def "Adding rule to a security group with existing rule list"() {
        given: "A correct set of attributes"
        mockParent.getEntries() >> mockEntries

        when: "running the script"
        createContext()
        loadWrapperTypes()

        engine.invokeFunction(
            addRuleToSecurityGroup,
            mockParent,
            someDirection,
            someEthertype,
            someAddressType,
            someAddressCidr,
            someAddressSecurityGroup,
            someProtocol,
            somePorts
        )

        then: "A new rule entry matching the input parameters should be added to the parent security group."
        1 * mockEntries.addPolicyRule({
            it.ethertype == someEthertype &&
            it.protocol == someProtocol})

        and: "The parent security group should be updated."
        1 * mockParent.update()
    }

    // TODO (unfinished)
    def "Adding an ingress rule to a security group with existing rule list"() {
        given: "A set of attributes describing an ingress rule"
        mockParent.getEntries() >> mockEntries
        def ingressDirection = "ingress"
        def ingressAddressType = someAddressType
        def ingressPorts = somePorts

        when: "running the script"
        createContext()
        loadWrapperTypes()

        engine.invokeFunction(
                addRuleToSecurityGroup,
                mockParent,
                someDirection,
                someEthertype,
                someAddressType,
                someAddressCidr,
                someAddressSecurityGroup,
                someProtocol,
                somePorts
        )

        then: "A new rule entry matching the input parameters should be added to the parent security group."
        1 * mockEntries.addPolicyRule({
            it.src})

        and: "The parent security group should be updated."
        1 * mockParent.update()
    }

    // TODO: delete
    def "Stuff should fail"() {
        given: "stuff"

        mockParent.getEntries() >> mockEntries
        def someDirection = ">"
        def someEthertype = "IPv4"
        def someAddressType = "CIDR"
        def someAddressCidr = "1.2.3.4/16"
        def someAddressSecurityGroup = null
        def someProtocol = "tcp"
        def somePorts = "1-2"


        when: "other stuff"
        createContext()
        loadWrapperTypes()

        def result = engine.invokeFunction(addRuleToSecurityGroup, mockParent, someDirection, someEthertype, someAddressType, someAddressCidr, someAddressSecurityGroup, someProtocol, somePorts)

        then: "it should not explode"
        1 * mockParent.update()
        1 * mockEntries.addPolicyRule(_)
    }
}
