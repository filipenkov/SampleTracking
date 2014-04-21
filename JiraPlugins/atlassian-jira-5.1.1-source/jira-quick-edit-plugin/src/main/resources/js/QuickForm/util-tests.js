AJS.test.require("com.atlassian.jira.jira-quick-edit-plugin:quick-create-issue");
AJS.test.require("com.atlassian.jira.jira-quick-edit-plugin:test-util");

test("Test scripts are not run when extracting from html", function () {

    expect(2);
    var html = "<div><span>something</span><script>ok(false, 'script should not be run');</script></div>";
    var result = JIRA.extractScripts(html);
    equal(result.html.length, 1);
    equal(result.scripts.length, 1);

});