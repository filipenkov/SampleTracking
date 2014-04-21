AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:viewissue");

module("IssueFieldUtil");

test("transformFieldHtml() handles editHtml containing scripts that produce errors", function() {
    var resp = {
        fields: [{
            id: 'testField',
            label: 'Test Field',
            editHtml: '<div class="description">Test</div><script>null()</script>',
            required: true
        }]
    };
    try {
        resp = JIRA.Issues.IssueFieldUtil.transformFieldHtml(resp);
        ok(true, "No exception thrown");
    } catch (e) {
        ok(false, "Transform didn't catch exception");
    }
});