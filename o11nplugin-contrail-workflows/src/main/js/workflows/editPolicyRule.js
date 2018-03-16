// TODO: update "actions" with Services, Mirror

var index = ContrailUtils.ruleStringToIndex(rule);
var theRule = parent.entries.policyRule[index];

var qosName = null;
if (qos) {
    qosName = qos.getQualifiedName().join(":");
}

var srcAddress = ContrailUtils.createAddress(srcAddressType, srcAddressCidr, srcAddressNetworkType, srcAddressNetwork, srcAddressPolicy, srcAddressSecurityGroup);
var dstAddress = ContrailUtils.createAddress(dstAddressType, dstAddressCidr, dstAddressNetworkType, dstAddressNetwork, dstAddressPolicy, dstAddressSecurityGroup);
theRule.protocol = protocol;
theRule.direction = direction;

theRule.clearSrcAddresses();
theRule.clearDstAddresses();
theRule.clearSrcPorts();
theRule.clearDstPorts();

theRule.addSrcAddresses(srcAddress);
theRule.addDstAddresses(dstAddress);

ContrailUtils.parsePorts(srcPorts).forEach(function(port) {
   theRule.addSrcPorts(port);
});
ContrailUtils.parsePorts(dstPorts).forEach(function(port) {
   theRule.addDstPorts(port);
});

var actions = new ContrailActionListType()
actions.setSimpleAction(simpleAction);
actions.setLog(log);
actions.setQosAction(qosName);

theRule.setActionList(actions);

var executor = ContrailConnectionManager.executor(parent.internalId.toString());
executor.updateNetworkPolicy(parent);