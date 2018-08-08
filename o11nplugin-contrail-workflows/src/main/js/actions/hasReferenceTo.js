
if (children === null) {
    return null
}

parent_id = parent.toString().split('.').pop()
className = parent_id.split('@')[0]
methodName = "has" + className + "Reference"
toInvoke = "children." + methodName + "(parent)";

if (eval(toInvoke)) {
    return "Already referenced"
}

return null