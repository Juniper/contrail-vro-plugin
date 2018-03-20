var executor = ContrailConnectionManager.executor(item.internalId.toString());

var ipPrefix = ContrailUtils.parseSubnetIP(ip);
var ipPrefixLen = ContrailUtils.parseSubnetPrefix(ip);
var subnet = new ContrailSubnetType(ipPrefix, parseInt(ipPrefixLen));

var pair = new ContrailAllowedAddressPair(subnet, mac);
if (addressMode){
    pair.setAddressMode(addressMode);
}

var index = executor.interfaceIndexByName(item, interfaceName);
var pairs = item.getProperties().getInterfaceList()[index].getAllowedAddressPairs();
if (!pairs){
    pairs = new ContrailAllowedAddressPairs();
}
pairs.addAllowedAddressPair(pair);
item.getProperties().getInterfaceList()[index].setAllowedAddressPairs(pairs);

item.update();