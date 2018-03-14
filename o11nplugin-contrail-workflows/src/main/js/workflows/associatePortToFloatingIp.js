item.addPort(port);
var executor = ContrailConnectionManager.executor(item.internalId.toString());
var instanceIp = executor.instanceIpOfPort(port);

if (fixedIpAddress && instanceIp && instanceIp.getAddress()){
    item.setFixedIpAddress(instanceIp.getAddress());
}
executor.updateFloatingIp(item);
