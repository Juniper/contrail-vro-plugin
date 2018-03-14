var index = ContrailUtils.ruleStringToIndex(allowedAddressPair);
var executor = ContrailConnectionManager.executor(item.internalId.toString());

var list = executor.getAllowedAddressPairs(item, interfaceName);
list.splice(index, 1);
var interfaceIndex = executor.getInterfaceIndexByName(item, interfaceName);

item.getProperties().getInterfaceList()[interfaceIndex].setAllowedAddressPairs(new ContrailAllowedAddressPairs(list));

executor.updateServiceInstance(item);