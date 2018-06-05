var ruleUuid = ContrailUtils.randomUUID();

var parsedSrcPorts = ContrailUtils.parsePortsOfNetworkPolicyRule(srcPorts);
var parsedDstPorts = ContrailUtils.parsePortsOfNetworkPolicyRule(dstPorts);

var srcAddr = [ContrailUtils.createAddress(srcAddressType, srcSubnet, srcVirtualNetworkType, srcVirtualNetwork, srcNetworkPolicy, srcSecurityGroup)];
var dstAddr = [ContrailUtils.createAddress(dstAddressType, dstSubnet, dstVirtualNetworkType, dstVirtualNetwork, dstNetworkPolicy, dstSecurityGroup)];

var application = [];

var mirrorAction = null;
if (defineMirror) {
    var instanceName = mirrorAnalyzerName;
    if (mirrorAnalyzerInstance != null) {
        instanceName = mirrorAnalyzerInstance.getQualifiedName().join(":");
    }
    var routingInstanceName;
    if (mirrorRoutingInstance != null) {
        routingInstanceName = mirrorRoutingInstance.getQualifiedName().join(":") + ":" + mirrorRoutingInstance.getName();
    }
    mirrorAction = new ContrailMirrorActionType();
    mirrorAction.setAnalyzerName(instanceName);
    mirrorAction.setAnalyzerIpAddress(mirrorAnalyzerIP);
    mirrorAction.setAnalyzerMacAddress(mirrorAnalyzerMac);
    mirrorAction.setRoutingInstance(routingInstanceName);
    mirrorAction.setUdpPort(mirrorUdpPort);
    mirrorAction.setJuniperHeader(mirrorJuniperHeader == "enabled");
    mirrorAction.setNhMode(mirrorNexthopMode);
    mirrorAction.setNicAssistedMirroring(mirrorType == "NIC Assisted");
    mirrorAction.setNicAssistedMirroringVlan(mirrorNicAssistedVlan);
    if (mirrorNexthopMode == "static") {
        staticNhHeader = new ContrailStaticMirrorNhType();
        staticNhHeader.setVtepDstIpAddress(mirrorVtepDestIp);
        staticNhHeader.setVtepDstMacAddress(mirrorVtepDestMac);
        staticNhHeader.setVni(mirrorVni);
        mirrorAction.setStaticNhHeader(staticNhHeader);
    }
}

var actions = new ContrailActionListType()
actions.setSimpleAction(simpleAction);
actions.setLog(log);
if (services != null) {
    services.forEach(function(instance){
        actions.addApplyService(instance.getQualifiedName().join(":"));
    });
}
actions.setMirrorTo(mirrorAction);

var rule = new ContrailPolicyRuleType(null, ruleUuid, direction, protocol, srcAddr, parsedSrcPorts, application, dstAddr, parsedDstPorts, actions);
var rules = item.getEntries();
if (!rules) {
    rules = new ContrailPolicyEntriesType();
    item.setEntries(rules);
}
rules.addPolicyRule(rule);

item.update();