
// TODO: update "actions" with Services, Mirror

var index = ContrailUtils.ruleStringToIndex(rule);
var ruule = parent.entries.policyRule[index];

var qosName = qos.getQualifiedName().join(":")

ruule.protocol = protocol;
ruule.direction = direction;
ruule.srcAddr = [ContrailUtils.createAddress(srcAddressType, srcAddressCidr, srcAddressNetwork, srcAddressPolicy)];
ruule.dstAddr = [ContrailUtils.createAddress(dstAddressType, dstAddressCidr, dstAddressNetwork, dstAddressPolicy)];
ruule.srcPorts = ContrailUtils.parsePorts(srcPorts);
ruule.dstPorts = ContrailUtils.parsePorts(dstPorts);
ruule.actions = new ContrailActionListType(simpleAction, null, null, null, null, null, log, null, qosName);

var executor = ContrailConnectionManager.executor(parent.internalId.toString());
executor.updateNetworkPolicy(parent);