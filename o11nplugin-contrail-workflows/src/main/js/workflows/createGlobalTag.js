var type = typeName.toLowerCase();
item = new ContrailTag();
item.setName(type+"="+value);
item.setParentConfigRoot(new ContrailConfigRoot());
item.setParentConnection(parent);
item.setValue(value);
item.setTypeName(type);
item.create();