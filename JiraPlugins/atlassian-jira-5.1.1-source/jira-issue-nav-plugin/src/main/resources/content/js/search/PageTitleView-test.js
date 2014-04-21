AJS.test.require("com.atlassian.jira.jira-issue-nav-plugin:common");

module("JIRA.Issues.Mixin.PageTitleView", {
    setup: function() {
        var MyView = JIRA.Issues.BaseView.extend({
            mixins: [JIRA.Issues.Mixin.PageTitleView]
        });
        this.view = new MyView();
        this.currentPageTitle = document.title;
    },
    teardown: function() {
        document.title = this.currentPageTitle;
    }
});

test("Test setting page title", function(){
    var pageTitle = "Hello world";
    var append = "again";
    this.view.updatePageTitle(pageTitle);
    equals(pageTitle,document.title, "Document title should be updated");
    this.view.appendToPageTitle(append);
    equals(pageTitle + append,document.title, "Document title should be appended to");
    this.view.appendToPageTitle(append,true);
    equals(pageTitle + append + " "+ append,document.title, "Document title should be appended to with space");
    this.view.prependToPageTitle(append);
    equals(append + pageTitle + append + " "+ append,document.title, "Document title should be appended");
    this.view.prependToPageTitle(append,true);
    equals(append + " " + append + pageTitle + append + " "+ append,document.title, "Document title should be appended");
});

test("Test restore page title", function() {
    var pageTitle = "Hello world";
    this.view.updatePageTitle(pageTitle);
    this.view.restorePreviousPageTitle();
    equals(this.currentPageTitle , document.title, "Document title should restore");
});






