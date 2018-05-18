var fqName = "default-domain:vCenter:"+portGroupName;

// default to 15 second timeout
if(!timeout) timeout = 15;
var start = new Date();
var timeoutDate = new Date(start.getTime() + (timeout * 1000));

var vn = null;

while(true) {
    vn = connection.findVirtualNetworkByFQName(fqName);
    if(vn) break;
    if(new Date().getTime() > timeoutDate.getTime())
        throw "Failed to retrieve virtual network due to timeout.";
    System.sleep(500);
}

return vn;