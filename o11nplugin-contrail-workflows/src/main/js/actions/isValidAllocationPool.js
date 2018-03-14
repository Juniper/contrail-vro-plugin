if (!cidr || !pools || pools.length == 0 || ContrailUtils.isValidAllocationPool(cidr, pools)){
	return null;
}
return "e.g. 192.168.2.3-192.168.2.10 <enter>... and IPs should be from CIDR";