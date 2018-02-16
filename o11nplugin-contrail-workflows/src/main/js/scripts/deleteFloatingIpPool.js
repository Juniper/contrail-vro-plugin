var executor = ContrailConnectionManager.executor(item.internalId.toString());
var projects = executor.getProjectsOfFloatingIpPool(item);

if (projects && projects.length > 0){
    projects.forEach(function(project) {
        project.removeFloatingIpPool(item);
        executor.updateProject(project);
    });
}
executor.deleteFloatingIpPool(item);