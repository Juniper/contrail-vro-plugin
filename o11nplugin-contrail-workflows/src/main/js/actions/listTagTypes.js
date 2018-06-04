if (!item) return ["application", "deployment", "label", "site", "tier"];
var connection = item.getConnection();
return connection.listTagTypes();