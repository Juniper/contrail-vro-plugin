var actionResult = new Array();
var subnets = parent.getIpamSubnets();
if (!subnets) return null;
var subnetList = subnets.getSubnets();

subnetList.forEach(function (value) {
    actionResult.push(ContrailUtils.ipamSubnetToString(value));
});
return actionResult;