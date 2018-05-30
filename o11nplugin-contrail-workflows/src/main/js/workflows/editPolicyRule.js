var index = ContrailUtils.stringToIndex(rule);
var theRule = item.entries.policyRule[index];

theRule.protocol = protocol;
theRule.direction = direction;

theRule.clearSrcPorts();
theRule.clearDstPorts();
ContrailUtils.parsePortsOfNetworkPolicyRule(srcPorts).forEach(function(port) {
   theRule.addSrcPorts(port);
});
ContrailUtils.parsePortsOfNetworkPolicyRule(dstPorts).forEach(function(port) {
   theRule.addDstPorts(port);
});

theRule.clearSrcAddresses();
theRule.clearDstAddresses();
var srcAddress = ContrailUtils.createAddress(srcAddressType, srcSubnet, srcVirtualNetworkType, srcVirtualNetwork, srcNetworkPolicy, srcSecurityGroup);
var dstAddress = ContrailUtils.createAddress(dstAddressType, dstSubnet, dstVirtualNetworkType, dstVirtualNetwork, dstNetworkPolicy, dstSecurityGroup);
theRule.addSrcAddresses(srcAddress);
theRule.addDstAddresses(dstAddress);

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

theRule.setActionList(actions);

item.update();