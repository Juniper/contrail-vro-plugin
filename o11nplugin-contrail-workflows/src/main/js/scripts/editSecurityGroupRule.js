var index = ContrailUtils.ruleStringToIndex(rule);
var ruule = parent.entries.policyRule[index];

var ruleSequence = new ContrailSequenceType(-1, -1);
var ruleUuid = ContrailUtils.randomUUID();

var parsedPorts = ContrailUtils.parsePorts(ports);
var addr = [ContrailUtils.createAddress(addressType, addressCidr, null, null, addressSecurityGroup)];

var localPorts = ContrailUtils.parsePorts("0-65535");
var localAddr = [ContrailUtils.createAddress("Security Group", null, null, null, null)];

var trafficDirection = ">"

var srcAddr, dstAddr, srcPorts, dstPorts;

if (direction == "ingress") {
    ruule.srcAddr = addr;
    ruule.srcPorts = parsedPorts;
    ruule.dstAddr = localAddr;
    ruule.dstPorts = localPorts;
} else {
    ruule.dstAddr = addr;
    ruule.dstPorts = parsedPorts;
    ruule.srcAddr = localAddr;
    ruule.srcPorts = localPorts;
}

ruule.protocol = protocol;
ruule.ethertype = ethertype;

var id = parent.internalId;
var executor = ContrailConnectionManager.getExecutor(id.toString());
executor.updateSecurityGroup(parent);