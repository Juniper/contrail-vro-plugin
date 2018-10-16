rule.setDirection(direction);

var actions = new ContrailActionListType()
actions.setSimpleAction(action);
rule.setActionList(actions);

var endpoint1 = ContrailUtils.createEndpoint(endpoint1Type, endpoint1Tags, endpoint1VirtualNetwork, endpoint1AddressGroup)
var endpoint2 = ContrailUtils.createEndpoint(endpoint2Type, endpoint2Tags, endpoint2VirtualNetwork, endpoint2AddressGroup)
rule.setEndpoint1(endpoint1)
rule.setEndpoint2(endpoint2)

var mTags = new ContrailFirewallRuleMatchTagsType([]);
if(matchTags != null) {
    mTags = new ContrailFirewallRuleMatchTagsType(matchTags);
}
rule.setMatchTags(mTags);

if(serviceType == "manual") {
    rule.clearServiceGroup();
    var service = new ContrailFirewallServiceType();
    service.setProtocol(serviceProtocol);
    service.setSrcPorts(ContrailUtils.parsePortsOfFirewallRule(serviceSrcPorts));
    service.setDstPorts(ContrailUtils.parsePortsOfFirewallRule(serviceDstPorts));
    rule.setService(service);
} else {
    if(serviceReference != null) {
        rule.setService(new ContrailFirewallServiceType());
        rule.addServiceGroup(serviceReference);
    }
}

// workaround for bug 1797825
rule.nullifyTag();

rule.update();