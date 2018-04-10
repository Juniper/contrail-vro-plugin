var actionResult = new Array();
var subnets = parent.subnets();
if (!subnets) return null;

subnets.forEach(function (value, index) {
    actionResult.push(ContrailUtils.ipamSubnetToString(value, index));
});
return actionResult;
