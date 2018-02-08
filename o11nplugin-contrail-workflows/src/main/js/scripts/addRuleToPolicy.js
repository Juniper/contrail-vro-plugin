
// TODO: update "actions" with Services, Mirror, QoS

var ruleSequence = new ContrailSequenceType(-1, -1);

var ruleUuid = ContrailUtils.randomUUID();

var parsedSrcPorts = ContrailUtils.parsePorts(srcPorts);
var parsedDstPorts = ContrailUtils.parsePorts(dstPorts);

var application = [];

var srcAddr = [ContrailUtils.createAddress(srcAddressType, srcAddressCidr, srcAddressNetwork, srcAddressPolicy)];
var dstAddr = [ContrailUtils.createAddress(dstAddressType, dstAddressCidr, dstAddressNetwork, dstAddressPolicy)];

var qosName = qos.getQualifiedName().join(":")

var actions = new ContrailActionListType(simpleAction, null, null, null, null, null, log, null, qosName);

var rule = new ContrailPolicyRuleType(ruleSequence, ruleUuid, direction, ContrailUtils.lowercase(protocol), srcAddr, parsedSrcPorts, application, dstAddr, parsedDstPorts, actions);

var id = parent.getInternalId().toString();
var executor = ContrailConnectionManager.getExecutor(id);

parent.getEntries().addPolicyRule(rule);

executor.updateNetworkPolicy(parent);