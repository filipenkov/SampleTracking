AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:common");
AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:testutils");

module("JIRA.Issues.TestUtils.areHtmlStringsEqual", {
});

test("Test tag name matching", function() {
    ok(JIRA.Issues.TestUtils.areHtmlStringsEqual("<div></div>","<div></div>"),"Empty elements with matching tag names returns true");
    ok(!JIRA.Issues.TestUtils.areHtmlStringsEqual("<div></div>","<span></span>"),"Empty elements with none matching tag names returns false");
});

test("Test attribute matching", function () {
    ok(JIRA.Issues.TestUtils.areHtmlStringsEqual("<div class='hello'></div>","<div class='hello'></div>"),"Same attributes on same tag name return true");
    ok(!JIRA.Issues.TestUtils.areHtmlStringsEqual("<div class='hello'></div>","<div class='hello1'></div>"),"Miss matched attributes on same tag name return false");
    ok(!JIRA.Issues.TestUtils.areHtmlStringsEqual("<div class='hello' id='test'></div>","<div id='test' class='hello' ></div>"),"Equal but out of order attributes return true");
});


test("Test text matching", function () {
    ok(JIRA.Issues.TestUtils.areHtmlStringsEqual("<div class='hello'>hi</div>","<div class='hello'>hi</div>"),"Same attributes same text return true");
    ok(!JIRA.Issues.TestUtils.areHtmlStringsEqual("<div class='hello'>h2i</div>","<div class='hello'>hi</div>"),"Same attributes different text return false");
});

