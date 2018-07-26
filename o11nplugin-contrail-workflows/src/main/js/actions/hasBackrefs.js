if (!item) {
    return null;
}

if (item.countBackrefs() > 0) {
    return "Object is still referenced by other objects";
}
return null;