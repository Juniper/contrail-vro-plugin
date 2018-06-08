var someBadChild = null;

if ()

var errorList = children.forEach(function(child){
    // global objects can be used by anyone
    System.log(child.parentType);
    if (child.parentType != "project") {
        return;
    }
    // non-global objects can be used only by objects from the same project
    System.log(parentUuid);
    System.log(child.parentUuid);
    if (parentUuid === child.parentUuid) {
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