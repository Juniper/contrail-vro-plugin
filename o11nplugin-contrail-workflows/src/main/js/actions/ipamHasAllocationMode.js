if (!networkIpam || !mode || (networkIpam.ipamSubnetMethod == mode)){
    return null;
}
return "Select a network IPAM with " + mode + " allocation mode";