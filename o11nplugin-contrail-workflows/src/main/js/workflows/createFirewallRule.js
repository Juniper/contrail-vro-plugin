var rule = new ContrailFirewallRule();
var ruleUuid = ContrailUtils.randomUUID();
rule.setUuid(ruleUuid);
if (parentProject) {
    rule.setParentProject(parentProject);
    System.log(parentProject);
}
if (parentPolicyManagement) {
    rule.setParentPolicyManagement(parentPolicyManagement);
    System.log(parentPolicyManagement);
}
rule.setDirection(direction);
System.log(direction);

var actions = new ContrailActionListType()
actions.setSimpleAction(action);
rule.setActionList(actions);
System.log(actions);

var endpoint1 = ContrailUtils.createEndpoint(endpoint1Type, endpoint1Tags, endpoint1VirtualNetworks, endpoint1AddressGroups)
System.log(endpoint1);
var endpoint2 = ContrailUtils.createEndpoint(endpoint2Type, endpoint2Tags, endpoint2VirtualNetworks, endpoint2AddressGroups)
System.log(endpoint2);
rule.setEndpoint1(endpoint1)
rule.setEndpoint2(endpoint2)

System.log("beforeMatchTags");

if(matchTags != null) {
    var mTags = new ContrailFirewallRuleMatchTagsType(matchTags);
    rule.setMatchTags(mTags);
}

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