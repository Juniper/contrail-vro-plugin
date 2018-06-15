var connections = ContrailConnectionManager.connections;
if (connections.length == 1) return connections[0];
return null;