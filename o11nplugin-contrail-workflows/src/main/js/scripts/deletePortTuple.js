var id = portTuple.internalId;
var executor = ContrailConnectionManager.executor(id.toString());

var ports = executor.getPortsOfPortTuple(portTuple);

ports.forEach(function(portObject){
    portObject.removePortTuple(portTuple);
    executor.updatePort(portObject);
});

executor.deletePortTuple(portTuple);