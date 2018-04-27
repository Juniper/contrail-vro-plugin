var id = vcvm.instanceId;

// default to 15 second timeout
if(!timeout) timeout = 15;
var start = new Date();
var timeoutDate = new Date(start.getTime() + (timeout * 1000));

var vm = null;
var ports = null;

while(true) {
    vm = connection.findVirtualMachine(id);
    if(vm) {
        ports = vm.portBackRefs;
        if(ports.length > 0)
           break;
    }
    if(new Date().getTime() > timeoutDate.getTime())
        throw "Failed to retrieve port of virtual machine due to timeout.";
    System.sleep(500);
}

return ports[0];