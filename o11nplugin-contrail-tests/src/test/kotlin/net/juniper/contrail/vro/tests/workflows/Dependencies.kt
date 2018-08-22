/*
 * Copyright (c) 2018 Juniper Networks, Inc. All rights reserved.
 */

package net.juniper.contrail.vro.tests.workflows

import net.juniper.contrail.vro.gen.AddressGroup_Wrapper
import net.juniper.contrail.vro.gen.ApplicationPolicySet_Wrapper
import net.juniper.contrail.vro.gen.ConfigRoot_Wrapper
import net.juniper.contrail.vro.gen.Connection_Wrapper
import net.juniper.contrail.vro.gen.FirewallPolicy_Wrapper
import net.juniper.contrail.vro.gen.FirewallRule_Wrapper
import net.juniper.contrail.vro.gen.FloatingIpPool_Wrapper
import net.juniper.contrail.vro.gen.FloatingIp_Wrapper
import net.juniper.contrail.vro.gen.IpamSubnetType_Wrapper
import net.juniper.contrail.vro.gen.NetworkIpam_Wrapper
import net.juniper.contrail.vro.gen.NetworkPolicy_Wrapper
import net.juniper.contrail.vro.gen.PolicyManagement_Wrapper
import net.juniper.contrail.vro.gen.PortTuple_Wrapper
import net.juniper.contrail.vro.gen.Project_Wrapper
import net.juniper.contrail.vro.gen.SecurityGroup_Wrapper
import net.juniper.contrail.vro.gen.ServiceGroup_Wrapper
import net.juniper.contrail.vro.gen.ServiceHealthCheck_Wrapper
import net.juniper.contrail.vro.gen.ServiceInstance_Wrapper
import net.juniper.contrail.vro.gen.ServiceTemplate_Wrapper
import net.juniper.contrail.vro.gen.SubnetType_Wrapper
import net.juniper.contrail.vro.gen.TagType_Wrapper
import net.juniper.contrail.vro.gen.Tag_Wrapper
import net.juniper.contrail.vro.gen.Utils_Wrapper
import net.juniper.contrail.vro.gen.VirtualMachineInterfacePropertiesType_Wrapper
import net.juniper.contrail.vro.gen.VirtualMachineInterface_Wrapper
import net.juniper.contrail.vro.gen.VirtualNetwork_Wrapper
import java.util.UUID

fun randomStringUuid() = UUID.randomUUID().toString()

class Dependencies(private val connection: Connection_Wrapper, private val utils: Utils_Wrapper) {
    val configRoot: ConfigRoot_Wrapper = ConfigRoot_Wrapper().apply {
        uuid = randomStringUuid()
        name = "config-root"
        setParentConnection(this@Dependencies.connection)
    }

    val defaultPolicyManagement: PolicyManagement_Wrapper = PolicyManagement_Wrapper().apply {
        uuid = randomStringUuid()
        name = "default-policy-management"
        // workaround to set PM's parent to configRoot while it's hidden
        __getTarget().setParent(configRoot.__getTarget())
        setParentConnection(this@Dependencies.connection)
    }

    val globalDraftPolicyManagement: PolicyManagement_Wrapper = PolicyManagement_Wrapper().apply {
        uuid = randomStringUuid()
        name = "draft-policy-management"
        __getTarget().setParent(configRoot.__getTarget())
        setParentConnection(this@Dependencies.connection)
    }

    fun someProject(): Project_Wrapper = Project_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someProject$uuid"
        setParentConnection(this@Dependencies.connection)
    }

    @JvmOverloads
    fun someProjectDraftPolicyManagement(parent: Project_Wrapper = someProject()): PolicyManagement_Wrapper = PolicyManagement_Wrapper().apply {
        uuid = randomStringUuid()
        name = "draft-policy-management"
        // workaround to set PM's parent to a project
        __getTarget().setParent(parent.__getTarget())
        internalId = parent.internalId.with("VirtualNetwork", uuid)
    }

    @JvmOverloads
    fun someVirtualNetwork(parent: Project_Wrapper = someProject()) = VirtualNetwork_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someVirtualNetwork$uuid"
        setParentProject(parent)
    }

    fun someGlobalApplicationPolicySet() = ApplicationPolicySet_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someApplicationPolicySet$uuid"
        setParentPolicyManagement(defaultPolicyManagement)
    }

    @JvmOverloads
    fun someFloatingIpPool(parent: VirtualNetwork_Wrapper = someVirtualNetwork()) = FloatingIpPool_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someFloatingIpPool$uuid"
        setParentVirtualNetwork(parent)
    }

    @JvmOverloads
    fun someNetworkIpam(parent: Project_Wrapper = someProject()) = NetworkIpam_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someNetworkIpam$uuid"
        setParentProject(parent)
    }

    @JvmOverloads
    fun somePort(parent: Project_Wrapper = someProject()) = VirtualMachineInterface_Wrapper().apply {
        uuid = randomStringUuid()
        name = "somePort$uuid"
        setParentProject(parent)
    }

    @JvmOverloads
    fun someFloatingIp(parent: FloatingIpPool_Wrapper = someFloatingIpPool()) = FloatingIp_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someFloatingIp$uuid"
        setParentFloatingIpPool(parent)
    }

    @JvmOverloads
    fun someNetworkPolicy(parent: Project_Wrapper = someProject()) = NetworkPolicy_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someNetworkPolicy$uuid"
        setParentProject(parent)
    }

    @JvmOverloads
    fun someServiceInstance(parent: Project_Wrapper = someProject()) = ServiceInstance_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someServiceInstance$uuid"
        setParentProject(parent)
    }

    @JvmOverloads
    fun someServiceHealthCheck(parent: Project_Wrapper = someProject()) = ServiceHealthCheck_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someServiceHealthCheck$uuid"
        setParentProject(parent)
    }

    @JvmOverloads
    fun someSecurityGroup(parent: Project_Wrapper = someProject()) = SecurityGroup_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someSecurityGroup$uuid"
        setParentProject(parent)
    }

    @JvmOverloads
    fun someAddressGroup(parent: Project_Wrapper = someProject()) = AddressGroup_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someSecurityGroup$uuid"
        setParentProject(parent)
    }

    @JvmOverloads
    fun somePortTuple(parent: ServiceInstance_Wrapper = someServiceInstance()) = PortTuple_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someSecurityGroup$uuid"
        setParentServiceInstance(parent)
    }

    @JvmOverloads
    fun someProjectFirewallPolicy(parent: Project_Wrapper = someProject()) = FirewallPolicy_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someProjectFirewallPolicy$uuid"
        setParentProject(parent)
    }

    @JvmOverloads
    fun someGlobalFirewallPolicy(parent: Project_Wrapper = someProject()) = FirewallPolicy_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someProjectFirewallPolicy$uuid"
        setParentPolicyManagement(defaultPolicyManagement)
    }

    @JvmOverloads
    fun someProjectFirewallRule(parent: Project_Wrapper = someProject()) = FirewallRule_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someProjectFirewallRule$uuid"
        setParentProject(parent)
    }

    @JvmOverloads
    fun someDraftFirewallRule(parent: PolicyManagement_Wrapper = globalDraftPolicyManagement) = FirewallRule_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someFirewallRule$uuid"
        setParentPolicyManagement(parent)
    }

    fun someGlobalFirewallRule() = FirewallRule_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someGlobalFirewallRule$uuid"
        setParentPolicyManagement(defaultPolicyManagement)
    }

    fun someServiceTemplate() = ServiceTemplate_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someServiceTemplate$uuid"
        setParentConnection(this@Dependencies.connection)
    }

    @JvmOverloads
    fun someProjectServiceGroup(parent: Project_Wrapper = someProject()) = ServiceGroup_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someProjectServiceGroup$uuid"
        setParentProject(parent)
    }

    @JvmOverloads
    fun someDraftServiceGroup(parent: PolicyManagement_Wrapper = globalDraftPolicyManagement) = ServiceGroup_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someServiceGroup$uuid"
        setParentPolicyManagement(parent)
    }

    fun someGlobalServiceGroup() = ServiceGroup_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someGlobalServiceGroup$uuid"
        setParentPolicyManagement(defaultPolicyManagement)
    }

    @JvmOverloads
    fun someProjectTag(parent: Project_Wrapper = someProject()) = Tag_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someProjectTag$uuid"
        setParentProject(parent)
    }

    fun someGlobalTag() = Tag_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someGlobalTag$uuid"
        setParentConfigRoot(configRoot)
        setParentConnection(this@Dependencies.connection)
    }

    fun someTagType() = TagType_Wrapper().apply {
        uuid = randomStringUuid()
        name = "someTag$uuid"
        setParentConnection(this@Dependencies.connection)
    }

    fun someIpamSubnetType() = IpamSubnetType_Wrapper()

    fun someSubnetType(ipPrefix: String = "1.2.3.4", ipPrefixLen: Int = 16) = SubnetType_Wrapper(ipPrefix, ipPrefixLen)

    fun somePortProperties() = VirtualMachineInterfacePropertiesType_Wrapper()
}
