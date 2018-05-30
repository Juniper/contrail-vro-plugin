if (!input) return null;
var theRule = ContrailUtils.stringToRuleFromNetworkPolicy(input.trim(), networkPolicy);

if (theRule.srcAddresses.length == 1 && theRule.dstAddresses.length == 1){
    return null;
}

return "Rules with multiple source or destination addresses are not supported in this workflow";