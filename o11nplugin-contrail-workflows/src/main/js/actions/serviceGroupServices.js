var actionResult = new Array();

if (!item) return actionResult;

var serviceList = item.getFirewallServiceList();
if (!serviceList) return actionResult;

var services = serviceList.getFirewallService();
if (! services) return actionResult;

services.forEach(function (value, index) {
    actionResult.push(ContrailUtils.firewallServiceToString(value, index));
});
return actionResult;