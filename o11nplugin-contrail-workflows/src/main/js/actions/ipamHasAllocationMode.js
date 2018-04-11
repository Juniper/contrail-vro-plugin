if (!networkIpam || !mode || !expected || ((networkIpam.ipamSubnetMethod == mode) == expected)){
    return null;
}
var not = ""
if (!expected){
    not = "not "
}
return "Select a network IPAM with " + not + mode + " allocation mode";