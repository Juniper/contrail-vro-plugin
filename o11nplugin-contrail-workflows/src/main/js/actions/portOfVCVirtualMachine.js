var id = vcvm.instanceId;

var vm = null;
var ports = null;

while(true) {
    vm = connection.findVirtualMachine(id);
    if(vm) {
		ports = vm.portBackRefs;
		if(ports.length > 0)
		   break;
    }
    System.sleep(500);
}

return ports[0];