item = new ContrailFloatingIpPool();
item.setName(name);

var id = parent.internalId;
var executor = ContrailConnectionManager.executor(id.toString());
executor.createFloatingIpPoolInVirtualNetwork(item, parent);
item.internalId = id.with("FloatingIpPool", item.uuid);

if (projects && projects.length > 0){
    projects.forEach(function(project) {
        project.addFloatingIpPool(item);
        executor.updateProject(project);
    });
}

