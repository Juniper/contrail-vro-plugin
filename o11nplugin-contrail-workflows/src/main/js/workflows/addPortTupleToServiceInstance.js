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
    if(port){
        port.properties.serviceInterfaceType;
    }
    return null;
});

function addTupleToPort(executor, portTuple, portObject, portIndex) {
    if(portObject){
        portObject.addPortTuple(portTuple);
        portObject.properties.setServiceInterfaceType(interfaceTypes[portIndex]);
        executor.updatePort(portObject);
    }
}

function removeTupleFromPort(executor, portTuple, portObject, portIndex) {
    if(portObject){
        portObject.removePortTuple(portTuple);
        portObject.properties.setServiceInterfaceType(oldPortInterfaceTypes[portIndex]);
        executor.updatePort(portObject);
    }
}

var id = parent.internalId;
var executor = ContrailConnectionManager.executor(id.toString());
var portTuple = new ContrailPortTuple();
portTuple.setName(name);
portTuple.setParentServiceInstance(parent);
executor.createPortTuple(portTuple);
executor.readPortTuple(portTuple);

try {
    ports.forEach(function(element, index){
        addTupleToPort(executor, portTuple, element, index);
    });
} catch(err) {
    ports.forEach(function(element, index){
        removeTupleFromPort(executor, portTuple, element, index);
    });

	executor.deletePortTuple(portTuple);
	throw err;
}