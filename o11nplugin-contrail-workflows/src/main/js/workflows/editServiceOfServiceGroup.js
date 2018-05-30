var index = ContrailUtils.stringToIndex(service);
var theService = item.firewallServiceList.firewallService[index];

theService.protocol = protocol;
theService.dstPorts = ContrailUtils.parseFirewallServicePorts(port);

item.update();