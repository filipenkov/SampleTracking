AJS.namespace("JIRA.Issues.IssueNavRouter");

JIRA.Issues.IssueNavRouter = JIRA.Issues.BaseRouter.extend({

    /**
     * routed: called after we have processed a route
     */
    namedEvents: ["routed"],

    initialize: function (options) {
        _.extend(this, options);
        _.bindAll(this);

        this.searchPageModel.bindSearchRequested(this.searchNav);
        this.searchPageModel.bindSearchModeChanged(this.searchNav);
        this.searchPageModel.bindSwitchedToDetailMode(this.searchNav);
        this.searchPageModel.bindIssueSelected(this.searchNav);
        this.searchPageModel.bindBackToSearch(this.searchNav);

        this.searchPageModel.bind("ignoreNextHistory", this.replaceNext);
    },

    routes: {
        "" : "search",
        "/" : "search",
        ":searchMode" : "search",
        ":searchMode/*textQuery" : "search",
        ":searchMode/*jql" : "search",
        ":searchMode/*filter" : "search"

    },

    search: function (searchMode, queryString) {
        if (this.ignoreRouting) {
            return;
        }
        this.ignoreRouting = true;
        var instance = this;
        var attrs = {};
        var params = JIRA.Issues.QueryStringParser.parse(queryString);

        // TODO: get user preference for keyword or advanced
        searchMode = searchMode || this.searchPageModel.KEYWORD_SEARCH;
        queryString = queryString || "";

        attrs.searchMode = searchMode;
        attrs.displayMode = params.displayMode;
        attrs.selectedIssue = params.issue;

        if (this.searchPageModel.getRealDisplayMode() !== this.searchPageModel.DETAILED_MODE) {
            // Update router with searching
            if (this.searchPageModel.KEYWORD_SEARCH === searchMode) {
                this.searcherCollection.restoreFromQueryString(params, queryString).done(_.bind(function () {
                    attrs.jql = this.searcherCollection.createJql() || "";
                    instance.handleSearchComplete(attrs);
                }, this));
            } else {
                this._handleAdvancedQueryString(attrs, params, queryString);
                this.handleSearchComplete(attrs);
            }
        } else {
            // Update router without searching
            attrs.jql = this.searchPageModel.getJql() || "";
            this.handleSearchComplete(attrs);
        }
    },

    handleSearchComplete: function (attrs) {
        this.searchPageModel.updateFromRouter(attrs);
        this.triggerRouted();
        this.ignoreRouting = false;
    },

    searchNav: function() {
        if (this.ignoreRouting) {
            return;
        }
        var searchMode = this.searchPageModel.getSearchMode();
        if (this.searchPageModel.KEYWORD_SEARCH === searchMode) {
            url = this._getKeywordUrl();
        }
        else {
            url = this._getAdvancedUrl();
        }
        this._doNavigation(url);
    },

    /**
     * Gets called via the ignoreNextHistory event triggered by the search results model.
     */
    replaceNext: function () {
        this.replace = true;
    },

    /**
     * @private
     * @param attrs
     * @param params
     * @param queryString
     */
    _handleAdvancedQueryString: function(attrs, params, queryString) {
        attrs.currentFilter = params.filter;
        if (params.jql) {
            attrs.jql = params.jql;
        }
        else {
            attrs.jql = "";
        }
        attrs.selectedIssue = params.issue;
    },

    /**
     * @private
     * @param {string} navString the url to navigate to.
     */
    _doNavigation: function(navString) {
        if (this.tm) {
            clearTimeout(this.tm);
            delete this.tm;
        }
        this.tm = window.setTimeout(_.bind(function () {
            AJS.log("IssueNavRouter: Navigating with" + (!!this.replace ? "out" : "") +  " history to '" + navString + "'");
            this.navigate(navString, {
                replace: !!this.replace
            });
            this.replace = false;
        }, this), 0);
    },

    /**
     * @private
     */
    _getAdvancedUrl: function() {
        var jql = this.searchPageModel.getJql(),
            currentFilter = this.searchPageModel.filterListModel.getCurrentFilter(),
            selectedIssueKey = this.searchPageModel.getSelectedIssue(),
            displayMode = this.searchPageModel.getDisplayMode();

        var url = this.searchPageModel.ADVANCED_SEARCH + "/";
        var queryComponents = [];

        if (currentFilter) {
            queryComponents.push("filter=" + currentFilter.getId());

        } else if (typeof jql === "string") {
            queryComponents.push("jql=" + jql);
        }

        if (selectedIssueKey) {
            queryComponents.push("issue=" + encodeURIComponent(selectedIssueKey));
        }

        if (displayMode) {
            queryComponents.push("displayMode=" + displayMode);
        }

        if (queryComponents.length > 0) {
            url += "?" + queryComponents.join("&");
        }

        return url;
    },

    /**
     * @private
     */
    _getKeywordUrl: function() {
        var paramString = this.searcherCollection.getQueryString();
        var selectedIssueKey = this.searchPageModel.getSelectedIssue();
        var displayMode = this.searchPageModel.getDisplayMode();

        var url = this.searchPageModel.KEYWORD_SEARCH + "/";
        var queryComponents = [];

        if (paramString) {
            queryComponents.push(paramString);
        }

        if (selectedIssueKey) {
            queryComponents.push("issue=" + encodeURIComponent(selectedIssueKey));
        }

        if (displayMode) {
            queryComponents.push("displayMode=" + displayMode);
        }

        if (queryComponents.length > 0) {
            url += "?" + queryComponents.join("&");
        }

        return url;
    }
});
