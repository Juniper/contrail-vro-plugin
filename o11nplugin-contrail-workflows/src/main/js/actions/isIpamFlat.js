if (!networkIpam || ContrailUtils.isIpamFlat(networkIpam)){
    return null;
}
return "Select a network IPAM with flat allocation mode";