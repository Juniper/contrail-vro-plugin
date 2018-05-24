if(!item) return null;
var subnet = eval('item.' + parameterPath);
return ContrailUtils.subnetToString(subnet);