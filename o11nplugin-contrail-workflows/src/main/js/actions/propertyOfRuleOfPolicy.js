switch (String(propertyName)) {
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