if (!input || ContrailUtils.isValidCidr(input.trim())){
    return null;
}
return "Enter valid IPv4 or IPv6 Subnet/Mask";