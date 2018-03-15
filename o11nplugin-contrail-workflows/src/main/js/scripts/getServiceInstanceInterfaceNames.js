if (!serviceInstance) return null;
var serviceInstanceId = serviceInstance.internalId;
var executor = ContrailConnectionManager.executor(serviceInstanceId.toString());
return executor.getInterfaceNamesFromService(serviceInstance);