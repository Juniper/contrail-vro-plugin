if (!cidr || ContrailUtils.isValidCidr(cidr)){
    return null;
}
return "Enter valid CIDR address";