switch (String(propertyName)) {
    case "direction":
        return securityGroup.rulePropertyDirection(rule);
    case "addressType":
        return securityGroup.rulePropertyAddressType(rule);
    case "addressCidr":
        return securityGroup.rulePropertyAddressCidr(rule);
    case "addressSecurityGroup":
        return securityGroup.rulePropertyAddressSecurityGroup(rule);
    case "ports":
        return securityGroup.rulePropertyPorts(rule);
    default:
        return null;
}