
if (children === null) {
    return null
}

methodName = "isReferencedBy" + parent.getObjectClass()
toInvoke = "children." + methodName + "(parent)";

if (eval(toInvoke)) {
    return "Already referenced"
}

return null