var actionResult = new Array();

if (!item) return actionResult;

var entries = item.getEntries();
if (!entries) return actionResult;

var rules = entries.getPolicyRule();
if (!rules) return actionResult;

rules.forEach(function (value, index) {
    actionResult.push(ContrailUtils.ruleToString(value, index));
});
return actionResult;