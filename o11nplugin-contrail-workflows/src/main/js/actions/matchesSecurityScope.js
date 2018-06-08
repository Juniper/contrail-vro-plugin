if (!arrayMode) {
    children = [children]
}

var expectedUuid = null
if (directMode && typeof parent.uuid !== 'undefined') {
    expectedUuid = parent.uuid
} else {
    // extract project uuid from rule
    expectedUuid = parent.parentUuid
}

var someBadChild = null;
var errorList = children.forEach(function(child){
    // global objects can be used by anyone
    if (child.parentType !== "project") {
        return;
    }
    // non-global objects can be used only by objects from the same project
    if (expectedUuid === child.parentUuid) {
        return;
    }
    someBadChild = child.name;
});

if (someBadChild != null) {
    return someBadChild + " comes from an inaccessible project."
} else {
    return null;
}