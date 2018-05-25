package net.juniper.contrail.vro.model

import net.juniper.contrail.api.types.AddressGroup
import net.juniper.contrail.api.types.FirewallRule
import net.juniper.contrail.api.types.FirewallRuleEndpointType
import net.juniper.contrail.api.types.Tag
import net.juniper.contrail.api.types.VirtualNetwork
import net.juniper.contrail.vro.config.constants.EndpointType
import net.juniper.contrail.vro.config.constants.ServiceType

interface FirewallRuleSpecialProperties {
    fun FirewallRule.endpointType(endpointNumber: Int): String?
    fun FirewallRule.serviceType(): String?
    fun FirewallRule.endpointNetwork(endpointNumber: Int): VirtualNetwork?
    fun FirewallRule.endpointAddressGroup(endpointNumber: Int): AddressGroup?
    fun FirewallRule.endpointTags(endpointNumber: Int): List<Tag>?
}

open class FirewallRuleSpecialPropertyExecutor(private val connection: Connection) : FirewallRuleSpecialProperties {
    override fun FirewallRule.endpointType(endpointNumber: Int): String? {
        return endpoint(endpointNumber)?.type()
    }

    override fun FirewallRule.serviceType(): String? {
        return when {
            serviceGroup[0] != null -> ServiceType.Reference.value
            else -> ServiceType.Manual.value
        }
    }

    override fun FirewallRule.endpointNetwork(endpointNumber: Int): VirtualNetwork? {
        val networkFqn = endpoint(endpointNumber)?.virtualNetwork ?: return null
        return connection.findByFQN(networkFqn)
    }

    override fun FirewallRule.endpointAddressGroup(endpointNumber: Int): AddressGroup? {
        val addressGroupFqn = endpoint(endpointNumber)?.addressGroup ?: return null
        return connection.findByFQN(addressGroupFqn)
    }

    // TODO
    override fun FirewallRule.endpointTags(endpointNumber: Int): List<Tag>? {
        return null
    }

    private fun FirewallRuleEndpointType.type(): String? {
        println(this)
        return when {
            any == true -> EndpointType.AnyWorkload.value
            addressGroup != null -> EndpointType.AddressGroup.value
            virtualNetwork != null -> EndpointType.VirtualNetwork.value
            tags.isNotEmpty() -> EndpointType.Tag.value
            else -> EndpointType.None.value
        }
    }

    private fun FirewallRule.endpoint(endpointNumber: Int): FirewallRuleEndpointType? {
        return when (endpointNumber) {
            1 -> endpoint1
            2 -> endpoint2
            else -> null
        }
    }
}