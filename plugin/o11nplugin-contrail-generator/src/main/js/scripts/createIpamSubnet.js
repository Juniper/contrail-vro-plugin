var executor = ContrailConnectionManager.getExecutor(parent.getInternalId().toString());

var ipamSubnet = new ContrailIpamSubnetType();
var subnetType = new ContrailSubnetType(ip_prefix, ip_prefix_len);
var subnet = new ContrailSubnet();

subnet.setIpPrefix(subnetType);
subnet.setName(subnet_name);
executor.createSubnet(subnet);
var uuid = subnet.getUuid();

ipamSubnet.setEnableDhcp(enable_dhcp);
ipamSubnet.setDefaultGateway(default_gateway);
ipamSubnet.setDnsServerAddress(dns_server_address);
ipamSubnet.setSubnet(subnetType);
ipamSubnet.setSubnetName(subnet_name);
ipamSubnet.setSubnetUuid(uuid);
ipamSubnet.setAddrFromStart(addr_from_start);

allocation_pools.forEach(function(element) {
   ipamSubnet.addAllocationPools(element.start, element.end);
});

var vnSubnet = ContrailUtils.getVnSubnet(parent, ipam);

vnSubnet.addIpamSubnets(ipamSubnet);
if (!ContrailUtils.isNetworRelatedToIpam(parent, ipam)){
	parent.addNetworkIpam(ipam, vnSubnet);
}

executor.updateVirtualNetwork(parent);