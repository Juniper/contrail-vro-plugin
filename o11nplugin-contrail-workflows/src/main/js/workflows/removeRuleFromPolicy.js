var index = ContrailUtils.ruleStringToIndex(rule);

var list = parent.getEntries().getPolicyRule();
list.remove(list[index]);
parent.setEntries(new ContrailPolicyEntriesType(list));

parent.update();