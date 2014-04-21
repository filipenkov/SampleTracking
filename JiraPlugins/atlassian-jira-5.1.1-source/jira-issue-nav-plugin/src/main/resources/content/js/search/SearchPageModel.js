AJS.namespace("JIRA.Issues.SearchPageModel");

JIRA.Issues.SearchPageModel = JIRA.Issues.BaseModel.extend({

    KEYWORD_SEARCH: "keyword",
    ADVANCED_SEARCH: "advanced",

    DETAILED_MODE: "detailed", // split view

    properties: ["issueCollection", "jql", "selectedIssue", "awaitingResults", "totalResultCount",
        "segmentSize", "searchMode", "displayMode", "realDisplayMode", "realSelectedIssue"],

    namedEvents: [
        // KA2 search control
        "searchRequested",
        "searchError",
        "searchSuccess",
        "ignoreNextHistory", // TODO fix this dirty hack, should be a normal boolean argument to the router control
        "issueNotFound",     // after search is successful, if there is a selected issue but it's not here

        // KA2 split view
        "searchModeChanged",
        "switchedToDetailMode",
        "selectedInDetailedMode",
        "detailedModeDismissed",
        "backToSearch",

        // KA2 issue selection
        "issueSelected"
    ],

    defaults: {
        segmentSize: 75,
        searchMode: "keyword"
    },

    initialize: function(options, overrides) {
        _.bindAll(this);
        _.extend(this, overrides);
        this.setIssueCollection(options.issueCollection);
        this.getIssueCollection().isSelected = this.isSelected;
        this.bind("change", this.update);
        this.filterListModel = new JIRA.Issues.FilterListModel({}, {searchPageModel: this});
    },

    update: function () {

        var lastUpdate = this.lastUpdate || {};

        if (lastUpdate.jql !== this.getJql()) {
            if (typeof this.getJql() !== "string") {
                this.set({
                    "selectedIssue":undefined,
                    "totalResultCount":undefined
                });
                this.getIssueCollection().reset();
            } else {
                this._search(this.getJql());
            }
        }

        if (lastUpdate.searchMode !== this.getSearchMode()) {
            this.switchToSearchMode(this.getSearchMode());
            JIRA.Issues.Analytics.trigger("kickass.switchto" + this.getSearchMode());
        }

        if (lastUpdate.selectedIssue !== this.getSelectedIssue()) {
            this.selectIssueByKey(this.getSelectedIssue());
        }

        if (lastUpdate.realDisplayMode !== this.getRealDisplayMode()) {
            if (lastUpdate.realDisplayMode === this.DETAILED_MODE) {
                this.triggerDetailedModeDismissed();
            }
        }

        this.lastUpdate = this.toJSON();
    },

    /**
     * Updates attributes that the router knows about
     * @param attrs attributes from url
     */
    updateFromRouter: function(attrs) {
        var instance = this;
        if(this.getRealDisplayMode() && this.getRealDisplayMode() != attrs.displayMode) {
            this.set({
                "displayMode": attrs.displayMode,
                "realDisplayMode" : attrs.displayMode
            });
        }
        if (attrs.currentFilter) {
            var filter = new JIRA.Issues.FilterModel({id:attrs.currentFilter});
            filter.fetch({
                success: function(data) {
                    instance.searchWithFilter(data, attrs);
                },
                error: this.reset
            });
        } else {
            delete attrs.currentFilter;
            this.set(attrs);
            if (attrs.jql === undefined) {
                this.getIssueCollection().reset();
            }
        }
    },

    // KA2 issue selection

    /**
     * Returns the currently selected issue model, or null if none is currently selected
     */
    getSelectedIssueModel: function() {
        var selectedKey = this.getSelectedIssue();
        return selectedKey ? this._getIssueByKey(selectedKey) : null;
    },

    /**
     * Sets selection to the next issue
     *
     * @return {JIRA.Issues.IssueRowModel} issue model
     */
    next: function () {
        var issue,
            collection = this.getIssueCollection(),
            index = collection.indexOf(this.getRealSelectedIssue()) + 1;

        if (index < collection.length) {
            issue = this._selectIssueAt(index);
        }
        return issue;
    },

    /**
     * Sets selection to the previous issue
     *
     * @return {JIRA.Issues.IssueRowModel} issue model
     */
    prev: function () {

        var issue,
            collection = this.getIssueCollection(),
            index = collection.indexOf(this.getRealSelectedIssue()) - 1;

        if (index >= 0) {
            issue = this._selectIssueAt(index);
        }
        return issue;
    },

    /**
     * Used to highlight the currently selected issue in search results.
     *
     * @public
     * @param {number} id The id of the issue to select.
     */
    selectIssue: function(id, options) {

        var instance = this,
                selectedIssueModel = this.getIssueCollection().get(id);

        options = options || {};

        if (this.selectIssueLocked) {
            return;
        }

        this.selectIssueLocked = true;

        if (selectedIssueModel && selectedIssueModel !== this.getRealSelectedIssue()) {

            this.setSelectedIssue(selectedIssueModel.getEntity().key);
            selectedIssueModel.triggerSelected();

            // We trigger a select because this selectIssue originates from the UI but only an individual issue model
            // is actually changed. So the router needs to subscribe to this model as a bridge from the inner model
            // change to the necessary consequence in router navigation.
            this.triggerIssueSelected(selectedIssueModel, this.getDisplayMode());

            if (this.getRealSelectedIssue()) {
                this.getRealSelectedIssue().triggerSelected();
            }

            this.setRealSelectedIssue(selectedIssueModel);

            if (this.getRealDisplayMode() === this.DETAILED_MODE) {
                this.triggerSelectedInDetailedMode(selectedIssueModel, options.data);
            }
        }

        if (options.mode === this.DETAILED_MODE && this.getRealDisplayMode() !== this.DETAILED_MODE) {
            this.setDisplayMode(instance.DETAILED_MODE);
            this.setRealDisplayMode(instance.DETAILED_MODE);
            this.triggerSwitchedToDetailMode(selectedIssueModel);
            this.triggerSelectedInDetailedMode(selectedIssueModel, options.data);
        }

        this.selectIssueLocked = false;
    },

    /**
     * Request that an issue be selected. This issue may not (yet or ever) exist in the issue collection.
     *
     * @public
     * @param {string} issueKey The key of the issue to select
     */
    selectIssueByKey: function(issueKey) {

        // need to lock selecting issue as setSelectedIssue can cause recursion if it is already the selected issue
        if (!this.selectIssueByKeyLocked) {

            this.selectIssueByKeyLocked = true;
            var issue = this._getIssueByKey(issueKey);

            // We need to set selected issue even if it is not in the collection yet. The router needs to set this so that
            // when we actually get a collection with result we can select it.
            this.setSelectedIssue(issueKey);

            if (issue) {
                this.selectIssue(issue.id);
            }

            this.selectIssueByKeyLocked = false;
        }
    },

    /**
     * Determines if the passed in IssueModel should be selected.
     *
     * @public
     * @param {JIRA.Issues.IssueRowModel} issue
     * @return {boolean} true if the issue is selected.
     */
    isSelected: function(issue) {
        return this.getSelectedIssue() && this.getSelectedIssue() === issue.getEntity().key;
    },

    _selectIssueAt:function (index) {
        if (this.getRealDisplayMode() !== this.DETAILED_MODE) {
            this.triggerIgnoreNextHistory(); // TODO remove yucky events hack
        }
        var issue = this.getIssueCollection().at(index);
        this.selectIssue(issue.id);
        return issue;
    },

    // KA2 issue table
    /**
     * Used for inifinity scroll and appends more issue results to the end of the backing
     * issue collection for this model.
     *
     * @public
     */
    appendMoreResults: function() {
        if (!this.getAwaitingResults() && this._hasMoreResults()) {
            this.setAwaitingResults(true);
            var startOffset = this.getIssueCollection().size();
            var jqXHR = this.getIssueCollection().search(this.getJql(), this.getSegmentSize(), startOffset, this._searchSuccess, this._searchError);
            this._logAnalyticsSearchEvent(jqXHR, startOffset);
            JIRA.Issues.Analytics.trigger("kickass.scroll", {startOffset: startOffset});
        }
    },

    /**
     *
     * @private
     * @return {boolean} true if there's more search results on the server than we've retrieved so far on the client. False otherwise
     */
    _hasMoreResults: function() {
        return this.getIssueCollection().size() < this.getTotalResultCount();
    },

    /**
     * Updates the issue entity for the given id
     *
     * @param issueId
     * @param data
     */
    updateEntity: function (issueId, data) {
        var issue = this.getIssueCollection().get(issueId);
        if (issue) {
            issue.setEntity(data);
        }
    },

    // KA2 split control
    backToSearch: function () {
        this.set({
            "displayMode": undefined,
            "realDisplayMode": undefined
        });
        this.triggerBackToSearch();
        JIRA.Issues.Analytics.trigger("kickass.returntosearch");
    },

    /**
     * Switches to detailed view
     * @public
     * @param {number} issueId The id of the issue to select. (optional)
     */
    switchToDetailedView: function (issueId, lastEditData) {

        var issueToSelect = issueId;
        if(!issueId) {
            issueToSelect = this.getRealSelectedIssue().id;
        }
        if(issueToSelect) {
            this.selectIssue(issueToSelect, {
                mode: this.DETAILED_MODE,
                data: lastEditData
            });
        }
    },

    /**
     * Sets search mode
     * @param searchMode search mode (keyword or advanced)
     */
    switchToSearchMode: function(searchMode) {
        this.setSearchMode(searchMode);
        this.triggerSearchModeChanged(searchMode);
    },

    // KA2 search control
    /**
     * Updates the currently active filter to the passed in search model and sets the
     * jql to the jql of the model.  This will then trigger a search.
     *
     * @public
     * @param filter {JIRA.Issues.FilterModel} the new filter to use
     * @param attrs {object} current state that may get passed along when restoring from a URL.
     */
    searchWithFilter: function(filter, attrs) {
        this.reset();
        this.set(_.extend(attrs || {}, {
            searchMode: this.ADVANCED_SEARCH,
            jql:filter.getJql()
        }));
        this.filterListModel.setCurrentFilter(filter);
        JIRA.Issues.Analytics.trigger("kickass.searchWithFilter");
    },

    /**
     * Resets the current active filter to undefined (in case there was one) and updates
     * the jql to the passed querystring (which will trigger a search).
     *
     * @public
     * @param {string} jql the jql query string
     */
    searchWithJql: function(jql) {
        if(this.filterListModel.isNewSearch(jql)) {
            this.reset(); // clear out any invalid state. We are doing a new search...
            this.setJql(jql);
            this.triggerSearchRequested(jql);
        }
    },

    /**
     * Clear the jql and/or filter in this search result.
     *
     * @public
     */
    reset: function() {
        this.set({
            jql:undefined,
            selectedIssue: undefined
        }, {
            silent: true
        });
        this.filterListModel.setCurrentFilter(undefined);
        delete this.lastUpdate;
    },

    /**
     * Runs the actual search on the backing issue collection for this model.
     *
     * @private
     * @param {string} jql The jql to run on the server.
     */
    _search: function(jql) {
        this.set({
            "awaitingResults": true,
            "realSelectedIssue": undefined // JRADEV-9287 - Don't remember last selected. We are doing a new search
        });
        var jqXHR = this.getIssueCollection().search(jql, this.getSegmentSize(), 0, this._searchSuccess, this._searchError);
        AJS.log("SearchPageModel: New Search [" + jql + "]");
        this._logAnalyticsSearchEvent(jqXHR, 0);
    },

    /**
     * Retrieve an issue by key.
     *
     * @private
     * @param {string} key the key for the issue to retrieve.
     */
    _getIssueByKey: function(key) {
        return this.getIssueCollection().find(function(i) {
                    return i.getEntity().key === key}
        );
    },

    /**
     * Callback that's called on a successful response from the server when running a search.
     *
     * @private
     * @param {JIRA.Issues.IssueCollection} issueCollection The backbone collection backing this model
     * @param {Object} response The server response.
     */
    _searchSuccess: function(issueCollection, response) {

        var issue = (this.getSelectedIssue())
            ? this._getIssueByKey(this.getSelectedIssue())
            : this.getIssueCollection().first();

        this.set({
            "awaitingResults":false,
            "totalResultCount":response.total
        });
        this.triggerSearchSuccess(response);

        if (issue) {
            this.triggerIgnoreNextHistory();
            this.selectIssueByKey(issue.getEntity().key);
            // We only do this if we can find an issue
            if (this.getDisplayMode() === this.DETAILED_MODE) {
                this.switchToDetailedView();
            }
        } else if (this.getSelectedIssue()) {
            // we may be in detailed mode so flick back to a view we can see. But don't add this transition to history
            this.triggerIgnoreNextHistory();
            this.backToSearch();
            // If we have a selected issue but can't find it notify the user
            this.triggerIssueNotFound(this.getSelectedIssue());
        }

    },

    /**
     * Callback to handle an error when runnning a search on the server.
     *
     * @private
     * @param {JIRA.Issues.IssueCollection} issueCollection The backbone collection backing this model
     * @param {XMLHttpRequest} response The server response.
     */
    _searchError: function(issueCollection, response) {
        this.setAwaitingResults(false);
        try {
            this.triggerSearchError(JSON.parse(response.responseText));
        } catch (Error) {
            this.triggerSearchError({errorMessages:[AJS.I18n.getText("issue.nav.common.server.error")]});
        }
    },

    /**
     * Note:
     *   In order to capture request duration, this method *must* be called
     *   at the time the ajax request is issued, not once it has completed.
     *
     * @private
     * @param {jQuery.Deferred} jqXHR -- object returned from jQuery.ajax()
     * @param {number} startOffset -- index within search results
     */
    _logAnalyticsSearchEvent: function(jqXHR, startOffset) {
        var requestStart = new Date().getTime();
        var properties = {
            searchMode: this.getSearchMode(),
            isSavedSearch: this.filterListModel.getCurrentFilter() !== undefined,
            totalResults: 0,
            maxResultsPerPage: this.getSegmentSize(),
            startOffset: startOffset,
            success: false
        };
        jqXHR.complete(function() {
            // The success/error callbacks should fire synchronously
            // since this request has been completed.
            jqXHR.success(function(response) {
                properties.totalResults = response.total;
                properties.success = true;
            });
            properties.status = jqXHR.status;
            properties.statusText = jqXHR.statusText;
            properties.requestDuration = new Date().getTime() - requestStart;
            JIRA.Issues.Analytics.trigger("kickass.search", properties);
        });
    }
});
