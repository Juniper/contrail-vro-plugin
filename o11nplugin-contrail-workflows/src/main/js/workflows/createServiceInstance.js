item = new ContrailServiceInstance();
item.setName(name);

var id = parent.internalId;
var executor = ContrailConnectionManager.executor(id.toString());
var interfaces = [
    interface0,
    interface1,
    interface2,
    interface3,
    interface4,
    interface5,
    interface6,
    interface7,
    interface8,
    interface9,
    interface10
];
item.setParentProject(parent);
item.setServiceTemplate(serviceTemplate);

var properties = new ContrailServiceInstanceType();

var names = executor.interfaceNamesFromTemplate(serviceTemplate);
names.forEach(function(name) {
    var idx = ContrailConstants.serviceInterfaceNames.indexOf(name);
    var network = interfaces[idx];
    var fqn = network.getQualifiedName().join(':');
    var type = new ContrailServiceInstanceInterfaceType(fqn);
    properties.addInterface(type);
});

item.setProperties(properties);

executor.createServiceInstance(item);
item.internalId = id.with("ServiceInstance", item.uuid);
