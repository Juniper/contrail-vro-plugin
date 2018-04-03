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

item.create();