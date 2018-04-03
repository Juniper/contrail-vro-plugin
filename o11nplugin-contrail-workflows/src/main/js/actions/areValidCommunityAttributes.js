if (!communityAttributes) return null;

for (var i in communityAttributes) {
    var attribute = communityAttributes[i];
    if (!ContrailUtils.isValidCommunityAttribute(attribute)) {
        return "Invalid community attribute format: " + attribute +
            "\n Valid format is 'number:number'. First number must be in [0, 65535].";
    }
}
return null;