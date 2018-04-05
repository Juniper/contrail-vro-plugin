item.addPort(child);
var instanceIp = child.instanceIp();

if (fixedIpAddress && instanceIp && instanceIp.getAddress()){
    item.setFixedIpAddress(instanceIp.getAddress());
}

item.update();
