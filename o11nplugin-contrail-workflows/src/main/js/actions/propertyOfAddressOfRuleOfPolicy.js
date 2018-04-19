switch (String(propertyName)) {
    case "virtualNetwork":
        return networkPolicy.ruleAddressPropertyNetwork(rule, dstMode);
    case "securityGroup":
        return networkPolicy.ruleAddressPropertySG(rule, dstMode);
    case "networkPolicy":
        return networkPolicy.ruleAddressPropertyPolicy(rule, dstMode);
    case "subnet":
        return networkPolicy.ruleAddressPropertySubnet(rule, dstMode);
    default:
        return null;
}