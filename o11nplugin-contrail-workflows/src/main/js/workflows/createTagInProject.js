var type = typeName.toLowerCase();
item = new ContrailTag();
item.setName(type+"="+value);
item.setParentProject(parent);
item.setValue(value);
item.setTypeName(type);
item.create();