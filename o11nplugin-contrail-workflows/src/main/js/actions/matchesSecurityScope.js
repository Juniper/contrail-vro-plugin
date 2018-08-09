if (children === null) {
    return null;
}

if (!arrayMode) {
    children = [children];
}

var expectedUuid = null;

if (directMode) {
    if (parent.objectClassName == "PolicyManagement" && parent.name == "draft-policy-management") {
        expectedUuid = parent.parentUuid;
    } else if (typeof parent.uuid !== "undefined") {
        expectedUuid = parent.uuid;
    }
} else {
    // extract project uuid from rule
    expectedUuid = parent.nonDraftParentUuid;
}

var someBadChild = null;
for (var idx in children) {
    // Child is considered erroneous if it is non-global (project-scope) AND it's project is different to the one we test for
    var child = children[idx];
    if (child.nonDraftParentType === "project" && expectedUuid !== child.nonDraftParentUuid) {
        someBadChild = child;
    }
}

if (someBadChild != null) {
    return someBadChild.name + " comes from an inaccessible project."
} else {
    return null;
}