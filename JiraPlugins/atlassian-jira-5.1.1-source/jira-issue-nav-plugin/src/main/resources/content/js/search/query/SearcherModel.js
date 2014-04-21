AJS.namespace("JIRA.Issues.SearcherModel");

JIRA.Issues.SearcherModel = JIRA.Issues.BaseModel.extend({

    /**
     * id: searcher id
     * name: The name of the clause
     * groupId: group id
     * groupName: group name
     * displayValue: the value representation of the query to display to the user. May be plain text or html, depending on the view representing this clause
     * jql: jql representation of the clause
     * validSearcher: is entire searcher valid for current search context
     * editHtml: html to display in select picker
     */
    properties: ["id", "name", "groupId", "groupName", "displayValue", "jql", "validSearcher", "editHtml"],

    /**
     * readyForDisplay: edit html has been retrieved and is ready to be displayed
     */
    namedEvents: ["readyForDisplay"],

    initialize: function() {
        _.bindAll(this);
        this.bind("change:editHtml", this._onEditHtmlUpdated);
    },

    hasEditHtml: function() {
        return !!this.getEditHtml();
    },

    getQueryString: function() {
        // custom handling for text query
        if (this.collection.QUERY_ID === this.getId()) {
            if (this.getDisplayValue()) {
                var params = {};
                params[this.collection.QUERY_PARAM] = this.getDisplayValue();
                return AJS.$.param(params);
            }
            return null;
        }

        // return jql for invalid searchers as the server doesn't return editHtml if a searcher is invalid, but it does return jql
        if (!this.getValidSearcher() && this.getJql()) {
            var params = {};
            params[this.collection.JQL_INVALID_QUERY_PREFIX + this.getId()] = this.getJql();
            return AJS.$.param(params);
        }
        // TODO: should cache this and update when editHtml updated
        var $editHtml = this._getEditHtmlElement();
        if (!$editHtml) {
            return null;
        }
        // Need to add to a form to serialize. Check if it's already in a form
        var parentForm = $editHtml.parents("form");
        if (!parentForm.length) {
            parentForm = AJS.$("<form></form>").append($editHtml);
        }
        return parentForm.serialize();
    },

    hasClause: function() {
        if (this.collection.QUERY_ID === this.getId()) {
            return !!this.getDisplayValue();
        }
        else if (!this.getValidSearcher()) {
            return !!this.getJql();
        }
        else {
            return !!this.getQueryString();
        }
    },

    /**
     * Resets search state.
     * If invalidSearcher == true, remove it entirely
     */
    clearSearchState: function() {
        if (this.getValidSearcher()) {
            this.set({
                displayValue: null,
                jql: null,
                validSearcher: null,
                editHtml: null
            });
        }
        else {
            this.collection.remove(this);
        }
    },

    /**
     * Ensures edit html exists. Triggers readyForDisplay when editHtml has been retrieved, which may be asynchronous if the value has not been retrieved,
     * or immediate if we already have editHtml
     */
    retrieveEditHtml: function() {
        if (null == this.getEditHtml()) {
            this._fetchEditHtml();
        }
        else {
            this.triggerReadyForDisplay(this._getEditHtmlElement());
        }
    },

    /**
     * Returns true if this element has edit html and it contains an error class
     */
    hasErrorInEditHtml: function() {
        var $el = this._getEditHtmlElement();
        if (!$el) {
            return false;
        }
        return $el.find(".error").length > 0;
    },

    hasError: function() {
        return !this.getValidSearcher() || this.hasErrorInEditHtml();
    },

    _fetchEditHtml: function() {
        var jql = this.collection.createJql();

        // TODO: Abort a pending request if there is one.
        // Avoid race condition -- ensure last request received is also last request issued.

        JIRA.SmartAjax.makeRequest({
            type: "get",
            url: contextPath +"/secure/SearchRendererEdit!Default.jspa",
            success: this._onFetchEditHtml,
            dataType: "html",
            //error: this._handleSaveError,
            data: {
                fieldId: this.getId(),
                decorator: "none",
                jqlContext: jql
            }
        });
    },

    _getEditHtmlElement: function() {
        if (!this.hasEditHtml()) {
            return null;
        }
        if (!this._editHtmlElement) {
            this._editHtmlElement = AJS.$(this.getEditHtml());
        }
        return this._editHtmlElement;
    },

    _onEditHtmlUpdated: function() {
        if (this._editHtmlElement) {
            this._editHtmlElement = null;
        }
    },

    _onFetchEditHtml: function(response) {
        this.setEditHtml(response);
        this.triggerReadyForDisplay(this._getEditHtmlElement());
    }
});
