var actionResult = new Array();
if (!serviceInstance || !interfaceName) return actionResult;
var serviceInstanceId = serviceInstance.internalId;
var executor = ContrailConnectionManager.executor(serviceInstanceId.toString());
var pairs = executor.getAllowedAddressPairs(serviceInstance, interfaceName);

pairs.forEach(function (value, index) {
	actionResult.push(ContrailUtils.allowedAddressPairToString(value, index));
});
return actionResult;