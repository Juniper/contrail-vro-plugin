var rule = new ContrailFirewallRule();
var ruleUuid = ContrailUtils.randomUUID();
rule.setUuid(ruleUuid);
rule.setName(ruleUuid);
if (parentProject) {
    rule.setParentProject(parentProject);
}
if (parentPolicyManagement) {
    rule.setParentPolicyManagement(parentPolicyManagement);
}
rule.setDirection(direction);

var actions = new ContrailActionListType()
actions.setSimpleAction(action);
rule.setActionList(actions);

var endpoint1 = ContrailUtils.createEndpoint(endpoint1Type, endpoint1Tags, endpoint1VirtualNetwork, endpoint1AddressGroup);
var endpoint2 = ContrailUtils.createEndpoint(endpoint2Type, endpoint2Tags, endpoint2VirtualNetwork, endpoint2AddressGroup);
rule.setEndpoint1(endpoint1);
rule.setEndpoint2(endpoint2);

var mTags = new ContrailFirewallRuleMatchTagsType([]);
if(matchTags != null) {
    mTags = new ContrailFirewallRuleMatchTagsType(matchTags);
}
rule.setMatchTags(mTags);

if(serviceType == "manual") {
    var service = new ContrailFirewallServiceType();
    service.setProtocol(serviceProtocol);
    service.setSrcPorts(ContrailUtils.parsePortsOfFirewallRule(serviceSrcPorts));
    service.setDstPorts(ContrailUtils.parsePortsOfFirewallRule(serviceDstPorts));
    rule.setService(service);
} else {
    if(serviceReference != null) {
        rule.addServiceGroup(serviceReference);
    }
}

rule.create();