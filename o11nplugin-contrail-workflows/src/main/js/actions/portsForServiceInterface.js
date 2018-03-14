if (!serviceInstance) return null;
var serviceInstanceId = serviceInstance.internalId;
var executor = ContrailConnectionManager.executor(serviceInstanceId.toString());
var network = executor.networkOfServiceInterface(serviceInstance, name);
if (!network) return null;
network.internalId = serviceInstanceId.with("VirtualNetwork", network.uuid);
return network.getPortBackRefs();