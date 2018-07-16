var rules = item.getFirewallRule();
var count = 0;
if (rules){
    count = rules.length;
}
var sequence = new ContrailFirewallSequence(count.toString());

item.addFirewallRule(child, sequence);

item.update();