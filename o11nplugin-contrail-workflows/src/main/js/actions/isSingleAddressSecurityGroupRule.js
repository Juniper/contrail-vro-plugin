if (!input) return null;
var theRule = ContrailUtils.stringToRuleFromSecurityGroup(input.trim(), securityGroup);

if (theRule.srcAddresses.length == 1 && theRule.dstAddresses.length == 1){
    return null;
}

return "Rules with multiple source or destination addresses are not supported in this workflow";