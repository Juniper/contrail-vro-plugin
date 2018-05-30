var index = ContrailUtils.stringToIndex(service);

var services = item.getFirewallServiceList().getFirewallService();
services.splice(index, 1);
item.setFirewallServiceList(new ContrailFirewallServiceGroupType(services));

item.update();