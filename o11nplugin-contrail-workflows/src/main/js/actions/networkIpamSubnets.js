var actionResult = new Array();
var subnets = parent.getIpamSubnets();
if (!subnets) return null;
var subnetList = subnets.getSubnets();

subnetList.forEach(function (value, index) {
    actionResult.push(ContrailUtils.ipamSubnetToString(value, index));
});
return actionResult;