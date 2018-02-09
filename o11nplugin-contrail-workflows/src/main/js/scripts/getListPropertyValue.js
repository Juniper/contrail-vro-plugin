// TODO make it more general
var objectIndex = ContrailUtils.ruleStringToIndex(childItem);
var child = eval("parentItem." + listAccessor + "[" + objectIndex + "]");
return eval("child." + propertyPath);