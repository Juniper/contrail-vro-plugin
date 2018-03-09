var executor = ContrailConnectionManager.executor(item.internalId.toString());
var projects = item.getProjectBackRefs();

projects.forEach(function(project) {
    project.removeFloatingIpPool(item);
    executor.updateProject(project);
});

executor.deleteFloatingIpPool(item);