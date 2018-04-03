var connection = ContrailConnectionManager.connection(parent.internalId.toString());
var ipPrefix = ContrailUtils.parseSubnetIP(subnet.trim());
var ipPrefixLen = ContrailUtils.parseSubnetPrefix(subnet.trim());
var subnetName = ContrailUtils.randomUUID();

var ipamSubnet = new ContrailIpamSubnetType();
var subnetType = new ContrailSubnetType(ipPrefix, ipPrefixLen);
var csubnet = new ContrailSubnet();

csubnet.setIpPrefix(subnetType);
csubnet.setName(subnetName);
csubnet.setParentConnection(connection);
csubnet.create();
var uuid = csubnet.getUuid();

ipamSubnet.setEnableDhcp(enableDhcp);
ipamSubnet.setDefaultGateway(defaultGateway.trim());
if (dnsServerAddress){
    ipamSubnet.setDnsServerAddress(dnsServerAddress.trim());
}
ipamSubnet.setSubnet(subnetType);
ipamSubnet.setSubnetName(subnetName);
ipamSubnet.setSubnetUuid(uuid);
ipamSubnet.setAddrFromStart(addrFromStart);

if (!ContrailUtils.isBlankList(allocationPools)) {
    var pools = ContrailUtils.trimList(allocationPools);
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

parent.update();