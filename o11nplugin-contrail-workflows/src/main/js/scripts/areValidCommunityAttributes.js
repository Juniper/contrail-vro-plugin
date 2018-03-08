if (!communityAttributes || ContrailUtils.areValidCommunityAttributes(communityAttributes)){
    return null;
}
return "Enter valid IPv4 or IPv6 Subnet/Mask";