if (!serviceTemplate || !name) return false;
var serviceTemplateId = serviceTemplate.internalId;
var executor = ContrailConnectionManager.executor(serviceTemplateId.toString());
return executor.templateHasInterfaceWithName(serviceTemplate, name);