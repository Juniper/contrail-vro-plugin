var ruleUuid = ContrailUtils.randomUUID();

var parsedSrcPorts = ContrailUtils.parsePorts(srcPorts);
var parsedDstPorts = ContrailUtils.parsePorts(dstPorts);

var srcAddr = [ContrailUtils.createAddress(srcAddressType, srcAddressCidr, srcAddressNetwork, srcAddressPolicy, srcAddressSecurityGroup)];
var dstAddr = [ContrailUtils.createAddress(dstAddressType, dstAddressCidr, dstAddressNetwork, dstAddressPolicy, dstAddressSecurityGroup)];

var application = [];

var qosName = null;
if (qos) {
    qosName = qos.getQualifiedName().join(":");
}

var mirrorAction = null;
if (mirror) {
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
actions.setQosAction(qosName);
if (serviceInstances != null) {
    serviceInstances.forEach(function(instance){
        actions.addApplyService(instance.getQualifiedName().join(":"));
    });
}
actions.setMirrorTo(mirrorAction);

var rule = new ContrailPolicyRuleType(null, ruleUuid, direction, protocol, srcAddr, parsedSrcPorts, application, dstAddr, parsedDstPorts, actions);
var rules = parent.getEntries();
if (!rules) {
    rules = new ContrailPolicyEntriesType();
    parent.setEntries(rules);
}
rules.addPolicyRule(rule);

var id = parent.internalId;
var executor = ContrailConnectionManager.executor(id.toString());
executor.updateNetworkPolicy(parent);