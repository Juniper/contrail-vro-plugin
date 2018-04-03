if (!item) return null;
var id = item.internalId;
var connection = ContrailConnectionManager.connection(id.toString());
connection.internalId = id;
return connection;