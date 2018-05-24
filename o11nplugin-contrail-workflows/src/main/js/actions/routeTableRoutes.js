var actionResult = new Array();
var routes = item.getRoutes().getRoute()

routes.forEach(function (value, index) {
    actionResult.push(ContrailUtils.routeToString(value, index));
});
return actionResult;