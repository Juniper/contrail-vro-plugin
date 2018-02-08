var actionResult = new Array();
var rules = netpolicy.getEntries().getPolicyRule()

rules.forEach(function (value, index) {
	actionResult.push(ContrailUtils.ruleToString(value, index));
});
return actionResult;