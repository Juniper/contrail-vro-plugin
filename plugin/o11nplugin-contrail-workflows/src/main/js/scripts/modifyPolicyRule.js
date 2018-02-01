
// TODO: update "actions" with Services, Mirror, QoS

var index = ContrailUtils.ruleStringToIndex(rule);
var ruule = parent.entries.policyRule[index];

var simpleAction = ContrailUtils.lowercase(simple_action)
var qosName = qos.getQualifiedName().join(":")

ruule.protocol = protocol;
ruule.direction = direction;
ruule.srcAddr = [ContrailUtils.createAddress(src_address_type, src_address_cidr, src_address_network, src_address_policy)];
ruule.dstAddr = [ContrailUtils.createAddress(dst_address_type, dst_address_cidr, dst_address_network, dst_address_policy)];
ruule.srcPorts = ContrailUtils.parsePorts(src_ports);
ruule.dstPorts = ContrailUtils.parsePorts(dst_ports);
ruule.actions = new ContrailActionListType(simpleAction, null, null, null, null, null, log, null, qosName);

var executor = ContrailConnectionManager.getExecutor(parent.getInternalId().toString());
executor.updateNetworkPolicy(parent);