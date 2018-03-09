var id = parent.internalId;
var executor = ContrailConnectionManager.executor(id.toString());

var communityAttributes = new ContrailCommunityAttributes();
if(knownCommunityAttributes){
    knownCommunityAttributes.forEach(function(attr){
        communityAttributes.addCommunityAttribute(attr)
    });
}
if(customCommunityAttributes){
    customCommunityAttributes.forEach(function(attr){
        communityAttributes.addCommunityAttribute(attr)
    });
}

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