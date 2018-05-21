var index = ContrailUtils.ruleStringToIndex(rule);

var list = new Array(parent.getEntries().getPolicyRule());
list.splice(index, 1);

parent.setEntries(new ContrailPolicyEntriesType(list));

parent.update();