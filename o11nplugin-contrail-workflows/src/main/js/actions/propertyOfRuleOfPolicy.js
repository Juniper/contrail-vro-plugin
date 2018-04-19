switch (String(propertyName)) {
    case "defineService":
        return networkPolicy.rulePropertyDefineService(rule);
    case "defineMirror":
        return networkPolicy.rulePropertyDefineMirror(rule);
    case "services":
        return networkPolicy.rulePropertyServices(rule);
    default:
        return null;
}