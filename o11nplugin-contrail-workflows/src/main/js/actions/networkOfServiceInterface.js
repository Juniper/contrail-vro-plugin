if (!serviceInstance) return null;
var serviceInstanceId = serviceInstance.internalId;
var executor = ContrailConnectionManager.executor(serviceInstanceId.toString());
var network = executor.networkOfServiceInterface(serviceInstance, name);
network.internalId = serviceInstanceId.with("VirtualNetwork", network.uuid);
return network;