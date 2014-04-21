AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:viewissue");

test("Title Attribute rendering", function() {
    var html = JIRA.Templates.ViewIssue.Header.titleAttr({
        title: "This is a title",
        label: ""
    });

    equal(html, "title=\"This is a title\"", "Given a title and no label, the title should be in the attribute");

    html = JIRA.Templates.ViewIssue.Header.titleAttr({
        title: "This is a title",
        label: "Fred Flintstone"
    });
    equal(html, "title=\"This is a title\"", "Given a title and label, the title should be in the attribute");

    html = JIRA.Templates.ViewIssue.Header.titleAttr({
        title: "",
        label: "Short Label"
    });
    equal(html, "", "Given no title and a short label, the title should be empty");

    html = JIRA.Templates.ViewIssue.Header.titleAttr({
        title: "",
        label: "This is a very long label with more than 25 chars"
    });
    equal(html, "title=\"This is a very long label with more than 25 chars\"", "Given no title and a long label, the title should be the label");
});
