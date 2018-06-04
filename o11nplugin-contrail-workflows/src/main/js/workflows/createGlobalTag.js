item = new ContrailTag();
item.setName(typeName+"="+value);
item.setParentConfigRoot(new ContrailConfigRoot());
item.setParentConnection(parent);
item.setValue(value);
item.setTypeName(typeName);
item.create();