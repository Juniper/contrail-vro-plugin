if(!item) return null;
var actionResult = new Array();
var subnets = item.subnets();
if (!subnets) return actionResult;

subnets.forEach(function (value) {
    var subnet = ContrailUtils.ipamSubnetToString(value);
    if(subnet) actionResult.push(subnet);
});
return actionResult;
