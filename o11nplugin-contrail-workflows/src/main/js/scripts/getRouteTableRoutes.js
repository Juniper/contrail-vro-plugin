var actionResult = new Array();
var routes = parent.getRoutes().getRoute()

routes.forEach(function (value, index) {
	actionResult.push(ContrailUtils.routeToString(value, index));
});
return actionResult;