var subnets = item.getPrefix();

if (!subnets) {
    subnets = new ContrailSubnetListType();
    item.setPrefix(subnets);
}

var ipPrefix = ContrailUtils.parseSubnetIP(subnet.trim());
var ipPrefixLen = ContrailUtils.parseSubnetPrefix(subnet.trim());

var subnetType = new ContrailSubnetType(ipPrefix, ipPrefixLen);

subnets.addSubnet(subnetType);

item.update();