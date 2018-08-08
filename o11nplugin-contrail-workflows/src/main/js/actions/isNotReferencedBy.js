
if (child === null) {
    return null
}

methodName = "isReferencedBy" + parent.getObjectClassName()
toInvoke = "child." + methodName + "(parent)";

if (eval(toInvoke)) {
    return "Already referenced"
}

return null