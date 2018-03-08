var index = ContrailUtils.routeStringToIndex(route);

var list = parent.getRoutes().getRoute();
list.splice(index, 1);

parent.setRoutes(new ContrailRouteTableType(list));

var id = parent.internalId;
var executor = ContrailConnectionManager.executor(id.toString());
executor.updateRouteTable(parent);