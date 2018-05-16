var ipPrefix = ContrailUtils.parseSubnetIP(subnet.trim());
var ipPrefixLen = ContrailUtils.parseSubnetPrefix(subnet.trim());

var ipamSubnet = new ContrailIpamSubnetType();
var subnetType = new ContrailSubnetType(ipPrefix, ipPrefixLen);

ipamSubnet.setEnableDhcp(enableDhcp);
ipamSubnet.setDefaultGateway(defaultGateway.trim());
if (dnsServerAddress){
    ipamSubnet.setDnsServerAddress(dnsServerAddress.trim());
}
ipamSubnet.setSubnet(subnetType);
ipamSubnet.setAddrFromStart(addrFromStart);
if (allocUnit){
    ipamSubnet.setAllocUnit(allocUnit);
}

if (!ContrailUtils.isBlankList(allocationPools)) {
    var pools = ContrailUtils.trimList(allocationPools);
    pools.forEach(function(element) {
       var parts = element.split("-");
       var allocPool = new ContrailAllocationPoolType(parts[0], parts[1]);
       ipamSubnet.addAllocationPools(allocPool);
    });
}

var ipamSubnets = parent.getIpamSubnets();
if (!ipamSubnets){
    ipamSubnets = new ContrailIpamSubnets();
    parent.setIpamSubnets(ipamSubnets);
}

ipamSubnets.addSubnets(ipamSubnet);

parent.update();