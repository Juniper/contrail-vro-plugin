if (!networkIpam || !mode || !(networkIpam.ipamSubnetMethod == mode)){
    return null;
}
return "Select a network IPAM with allocation mode other than " + mode;