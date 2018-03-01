if (!virtualNetwork) return null;
var virtualNetworkId = virtualNetwork.internalId;
var executor = ContrailConnectionManager.executor(virtualNetworkId.toString());
var elements = executor.getPortsOfVirtualNetwork(virtualNetwork);
for each (e in elements) {
    e.internalId = virtualNetworkId.with("Port", e.uuid);
}
return elements;