AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:issuenav");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:sinon");

module("JIRA.Issues.TextQueryBuilder");

test("Empty", function() {
    equals(JIRA.Issues.TextQueryBuilder.buildJql(), "");
});

test("Single term", function() {
    equals(JIRA.Issues.TextQueryBuilder.buildJql("hi"), "text ~ \"hi\"");
});

test("Multiple terms", function() {
    equals(JIRA.Issues.TextQueryBuilder.buildJql("hi everybody"), "text ~ \"hi\" AND text ~ \"everybody\"");
});

test("Reserved word", function() {
    equals(JIRA.Issues.TextQueryBuilder.buildJql("and"), "text ~ \"and\"");
});

test("Whitespace in query", function() {
    equals(JIRA.Issues.TextQueryBuilder.buildJql(" hi"), "text ~ \"hi\"");
    equals(JIRA.Issues.TextQueryBuilder.buildJql("hi "), "text ~ \"hi\"");
    equals(JIRA.Issues.TextQueryBuilder.buildJql("hi   everybody"), "text ~ \"hi\" AND text ~ \"everybody\"");
    equals(JIRA.Issues.TextQueryBuilder.buildJql("  hi   dr   nic  "), "text ~ \"hi\" AND text ~ \"dr\" AND text ~ \"nic\"");
});

