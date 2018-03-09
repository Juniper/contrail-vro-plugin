var id = item.internalId;
var executor = ContrailConnectionManager.executor(id.toString());

var ports = item.getPortBackRefs();

ports.forEach(function(port){
    port.removePortTuple(item);
    executor.updatePort(port);
});

executor.deletePortTuple(item);