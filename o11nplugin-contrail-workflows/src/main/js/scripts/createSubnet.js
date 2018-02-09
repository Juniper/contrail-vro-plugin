var executor = ContrailConnectionManager.executor(parent.internalId.toString());
var ip_prefix = ContrailUtils.parseSubnetIP(subnet.trim());
var ip_prefix_len = ContrailUtils.parseSubnetPrefix(subnet.trim());
var subnet_name = ContrailUtils.randomUUID();

var ipamSubnet = new ContrailIpamSubnetType();
var subnetType = new ContrailSubnetType(ip_prefix, ip_prefix_len);
var csubnet = new ContrailSubnet();

csubnet.setIpPrefix(subnetType);
csubnet.setName(subnet_name);
executor.createSubnet(csubnet);
var uuid = csubnet.getUuid();

ipamSubnet.setEnableDhcp(enableDhcp);
ipamSubnet.setDefaultGateway(defaultGateway.trim());
if (dnsServerAddress){
    ipamSubnet.setDnsServerAddress(dnsServerAddress.trim());
}
ipamSubnet.setSubnet(subnetType);
ipamSubnet.setSubnetName(subnet_name);
ipamSubnet.setSubnetUuid(uuid);
ipamSubnet.setAddrFromStart(addrFromStart);

var pools = ContrailUtils.splitMultiline(allocationPools);
if (pools.length != 0) {
    pools.forEach(function(element) {
       var parts = element.split("-");
       ipamSubnet.addAllocationPools(parts[0], parts[1]);
    });
}

var vnSubnet = ContrailUtils.getVnSubnet(parent, ipam);

vnSubnet.addIpamSubnets(ipamSubnet);
if (!ContrailUtils.isNetworRelatedToIpam(parent, ipam)){
	parent.addNetworkIpam(ipam, vnSubnet);
}

executor.updateVirtualNetwork(parent);