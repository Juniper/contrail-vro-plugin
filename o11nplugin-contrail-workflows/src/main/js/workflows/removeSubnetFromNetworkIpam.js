var index = ContrailUtils.ipamSubnetStringToIndex(ipamSubnet);

var list = item.getIpamSubnets().getSubnets();
list.splice(index, 1);

item.setIpamSubnets(new ContrailIpamSubnets(list));

item.update();