AJS.namespace("JIRA.Issues.TestUtils");

JIRA.Issues.TestUtils = {
    /**
     * Creates an IssueCollection whose server call has been mocked out.
     */
    createIssueCollection: function(results) {
        var issueCollection = new JIRA.Issues.IssueCollection(results);
        issueCollection.search = sinon.spy();
        return issueCollection;
    },
    /**
     * Creates a search results model with an issue issue collection whose server call has been stubbed out
     */
    createSearchPageModel: function(extendProps, results) {
        extendProps = _.extend({
            _logAnalyticsSearchEvent: sinon.stub()
        }, extendProps);
        return new JIRA.Issues.SearchPageModel({
            issueCollection: this.createIssueCollection(results)
        }, extendProps);
    },
    /**
     * Creates an issue nav router with the navigate method (that actually sets the url) mocked out.
     *
     * Note that in order to use this navigate method you will want to use sinon timers as the issue nav router
     * currently debounces multiple navigate calls using setTimeout
     *
     * Also initialises backbone history and adds a "fakeLoad" method to the issueNavRouter that triggers the actual
     * routing. Eg fakeLoad("/some/url/fragment") will actually trigger the configured route for that url fragment.
     *
     * @param options options to pass to IssueNavRouter constructor
     */
    createIssueNavRouter: function(options) {
        var fakeContextRoot = "fakeContextRoot";

        var issueNavRouter = new JIRA.Issues.IssueNavRouter(options);

        // Don't call Backbone.history.start, as it puts in listeners and we can't call it twice
        // Set root directly, as it's required for "loadUrl" calls below
        // We're using undocumented backbone API here and it could break in future versions,
        // but it's worth it to test our routes
        Backbone.history.options = {
            root: fakeContextRoot
        };
        issueNavRouter.fakeLoad = function(urlFragment) {
            Backbone.history.loadUrl(fakeContextRoot + urlFragment);
        };

        // Mock out navigation as it sets the URL
        issueNavRouter.navigate = sinon.spy();

        return issueNavRouter;
    },
    //quick function to test if two strings contain the same html.
    //could probably be improved by using a dom fragment, but this method *should* takes implementation details away from the browser
    //currently only tests single elements.
    //This is mainly because i did not trust .html();
    areHtmlStringsEqual: function(initial, compare) {
        var $initial = AJS.$(initial),
            $compare = AJS.$(compare),
            compareAttr = $compare[0].attributes,
            initialAttr = $initial[0].attributes;

        if($compare[0].tagName != $initial[0].tagName) {
            return false;
        }

        if(AJS.$.trim($compare.text()) != AJS.$.trim($initial.text())) {
            return false;
        }
        var attributeKVP = function(attr,key) {
            return {name : attr.nodeName , value: attr.nodeValue }
        };

        return _.isEqual(_.map(compareAttr,attributeKVP), _.map(initialAttr,attributeKVP));
    }
};
