ContrailUtils.removeSubnetFromVirtualNetwork(item, subnet);

var executor = ContrailConnectionManager.executor(item.internalId.toString());
executor.updateVirtualNetwork(item);
executor.deleteSubnet(subnet);