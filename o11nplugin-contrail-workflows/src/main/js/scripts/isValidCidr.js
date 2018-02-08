if (!input || ContrailUtils.isValidCidr(input)){
    return null;
}
return "Enter valid IPv4 or IPv6 Subnet/Mask";