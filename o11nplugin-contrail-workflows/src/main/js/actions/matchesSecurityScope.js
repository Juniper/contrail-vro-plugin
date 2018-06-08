if (children === null) {
    return null;
}

if (!arrayMode) {
    children = [children];
}

var expectedUuid = null;
if (directMode && typeof parent.uuid !== "undefined") {
    expectedUuid = parent.uuid;
} else {
    // extract project uuid from rule
    expectedUuid = parent.parentUuid;
}

var someBadChild = null;
for (var idx in children) {
    // Child is considered erroneous if it is non-global (project-scope) AND it's project is different to the one we test for
    var child = children[idx];
    if (child.parentType === "project" && expectedUuid !== child.parentUuid) {
        someBadChild = child;
    }
}

if (someBadChild != null) {
    return someBadChild.name + " comes from an inaccessible project."
} else {
    return null;
}