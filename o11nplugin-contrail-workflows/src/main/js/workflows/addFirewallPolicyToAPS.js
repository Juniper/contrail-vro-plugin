var policies = item.getFirewallPolicy();
var count = 0;
if (policies){
    count = policies.length;
}
var sequence = new ContrailFirewallSequence(count.toString());

item.addFirewallPolicy(child, sequence);

item.update();