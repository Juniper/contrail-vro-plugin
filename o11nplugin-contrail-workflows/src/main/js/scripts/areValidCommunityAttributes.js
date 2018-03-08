if (!communityAttributes) return null;

for (i = 0; i < communityAttributes.length; ++i) {
    var attribute = communityAttributes[i];
    if (!ContrailUtils.isValidCommunityAttribute(attribute)) {
        return "Invalid community attribute format: " + attribute + " ; Use 'number:number'.";
    }
}
return null;