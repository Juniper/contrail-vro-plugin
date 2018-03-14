if (!parent) return null;
var parentId = parent.internalId;
var executor = ContrailConnectionManager.executor(parentId.toString());
var subnets = executor.subnetsOfVirtualNetwork(parent);
for each (s in subnets) {
    s.internalId = parentId.with("Subnet", s.uuid);
}
return subnets;