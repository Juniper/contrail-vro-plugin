child.addPort(item);
var instanceIp = item.instanceIp();

if (fixedIpAddress && instanceIp && instanceIp.getAddress()){
    child.setFixedIpAddress(instanceIp.getAddress());
}

child.update();
