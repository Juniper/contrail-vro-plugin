if (!cidr || !ip || ContrailUtils.isFree(cidr, ip, pools, dns)){
	return null;
}
return "Default Gateway IP must be in CIDR and not be in allocation pools or be the same as DNS server IP";