switch (String(propertyName)) {
    case "srcAddressType":
        return networkPolicy.rulePropertyAddressType(rule, false);
    case "srcVirtualNetwork":
        return networkPolicy.ruleAddressPropertyVirtualNetwork(rule, false);
    case "srcSecurityGroup":
        return networkPolicy.ruleAddressPropertySecurityGroup(rule, false);
    case "srcNetworkPolicy":
        return networkPolicy.ruleAddressPropertyNetworkPolicy(rule, false);
    case "srcSubnet":
        return networkPolicy.ruleAddressPropertySubnet(rule, false);
    case "srcVirtualNetworkType":
        return networkPolicy.ruleAddressPropertyNetworkType(rule, false);
    case "srcPorts":
        return networkPolicy.ruleAddressPropertyPorts(rule, false);
    case "dstAddressType":
        return networkPolicy.rulePropertyAddressType(rule, true);
    case "dstVirtualNetwork":
        return networkPolicy.ruleAddressPropertyVirtualNetwork(rule, true);
    case "dstSecurityGroup":
        return networkPolicy.ruleAddressPropertySecurityGroup(rule, true);
    case "dstNetworkPolicy":
        return networkPolicy.ruleAddressPropertyNetworkPolicy(rule, true);
    case "dstSubnet":
        return networkPolicy.ruleAddressPropertySubnet(rule, true);
    case "dstVirtualNetworkType":
        return networkPolicy.ruleAddressPropertyNetworkType(rule, true);
    case "dstPorts":
        return networkPolicy.ruleAddressPropertyPorts(rule, true);
    case "defineServices":
        return networkPolicy.rulePropertyDefineServices(rule);
    case "services":
        return networkPolicy.rulePropertyServices(rule);
    case "defineMirror":
        return networkPolicy.rulePropertyDefineMirror(rule);
    case "mirrorType":
        return networkPolicy.rulePropertyMirrorType(rule);
    case "mirrorAnalyzerName":
        return networkPolicy.rulePropertyMirrorAnalyzerName(rule);
    case "mirrorAnalyzerInstance":
        return networkPolicy.rulePropertyMirrorAnalyzerInstance(rule);
    case "mirrorNicAssistedVlan":
        return networkPolicy.rulePropertyMirrorNicAssistedVlan(rule);
    case "mirrorAnalyzerIP":
        return networkPolicy.rulePropertyMirrorAnalyzerIP(rule);
    case "mirrorAnalyzerMac":
        return networkPolicy.rulePropertyMirrorAnalyzerMac(rule);
    case "mirrorUdpPort":
        return networkPolicy.rulePropertyMirrorUdpPort(rule);
    case "mirrorJuniperHeader":
        return networkPolicy.rulePropertyMirrorJuniperHeader(rule);
    case "mirrorRoutingInstance":
        return networkPolicy.rulePropertyMirrorRoutingInstance(rule);
    case "mirrorNexthopMode":
        return networkPolicy.rulePropertyMirrorNexthopMode(rule);
    case "mirrorVtepDestIp":
        return networkPolicy.rulePropertyMirrorVtepDestIp(rule);
    case "mirrorVtepDestMac":
        return networkPolicy.rulePropertyMirrorVtepDestMac(rule);
    case "mirrorVni":
        return networkPolicy.rulePropertyMirrorVni(rule);
    default:
        return null;
}