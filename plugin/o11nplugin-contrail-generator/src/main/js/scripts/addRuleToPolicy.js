
// TODO: update "actions" with Services, Mirror, QoS

var rule_sequence = new ContrailSequenceType(-1, -1);

var ruleUuid = ContrailUtils.randomUUID();

var srcPorts = ContrailUtils.parsePorts(src_ports);
var dstPorts = ContrailUtils.parsePorts(dst_ports);

var application = [];

var srcAddr = [ContrailUtils.createAddress(src_address_type, src_address_cidr, src_address_network, src_address_policy)];
var dstAddr = [ContrailUtils.createAddress(dst_address_type, dst_address_cidr, dst_address_network, dst_address_policy)];

var actions = new ContrailActionListType(ContrailUtils.lowercase(action));

var rule = new ContrailPolicyRuleType(rule_sequence, ruleUuid, direction, ContrailUtils.lowercase(protocol), srcAddr, srcPorts, application, dstAddr, dstPorts, actions);

var id = parent.getInternalId().toString();
var executor = ContrailConnectionManager.getExecutor(id);

parent.getEntries().addPolicyRule(rule);

executor.updateNetworkPolicy(parent);