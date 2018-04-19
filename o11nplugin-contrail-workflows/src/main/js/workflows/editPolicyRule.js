var index = ContrailUtils.ruleStringToIndex(rule);
var theRule = parent.entries.policyRule[index];

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
var srcAddress = ContrailUtils.createAddress(srcAddressType, srcAddressCidr, srcAddressNetworkType, srcAddressNetwork, srcAddressPolicy, srcAddressSecurityGroup);
var dstAddress = ContrailUtils.createAddress(dstAddressType, dstAddressCidr, dstAddressNetworkType, dstAddressNetwork, dstAddressPolicy, dstAddressSecurityGroup);
theRule.addSrcAddresses(srcAddress);
theRule.addDstAddresses(dstAddress);

var mirrorAction = null;
if (defineMirror) {
    var instanceName = analyzerName;
    if (analyzerInstance != null) {
        instanceName = analyzerInstance.getQualifiedName().join(":");
    }
    var routingInstanceName;
    if (routingInstance != null) {
        routingInstanceName = routingInstance.getQualifiedName().join(":") + ":" + routingInstance.getName();
    }
    mirrorAction = new ContrailMirrorActionType();
    mirrorAction.setAnalyzerName(instanceName);
    mirrorAction.setAnalyzerIpAddress(analyzerIP);
    mirrorAction.setAnalyzerMacAddress(analyzerMac);
    mirrorAction.setRoutingInstance(routingInstanceName);
    mirrorAction.setUdpPort(udpPort);
    mirrorAction.setJuniperHeader(juniperHeader == "enabled");
    mirrorAction.setNhMode(nexthopMode);
    mirrorAction.setNicAssistedMirroring(mirrorType == "NIC Assisted");
    mirrorAction.setNicAssistedMirroringVlan(nicAssistedVlan);
    if (nexthopMode == "static") {
        staticNhHeader = new ContrailStaticMirrorNhType();
        staticNhHeader.setVtepDstIpAddress(vtepDestIp);
        staticNhHeader.setVtepDstMacAddress(vtepDestMac);
        staticNhHeader.setVni(vni);
        mirrorAction.setStaticNhHeader(staticNhHeader);
    }
}

var actions = new ContrailActionListType()
actions.setSimpleAction(simpleAction);
actions.setLog(log);
if (serviceInstances != null) {
    serviceInstances.forEach(function(instance){
        actions.addApplyService(instance.getQualifiedName().join(":"));
    });
}
actions.setMirrorTo(mirrorAction);

theRule.setActionList(actions);

parent.update();