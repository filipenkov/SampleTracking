AJS.namespace("JIRA.Issues.Mixin.PageTitleView");

JIRA.Issues.Mixin.PageTitleView = {
    _pageTitle : document.title,
    updatePageTitle : function(value) {
        document.title =  value;
    },
    prependToPageTitle : function(item, withSpace) {
        var appendChar = withSpace ? " " : "";
        this.updatePageTitle(item + appendChar + document.title );
    },
    appendToPageTitle : function(item, withSpace) {
        var appendChar = withSpace ? " " : "";
        this.updatePageTitle(document.title + appendChar + item);
    },
    restorePreviousPageTitle : function() {
        document.title = this._pageTitle;
    }
};