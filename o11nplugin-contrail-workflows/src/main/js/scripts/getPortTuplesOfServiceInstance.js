if (!parent) return null;
var parentId = parent.internalId;
var executor = ContrailConnectionManager.executor(parentId.toString());
var portTuples = executor.getPortTuplesOfServiceInstance(parent);
portTuples.forEach(function(tuple){
    tuple.internalId = parentId.with("PortTuple", tuple.uuid);
});
return portTuples;