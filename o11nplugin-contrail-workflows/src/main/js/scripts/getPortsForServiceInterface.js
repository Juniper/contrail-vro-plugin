if (!serviceInstance) return null;
var serviceInstanceId = serviceInstance.internalId;
var executor = ContrailConnectionManager.executor(serviceInstanceId.toString());
var network = executor.getNetworkOfServiceInterface(serviceInstance, name);
if (!network) return null;
network.internalId = serviceInstanceId.with("VirtualNetwork", network.uuid);

var elements = executor.getPortsOfVirtualNetwork(network);
for each (e in elements) {
    e.internalId = network.internalId.with("Port", e.uuid);
}
return elements;