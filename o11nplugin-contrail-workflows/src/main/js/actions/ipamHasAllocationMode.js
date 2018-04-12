if (!networkIpam || !mode || ((networkIpam.ipamSubnetMethod == mode) == expected)){
    return null;
}
var not = ""
if (!expected){
    not = "not "
}
return "Select a network IPAM with " + not + mode + " allocation mode";