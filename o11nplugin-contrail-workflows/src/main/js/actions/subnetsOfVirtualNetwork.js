var actionResult = new Array();
var subnets = parent.subnets();
if (!subnets) return null;

subnets.forEach(function (value) {
    actionResult.push(ContrailUtils.ipamSubnetToString(value));
});
return actionResult;
