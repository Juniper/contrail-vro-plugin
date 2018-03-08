item = new ContrailFloatingIp();
var uuid = ContrailUtils.randomUUID();
item.setName(uuid);
item.setParentFloatingIpPool(parent);
item.setUuid(uuid);
if (address){
    item.setAddress(address);
}
if (projects && projects.length > 0){
    projects.forEach(function(element) {
       item.addProject(element);
    });
}

var id = parent.internalId;
var executor = ContrailConnectionManager.executor(id.toString());
executor.createFloatingIp(item);
item.internalId = id.with("FloatingIp", item.uuid);