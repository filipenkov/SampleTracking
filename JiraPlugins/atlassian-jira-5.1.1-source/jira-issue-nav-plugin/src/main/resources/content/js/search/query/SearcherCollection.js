AJS.namespace("JIRA.Issues.SearcherCollection");

JIRA.Issues.SearcherCollection = JIRA.Issues.BaseCollection.extend({

    model: JIRA.Issues.SearcherModel,

    /**
     * collectionChanged: the collection has changed. This is fired at most once per public interface method. Clients
     * requesting changes should listen to this method to receive updates instead of add, change, remove if they
     * want only a single notification of change.
     */
    namedEvents: ["collectionChanged", "restoringFromQuery", "jqlTooComplex"],

    QUERY_PARAM: "q",
    QUERY_ID: "query",

    /**
     * Prefix used to send jql for invalid searchers to the server, because we don't generate editHtml for invalid searchers
     * (but we can generate jql)
     */
    JQL_INVALID_QUERY_PREFIX: "__jql_",

    initialize: function(models, options) {
        // TODO: this.fixedLozenges should be a fixedlozengecollection not an array
        _.bindAll(this);
        this.fixedLozenges = options && options.fixedLozenges ? options.fixedLozenges : [];
    },

    /**
     * Returns the subset of searchers in their groups that should be available to the user for selection.
     *
     * Examples of excluded searchers: those in the fixed clause list, the "text" type that searches multiple text
     * fields, any searcher that is already represented in the clause collection.
     *
     * @return A JSON representation of grouped searchers.
     */
    getSearcherGroupsForAddMenu: function() {
        var clauses = this.filter(function(searcherModel) {
            return searcherModel.hasClause();
        });
        var clauseIds = _.pluck(clauses, "id");

        var excludeSearchers = _.union(clauseIds, ['query'], this.fixedLozengeIds());
        var searchers = this.reject(function(searcher) {
            return _.contains(excludeSearchers, searcher.getId());
        });

        var groups = [];
        var groupIds = {};
        _.each(searchers, function(searcher) {
            if (!groupIds[searcher.getGroupId()]) {
                groupIds[searcher.getGroupId()] = true;
                groups.push({
                    id: searcher.getGroupId(),
                    name: searcher.getGroupName(),
                    searchers: [searcher.toJSON()]
                });
            } else {
                var group = _.find(groups, function(group) {
                    return group.id === searcher.getGroupId();
                });
                group.searchers.push(searcher.toJSON());
            }
        });

        return groups;
    },

    /**
     * Sets the jql for the model with the given id, creating one if it doesn't exist
     * @param id id of model
     * @param jql jql to set
     */
    setJql: function(id, jql) {
        this._addOrSet(id, {
            jql: jql
        });
    },

    /**
     * Returns a single jql string expressing all subclauses in this collection.
     *
     * If no text query is present, an empty one is created.
     */
    createJql: function() {
        if (!this.get(this.QUERY_ID)) {
            this.setTextQuery("");
        }
        var arr = this.pluck("jql");
        if (!arr.length) {
            return null;
        }
        return _.filter(arr, _.isNotBlank).join(" AND ");
    },

    /**
     * Has the user specified any clauses?
     */
    isDirty: function() {
        return this.any(function(lozenge) {
            // clauses are ultimately defined by a jql clause
            return lozenge.getJql() !== undefined && lozenge.getJql() !== ""; // TODO the stupid text field strikes again. it has a "" state after routing
        });
    },

    /**
     * Clears entire search state
     */
    clearSearchState: function() {
        this.each(function(searcherModel) {
            searcherModel.clearSearchState();
        });
        // TODO: optimise by only requerying server if search state changes
        this._querySearchersAndValues();
    },

    /**
     * Clears the search state for a single clause
     * @param id id of searcher to clear
     */
    clearClause: function(id) {
        // We don't need to requery here if invalid clauses
        var clause = this.get(id);
        if (clause) {
            clause.clearSearchState();
        }
        this.triggerCollectionChanged();
    },

    getTextQuery: function() {
        var model = this.get(this.QUERY_ID);
        return model ? model.getDisplayValue() : "";
    },

    setTextQuery: function(textQuery, opts) {
        if (textQuery === null || textQuery === undefined) {
            this.clearTextQuery(opts);
            return;
        }

        var jql = JIRA.Issues.TextQueryBuilder.buildJql(textQuery);

        this._addOrSet(this.QUERY_ID, {
            displayValue: textQuery,
            jql: jql
        }, opts);
    },

    clearTextQuery: function(opts) {
        this.remove(this.QUERY_ID, opts);
    },

    /**
     * Creates a queryString representing all querystring members
     * @return {string}
     */
    getQueryString: function() {
        var queryStrings = [];
        this.each(function(searcherModel) {
            var qs = searcherModel.getQueryString();
            if (qs) {
                queryStrings.push(qs);
            }
        });
        return queryStrings.join("&");
    },

    /**
     * Returns true if all the keys in the given object are client side aware
     * @param params
     */
    _hasOnlyKnownParams: function(params) {
        for (var key in params) {
            if (key !== this.QUERY_PARAM) {
                return false;
            }
        }
        return true;
    },

    /**
     * Adds or sets parameters. If a model with the given id is found, the values in params are set. Otherwise a model is created
     * with the given id and params.
     * @param id id to of model to find.
     * @param params parameters
     */
    _addOrSet: function(id, params, opts) {
        var model = this.get(id);
        if (model) {
            model.set(params, opts);
        }
        else {
            var paramsWithId = _.clone(params);
            paramsWithId.id = id;
            this.add(paramsWithId, opts);
            model = this.get(id);
        }
        return model;
    },

    /**
     * Silently resets and restores the simple search state based on the query string.
     *
     * @param {Object} params representation of queryString
     * @param {string} queryString
     *
     * @returns {jQuery.Deferred}
     */
    restoreFromQueryString: function(params, queryString) {

        this.triggerRestoringFromQuery(params);

        this.reset([], {
            silent: true
        });

        var deferred = AJS.$.Deferred();

        queryString += ""; // coerce value to string

        if ("?" === queryString.charAt(0)) {
            queryString = queryString.substring(1);
        }

        // TODO: as an optimisation, do not query if we have only known params; but we still need to query searchers so this of little value
//        if (this._hasOnlyKnownParams(params)) {
//            // If we have only known parameters, run the query
//            this.setTextQuery(params[this.QUERY_PARAM], {silent:true});
//            deferred.resolve();
//        }
//        else {
            // Else we need to go to the server to get the jql
            this.setTextQuery(params[this.QUERY_PARAM], {silent:true});

            this._querySearchersAndValues(queryString, deferred);
//        }

        return deferred.promise();
    },

    restoreFromJql: function(jql) {
        this.reset([], {
            silent: true
        });

        var response = AJS.$.ajax({
            url: contextPath + "/secure/Search!Jql.jspa?decorator=none",
            data: {
                jql: jql
            },
            type: "GET"
        });

        response.success(_.bind(function(data) {
            this._onQuerySearchersAndValues(data, true);
        }, this));

        response.error(_.bind(function(resp) {
            // TODO handle these errors in a central standardised place.
            if (resp.responseText.indexOf("jqlTooComplex") > -1) {
                this.triggerJqlTooComplex(jql);
            }
        }, this));
    },

    /**
     * Tells the clause to update from the values selected in its editHtml, creating or updating it as required.
     * This update involves an AJAX request to retrieve the jql and lozenge content from the server.
     * @param id
     */
    createOrUpdateClauseWithQueryString: function(id) {
        var deferred = AJS.$.Deferred();

        // Requery all searchers and values
        this._querySearchersAndValues(this.getQueryString(), deferred);

        return deferred.promise();
        // TODO: could optimise by only requesting all searchers and values when context changes (ie project or issue type)
        // and requesting only valuehtml for other cases. see _querySingleValue
    },

    _querySingleValue: function() {
        // Submitting retrieves clause details such as JQL and the new lozenge content
        var response = AJS.$.ajax({
            url: contextPath +"/secure/SearchRendererValue!Default.jspa?decorator=none&" + this.getQueryString(),
            type: "GET"
        });

        response.done(this._onValuesReturned);
    },

    /**
     * Returns a map by id of all searchers from the response
     * @param data response data
     */
    _parseSearcherGroups: function(data) {
        var searchers = {};

        _.each(data.groups, function(group) {
            _.each(group.searchers, function(searcher) {
                searcher.groupId = group.type;
                searcher.groupName = group.title;
                searchers[searcher.id] = searcher;
            });
        });

        return searchers;
    },

    _querySearchersAndValues: function(queryString, deferred) {
        var url = contextPath + "/secure/Search!Default.jspa?decorator=none";
        if (queryString) {
            url += "&" + queryString
        }
        var response = AJS.$.ajax({
            url: url,
            type: "GET"
        });
        response.done(_.bind(function(data) {
            this._onQuerySearchersAndValues(data, false);
        }, this));
        if (deferred) {
            response.done(deferred.resolve);
        }
        // TODO:
        //response.fail()
    },

    _onQuerySearchersAndValues: function(data, silent) {

        // remove searchers that do not have a clause
        this._removeSearchersWithoutClause();

        // merge searchers and values from response
        var searchers = this._parseSearcherGroups(data.searchers);
        _.each(data.values, _.bind(function(value, id) {
            // compose searcher and value from response
            var searcher = searchers[id];
            if (!searcher) {
                searchers[id] = value;
            }
            else {
                _.extend(searcher, value);
            }
        }, this));

        // push into collection
        _.each(searchers, _.bind(function(value, id) {
            if (id === this.QUERY_ID) {
                // ignore - never updated by context
            }
            else {
                this._addOrSet(id, {
                    groupId: value.groupId,
                    groupName: value.groupName,
                    name: value.name,
                    displayValue: value.viewHtml,
                    jql: value.jql,
                    editHtml: value.editHtml,
                    validSearcher: value.validSearcher
                });
            }
        }, this));

        if (!silent) {
            this.triggerCollectionChanged();
        }
    },

    /**
     * Iterates this collection, removing models that do not have a clause
     */
    _removeSearchersWithoutClause: function() {
        var i = 0;
        while (i < this.length) {
            var searcherModel = this.at(i);
            if (searcherModel.hasClause()) {
                ++i;
            }
            else {
                this.remove(searcherModel.getId(), {
                    silent: true
                });
            }
        }
    },

    _onValuesReturned: function(data) {
        this.each(_.bind(function(clauseModel) {
            //TODO: throb the main view
            var id = clauseModel.getId();
            var d = data[id];

            if (d) {
                clauseModel.set({
                    displayValue: d.viewHtml,
                    jql: d.jql,
                    validSearcher: d.validSearcher,
                    editHtml: d.editHtml
                });
            }
            else if (id === this.QUERY_ID) {
                // ignore
            }
            else {
                clauseModel.set({
                    displayValue: "",
                    jql: null,
                    validSearcher: true,
                    editHtml: ""
                });
            }
        }, this));

        this.triggerCollectionChanged();
    },

    /**
     * Return a list of variable clauses. "Variable" here means
     * - the searcher has a value
     * - the searcher is not query text or a fixed lozenge
     */
    getVariableClauses: function() {
        var variableClauses = [];
        this.each(_.bind(function(searcherModel) {
            if (!this._isFixed(searcherModel) && searcherModel.hasClause()) {
                variableClauses.push(searcherModel);
            }
        }, this));

        return variableClauses;
    },

    _isFixed: function(searcherModel) {
        return _.contains(this.fixedLozengeIds(), searcherModel.getId()) || searcherModel.getId() === this.QUERY_ID;
    },

    fixedLozengeIds: function() {
        return _.pluck(this.fixedLozenges, "id");
    }
});
