item = new ContrailServiceTemplate();
item.setName(name);
item.setParentConnection(parent);

var properties = new ContrailServiceTemplateType();
properties.setServiceMode(serviceMode);
properties.setServiceType(serviceType);
properties.setAvailabilityZoneEnable(availabilityZoneEnable);
properties.setInstanceData(instanceData);
properties.setVersion(version);

if (serviceVirtualizationType){
    properties.setServiceVirtualizationType(serviceVirtualizationType);
}
if (vrouterInstanceType){
    properties.setVrouterInstanceType(vrouterInstanceType);
}

if (interfaceType){
    interfaceType.forEach(function(element) {
        var type = new ContrailServiceTemplateInterfaceType(element);
        properties.addInterfaceType(type);
   });
}

item.setProperties(properties);

item.create();