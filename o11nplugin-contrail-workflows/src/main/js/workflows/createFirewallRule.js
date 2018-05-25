var rule = new ContrailFirewallRule();
var ruleUuid = ContrailUtils.randomUUID();
rule.setUuid(ruleUuid);
rule.setName(ruleUuid);
if (typeof parentProject === 'undefined') {
    // if the parent project is undefined, we're using the policy management version
    var defaultPolicyManagement = parentConnection.findPolicyManagementByFQName("default-policy-management");
    rule.setParentPolicyManagement(defaultPolicyManagement);
} else {
    rule.setParentProject(parentProject);
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