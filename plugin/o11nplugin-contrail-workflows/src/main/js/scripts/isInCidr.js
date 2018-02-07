if (!cidr || !ip){
	return null;
}
var trimmed_cidr = cidr.trim();
var trimmed_ip = ip.trim();
if (!ContrailUtils.isValidAddress(trimmed_ip) || !ContrailUtils.isValidCidr(trimmed_cidr)){
    return "Not valid format";
}
if (ContrailUtils.isInCidr(trimmed_cidr, trimmed_ip)){
    return null;
}
return "IP must be in defined CIDR";