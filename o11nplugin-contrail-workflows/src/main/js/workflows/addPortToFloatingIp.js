item.addPort(port);
var instanceIp = port.instanceIp();

if (fixedIpAddress && instanceIp && instanceIp.getAddress()){
    item.setFixedIpAddress(instanceIp.getAddress());
}

item.update();
