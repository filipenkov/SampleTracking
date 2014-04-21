AJS.test.require("jira.webresources:avatar-picker");

test("buildCompleteUrl should work for URLs with or without query params", function() {
    var restUrl = "http://localhost:8090/jira/rest/api/latest/project/HSP-1";
    var projAvatarStore = new JIRA.AvatarStore({
        restQueryUrl: "blah",
        restCreateTempUrl: "blah",
        restUpdateTempUrl: "blah",
        defaultAvatarId: 1000
    });
    equal(projAvatarStore._buildCompleteUrl(restUrl), "http://localhost:8090/jira/rest/api/latest/project/HSP-1", "URL for project avatar");

    restUrl = "http://localhost:8090/jira/rest/api/latest/user";
    var userAvatarStore = new JIRA.AvatarStore({
        restQueryUrl: "blah",
        restCreateTempUrl: "blah",
        restUpdateTempUrl: "blah",
        restParams: { username: "fred" },
        defaultAvatarId: 1000
    });
    equal(userAvatarStore._buildCompleteUrl(restUrl), "http://localhost:8090/jira/rest/api/latest/user?username=fred", "URL for user avatar");
});