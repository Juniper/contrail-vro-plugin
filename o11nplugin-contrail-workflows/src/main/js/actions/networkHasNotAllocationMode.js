if (!virtualNetwork || !mode || (virtualNetwork.addressAllocationMode != mode)){
    return null;
}
return "Select virtual network with allocation mode other than " + mode;