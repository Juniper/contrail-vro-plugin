if (!virtualNetwork || !ContrailUtils.isNetworkUserDefined(virtualNetwork)){
    return null;
}
return "Select virtual network with not user defined allocation mode";