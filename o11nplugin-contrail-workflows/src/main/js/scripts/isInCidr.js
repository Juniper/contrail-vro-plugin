if (!cidr || !ip || ContrailUtils.isInCidr(cidr, ip)){
	return null;
}
return "IP must be in defined CIDR";