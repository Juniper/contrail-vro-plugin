if (!serviceInstance) return null;
var serviceInstanceId = serviceInstance.internalId;
var executor = ContrailConnectionManager.executor(serviceInstanceId.toString());
return executor.serviceHasInterfaceWithName(serviceInstance, name);