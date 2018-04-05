var actionResult = new Array();
var rules = parent.getIpamSubnets().getSubnets();

rules.forEach(function (value, index) {
	actionResult.push(ContrailUtils.ipamSubnetToString(value, index));
});
return actionResult;