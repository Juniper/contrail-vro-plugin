if (!serviceInstance) return null;
var network = serviceInstance.networkOfServiceInterface(name);
if (!network) return null;
return network.getPortBackRefs();