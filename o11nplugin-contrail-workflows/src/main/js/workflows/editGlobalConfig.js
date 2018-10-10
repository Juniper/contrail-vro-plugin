var globalSystemConfig = parentConnection.globalSystemConfig();
var globalVrouterConfig = parentConnection.globalVrouterConfig();

globalSystemConfig.setEnableSecurityPolicyDraft(enableSecurityPolicyDraft);
globalVrouterConfig.setEncapsulationPriorities(new ContrailEncapsulationPrioritiesType(encapsulationPriorities));
globalVrouterConfig.setForwardingMode(forwardingMode);

globalVrouterConfig.update();
globalSystemConfig.update();