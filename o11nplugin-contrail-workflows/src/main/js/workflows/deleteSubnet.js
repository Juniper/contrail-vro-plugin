ContrailUtils.removeSubnetFromVirtualNetwork(item, subnet);

item.update();
subnet.delete();