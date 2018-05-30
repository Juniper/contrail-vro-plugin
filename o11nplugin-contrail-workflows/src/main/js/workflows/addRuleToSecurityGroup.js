var ruleUuid = ContrailUtils.randomUUID();

var parsedPorts = ContrailUtils.parsePortsOfSecurityGroupRule(ports);
var addr = [ContrailUtils.createAddress(addressType, addressCidr, null, null, null, addressSecurityGroup)];

var localPorts = ContrailUtils.parsePortsOfSecurityGroupRule("0-65535");
var localAddr = [ContrailUtils.createAddress("Security Group", null, null, null, null, null)];

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

var rule = new ContrailPolicyRuleType(null, ruleUuid, trafficDirection, protocol, srcAddr, srcPorts, null, dstAddr, dstPorts, null, ethertype);
var rules = item.getEntries();
if (!rules) {
    rules = new ContrailPolicyEntriesType();
    item.setEntries(rules);
}
rules.addPolicyRule(rule);

item.update();