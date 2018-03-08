var id = parent.internalId;
var executor = ContrailConnectionManager.executor(id.toString());

var communityAttributes = new ContrailCommunityAttributes();
knownCommunityAttributes.forEach(function(attr){
    communityAttributes.addCommunityAttribute(attr)
});
customCommunityAttributes.forEach(function(attr){
    communityAttributes.addCommunityAttribute(attr)
});

var route = new ContrailRouteType();
route.setPrefix(prefix);
route.setNextHopType(nextHopType);
route.setNextHop(nextHop);
route.setCommunityAttributes(communityAttributes);

var routes = parent.getRoutes();
if (!routes) {
    routes = new ContrailRouteTableType();
    parent.setRoutes(routes);
}
routes.addRoute(route);

executor.updateRouteTable(parent);