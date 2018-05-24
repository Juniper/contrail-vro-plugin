if(!item) return null;
var actionResult = new Array();
var subnets = item.getPrefix();
if (!subnets) return null;
var subnetList = subnets.getSubnet();

subnetList.forEach(function (value) {
    var subnet = ContrailUtils.subnetToString(value);
    if(subnet) actionResult.push(subnet);
});
return actionResult;