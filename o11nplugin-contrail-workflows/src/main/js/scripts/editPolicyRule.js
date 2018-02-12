
// TODO: update "actions" with Services, Mirror

var index = ContrailUtils.ruleStringToIndex(rule);
var theRule = parent.entries.policyRule[index];

var qosName = null;
if (qos) {
    qosName = qos.getQualifiedName().join(":");
}

theRule.protocol = protocol;
theRule.direction = direction;
theRule.clearSrcAddresses();
theRule.addSrcAddresses(ContrailUtils.createAddress(srcAddressType, srcAddressCidr, srcAddressNetwork, srcAddressPolicy));
theRule.clearDstAddresses();
theRule.addDstAddresses(ContrailUtils.createAddress(dstAddressType, dstAddressCidr, dstAddressNetwork, dstAddressPolicy));
theRule.clearSrcPorts();
ContrailUtils.parsePorts(srcPorts).forEach(function(port) {
   theRule.addSrcPorts(port);
});
var parsedDstPorts = ContrailUtils.parsePorts(dstPorts);
theRule.clearDstPorts();
ContrailUtils.parsePorts(dstPorts).forEach(function(port) {
   theRule.addDstPorts(port);
});
theRule.setActionList(new ContrailActionListType(simpleAction, null, null, null, null, null, log, null, qosName));

var executor = ContrailConnectionManager.executor(parent.internalId.toString());
executor.updateNetworkPolicy(parent);