if (!serviceInstance) return null;
var serviceInstanceId = serviceInstance.internalId;
var executor = ContrailConnectionManager.executor(serviceInstanceId.toString());
var network = executor.getNetworkOfServiceInterface(serviceInstance, name);
network.internalId = serviceInstanceId.with("VirtualNetwork", network.uuid);
return network;