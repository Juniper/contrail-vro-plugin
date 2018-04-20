item = new ContrailServiceTemplate();
item.setName(name);
item.setParentConnection(parent);

var properties = new ContrailServiceTemplateType();
properties.setServiceMode(serviceMode);
properties.setServiceType(serviceType);
properties.setVersion(version);

if (serviceVirtualizationType){
    properties.setServiceVirtualizationType(serviceVirtualizationType);
}

if (interfaceType){
    interfaceType.forEach(function(element) {
        var type = new ContrailServiceTemplateInterfaceType(element);
        properties.addInterfaceType(type);
   });
}

item.setProperties(properties);

item.create();