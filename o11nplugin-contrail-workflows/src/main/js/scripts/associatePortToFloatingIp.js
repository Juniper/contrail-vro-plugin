item.addPort(port);
var executor = ContrailConnectionManager.executor(item.internalId.toString());
var instanceIp = executor.getInstanceIpOfPort(port);

if (fixedIpAddress && instanceIp && instanceIp.getAddress()){
    item.setFixedIpAddress(instanceIp.getAddress());
}
executor.updateFloatingIp(item);
