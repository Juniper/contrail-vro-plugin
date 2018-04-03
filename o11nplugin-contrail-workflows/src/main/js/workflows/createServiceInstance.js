item = new ContrailServiceInstance();
item.setName(name);

item.setParentProject(parent);
item.setServiceTemplate(serviceTemplate);

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

var properties = new ContrailServiceInstanceType();
var names = serviceTemplate.interfaceNames();
names.forEach(function(name) {
    var idx = ContrailConstants.serviceInterfaceNames.indexOf(name);
    var network = interfaces[idx];
    var fqn = network.getQualifiedName().join(':');
    var type = new ContrailServiceInstanceInterfaceType(fqn);
    properties.addInterface(type);
});

item.setProperties(properties);

item.create();
