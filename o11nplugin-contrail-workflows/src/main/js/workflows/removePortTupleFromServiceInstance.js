var ports = item.getPortBackRefs();

ports.forEach(function(port){
    port.removePortTuple(item);
    port.update();
});

item.delete();