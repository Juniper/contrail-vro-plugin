// global objects can be used by anyone
System.log(child.parentType);
if (child.parentType != "project") {
    return null;
}
// non-global objects can be used only by objects from the same project
System.log(parentUuid);
System.log(child.parentUuid);
if (parentUuid === child.parentUuid) {
    return null;
}
return "Child object must be global or be from the same project.";