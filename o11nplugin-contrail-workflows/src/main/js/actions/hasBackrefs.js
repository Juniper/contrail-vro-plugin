if (!item) {
    return null;
}

if (item.backrefCount() > 0) {
    return "Object is still referenced by other objects";
}
return null;