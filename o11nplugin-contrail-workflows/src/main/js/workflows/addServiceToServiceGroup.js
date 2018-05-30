var service = new ContrailFirewallServiceType();
service.protocol = protocol;
service.srcPorts = ContrailUtils.parseFirewallServicePorts("any");
service.dstPorts = ContrailUtils.parseFirewallServicePorts(port);

var services = item.getFirewallServiceList();
if (!services) {
    services = new ContrailFirewallServiceGroupType();
    item.setFirewallServiceList(services);
}
services.addFirewallService(service);

item.update();