if (!cidr || !pools){
	return null;
}
var trimmed_cidr = cidr.trim();
var trimmed_pools = ContrailUtils.trimMultiline(pools);
if (ContrailUtils.isValidAllocationPool(trimmed_cidr, trimmed_pools)){
    return null;
}
return "e.g. 192.168.2.3-192.168.2.10 <enter>... and IPs should be from CIDR";