var index = ContrailUtils.stringToIndex(rule);
var theRule = item.entries.policyRule[index];

var parsedPorts = ContrailUtils.parsePortsOfSecurityGroupRule(ports);
var addr = ContrailUtils.createAddress(addressType, addressCidr, null, null, null, addressSecurityGroup);

var localPorts = ContrailUtils.parsePortsOfSecurityGroupRule("0-65535");
var localAddr = ContrailUtils.createAddress("Security Group", null, null, null, null, null);

var trafficDirection = ">"

var srcAddr, dstAddr, srcPorts, dstPorts;

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

item.update();