var ports = [
    port0,
    port1,
    port2,
    port3,
    port4,
    port5,
    port6,
    port7,
    port8,
    port9,
    port10
];

var interfaceTypes = [
    "left",
    "right",
    "management",
    "other0",
    "other1",
    "other2",
    "other3",
    "other4",
    "other5",
    "other6",
    "other7"
];

var oldPortInterfaceTypes = ports.map(function(port){
    if(port && port.properties){
        return port.properties.serviceInterfaceType;
    }
    return null;
});

function addTupleToPort(portTuple, portObject, portIndex) {
    if(portObject){
        portObject.addPortTuple(portTuple);
        if (!portObject.properties){
            portObject.properties = new ContrailVirtualMachineInterfacePropertiesType();
        }
        portObject.properties.setServiceInterfaceType(interfaceTypes[portIndex]);
        portObject.update();
    }
}

function removeTupleFromPort(portTuple, portObject, portIndex) {
    if(portObject){
        portObject.removePortTuple(portTuple);
        if (portObject.properties){
            portObject.properties.setServiceInterfaceType(oldPortInterfaceTypes[portIndex]);
        }
        portObject.update();
    }
}

var portTuple = new ContrailPortTuple();
portTuple.setName(name);
portTuple.setParentServiceInstance(parent);
portTuple.create();

try {
    ports.forEach(function(element, index){
        addTupleToPort(portTuple, element, index);
    });
} catch(err) {
    ports.forEach(function(element, index){
        removeTupleFromPort(portTuple, element, index);
    });

    portTuple.delete();
    throw err;
}