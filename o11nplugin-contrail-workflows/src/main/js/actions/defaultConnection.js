var connections = ContrailConnectionManager.connections;
if (connections.length == 1) {
    var connectionId = connections[0].name;
    return ContrailConnectionManager.connection(connectionId);
}
return null;