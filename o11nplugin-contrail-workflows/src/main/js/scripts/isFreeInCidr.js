if (!cidr || !ip){
	return null;
}
if (pools){
    pools = ContrailUtils.trimMultiline(pools);
    if (!ContrailUtils.isValidPool(pools)){
            return "Not valid format";
    }
}
if (dns){
    dns = dns.trim();
    if (!ContrailUtils.isValidAddress(dns)){
        return "Not valid format";
    }
}
var trimed_cidr = cidr.trim();
var trimed_ip = ip.trim();
if (!ContrailUtils.isValidAddress(trimed_ip) || !ContrailUtils.isValidCidr(trimed_cidr)){
    return "Not valid format";
}
if (ContrailUtils.isFree(trimed_cidr, trimed_ip, pools, dns)){
    return null;
}
return "Default Gateway Ip must be in CIDR and not in allocation pools or the same as DNS server ip";