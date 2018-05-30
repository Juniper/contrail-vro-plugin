var index = ContrailUtils.stringToIndex(rule);

var list = item.getEntries().getPolicyRule();
list.splice(index, 1);
item.setEntries(new ContrailPolicyEntriesType(list));

item.update();