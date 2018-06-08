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
    System.log(child.parentType);
    if (child.parentType !== "project") {
        return;
    }
    // non-global objects can be used only by objects from the same project
    System.log(expectedUuid);
    System.log(child.parentUuid);
    if (expectedUuid === child.parentUuid) {
        return;
    }
    someBadChild = child.name;
});

System.log(someBadChild);

if (someBadChild != null) {
    return someBadChild + " comes from an unaccessible project."
} else {
    return null;
}