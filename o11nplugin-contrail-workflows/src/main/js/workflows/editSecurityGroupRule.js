var index = ContrailUtils.ruleStringToIndex(rule);
var theRule = parent.entries.policyRule[index];

var parsedPorts = ContrailUtils.parsePortsOfSecurityGroupRule(ports);
var addr = ContrailUtils.createAddress(addressType, addressCidr, null, null, null, addressSecurityGroup);

var localPorts = ContrailUtils.parsePortsOfSecurityGroupRule("0-65535");
var localAddr = ContrailUtils.createAddress("Security Group", null, null, null, null, null);

var trafficDirection = ">"

var srcAddr, dstAddr, srcPorts, dstPorts;

// src ports need to be added as pairs (start, end).
// dst ports need to be added as PortType objects.
theRule.clearSrcAddresses()
theRule.clearDstAddresses()
theRule.clearSrcPorts()
theRule.clearDstPorts()

if (direction == "ingress") {
    theRule.addSrcAddresses(addr);
    parsedPorts.forEach(function(port) {
        theRule.addSrcPorts(port);
    });
    theRule.addDstAddresses(localAddr);
    theRule.addDstPorts(localPorts[0]);
} else {
    theRule.addDstAddresses(addr);
    parsedPorts.forEach(function(port) {
        theRule.addDstPorts(port);
    });
    theRule.addSrcAddresses(localAddr);
    theRule.addSrcPorts(localPorts[0]);
}

theRule.protocol = protocol;
theRule.ethertype = ethertype;

var id = parent.internalId;
var executor = ContrailConnectionManager.executor(id.toString());
executor.updateSecurityGroup(parent);