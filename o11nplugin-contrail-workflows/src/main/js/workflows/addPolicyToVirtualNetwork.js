var policies = item.getNetworkPolicy();
var count = policies.length;
var sequence = new ContrailSequenceType(count, 0);
var attribute = new ContrailVirtualNetworkPolicyType(sequence);

item.addNetworkPolicy(child, attribute);

item.update();