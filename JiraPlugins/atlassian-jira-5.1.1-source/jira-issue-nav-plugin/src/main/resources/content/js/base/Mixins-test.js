AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:backbone-ext");

module('JIRA.Issues.Mixins');

test("CreateMethodName", function() {
    equal(JIRA.Issues.Mixins.createMethodName("blah", "blah"), "blahBlah");
    equal(JIRA.Issues.Mixins.createMethodName("blah", "_lah"), "blah_lah");
});

