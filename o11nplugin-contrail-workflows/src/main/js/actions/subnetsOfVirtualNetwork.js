var actionResult = new Array();
var subnets = parent.subnets();
if (!subnets) return actionResult;

subnets.forEach(function (value) {
    actionResult.push(ContrailUtils.ipamSubnetToString(value));
});
return actionResult;
