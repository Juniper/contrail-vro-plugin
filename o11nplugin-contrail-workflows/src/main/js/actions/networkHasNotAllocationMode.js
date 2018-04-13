if (!virtualNetwork || !mode || (virtualNetwork.addressAllocationMode != mode)){
    return null;
}
return "Select virtual network with not " + mode + " allocation mode";