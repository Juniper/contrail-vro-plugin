var index = ContrailUtils.ipamSubnetStringToIndex(rule);

var list = parent.getIpamSubnets().getSubnets();
list.splice(index, 1);

parent.setIpamSubnets(new ContrailIpamSubnets(list));

parent.update();