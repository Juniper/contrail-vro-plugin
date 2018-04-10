if (!virtualNetwork || !ContrailUtils.isNetworkFlat(virtualNetwork)){
    return null;
}
return "Select virtual network with not flat allocation mode";