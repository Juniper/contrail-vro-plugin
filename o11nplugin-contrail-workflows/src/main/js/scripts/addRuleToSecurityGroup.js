
var ruleSequence = new ContrailSequenceType(-1, -1);
var ruleUuid = ContrailUtils.randomUUID();

var parsedPorts = ContrailUtils.parsePorts(ports);
var addr = [ContrailUtils.createAddress(addressType, addressCidr, null, null, addressSecurityGroup)];

var localPorts = ContrailUtils.parsePorts("0-65535");
var localAddr = [ContrailUtils.createAddress("Security Group", null, null, null, null)];

var trafficDirection = ">"

var srcAddr, dstAddr, srcPorts, dstPorts;

if (direction == "ingress") {
    srcAddr = addr;
    srcPorts = parsedPorts;
    dstAddr = localAddr;
    dstPorts = localPorts;
} else {
    dstAddr = addr;
    dstPorts = parsedPorts;
    srcAddr = localAddr;
    srcPorts = localPorts;
}

var rule = new ContrailPolicyRuleType(ruleSequence, ruleUuid, direction, ContrailUtils.lowercase(protocol), srcAddr, srcPorts, null, dstAddr, dstPorts, null, ethertype);

var id = parent.getInternalId().toString();
var executor = ContrailConnectionManager.getExecutor(id);

parent.getEntries().addPolicyRule(rule);

executor.updateSecurityGroup(parent);