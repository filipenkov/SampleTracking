JIRA.VersionModel = AJS.RestfulTable.EntryModel.extend({

    // rest resources
    RELATED_ISSUES_PATH: "/relatedIssueCounts",
    UNRESOLVED_ISSUES_PATH: "/unresolvedIssueCount",


    addExpand: function (changed) {
        changed.expand = "operations";
    },

    /**
     * Destroys the model on the server. We need to override the default method as it does not support sending of
     * query paramaters.
     *
     * @override
     * @param options
     * ... {function} success - Server success callback
     * ... {function} error - Server error callback
     * ... {object} data
     * ... ... {String} moveFixIssuesTo - The version to set fixVersion to on issues where the deleted version is the
     * fix version, If null then the fixVersion is removed.
     * ... ... {String}  moveAffectedIssuesTo The version to set affectedVersion to on issues where the deleted version
     * is the affected version, If null then the affectedVersion is removed.
     *
     * @return JIRA.VersionModel
     */
    destroy: function (options) {

        var instance = this,
            url = this.url(),
            data = jQuery.param(options.data);

        if (data !== "") {

            // we need to add to the url as the data param does not work for jQuery DELETE requests
            url = url + "?" + data;
        }

        JIRA.SmartAjax.makeRequest({
            url: url,
            type: "DELETE",
            dataType: "json",
            complete: function (xhr, status, smartAjaxResponse) {

                var smartAjaxResponseData = smartAjaxResponse.data;

                if (typeof smartAjaxResponse.data === "string") {
                    smartAjaxResponseData = JSON.parse(smartAjaxResponse.data);
                }

                var isValidationError = !(xhr.status === 400 && smartAjaxResponseData && smartAjaxResponseData.errors);

                if (smartAjaxResponse.successful) {
                    instance.collection.remove(instance);
                    if (options.success) {
                        options.success.call(instance, smartAjaxResponseData);
                    }
                } else if(isValidationError) {
                    instance._serverErrorHandler(smartAjaxResponse);
                    if (options.error) {
                        options.error.call(instance, smartAjaxResponseData);
                    }
                }
                if (options.complete) {
                    options.complete.call(instance, xhr.status, smartAjaxResponseData);
                }
            }
        });

        return this;
    },

    /**
     * Gets count for issues with either affects version or fix version containing this version
     *
     * @param options
     * ... {function} success - Server success callback
     * ... {function} error - Server error callback
     * @return JIRA.VersionModel
     */
    getRelatedIssueCount: function (options) {

        var instance = this;

        options = options || {};

        JIRA.SmartAjax.makeRequest({
            url: this.url() + this.RELATED_ISSUES_PATH,
            complete: function (xhr, status, smartAjaxResponse) {
                if (smartAjaxResponse.successful) {
                    options.success.call(instance, smartAjaxResponse.data);
                } else {
                    instance._serverErrorHandler(smartAjaxResponse)
                }
            }
        });

        return this;
    },

    /**
     * Gets JSON representation of available versions to migrate issues of this version into.
     *
     * @return {Array}
     */
    getSwapVersionsJSON: function () {

        var instance = this,
            availableSwapVersions = [];


        this.collection.sort().each(function (model) {
            if (!model.get("archived") && model !== instance) {
                availableSwapVersions.push(model.toJSON());
            }
        });

        return availableSwapVersions.reverse();
    },

    /**
     * Gets JSON representation of available versions to migrate issues of this version into.
     *
     * @return {Array}
     */
    getMoveVersionsJSON: function () {

        var instance = this,
            availableMoveVersions = [];

        this.collection.sort().each(function (model, i) {

            var json = model.toJSON();

            if (instance.collection.at(i+1) === instance) {
                json.nextScheduled = true;
            }

            if (!model.get("released") && model !== instance) {
                availableMoveVersions.push(json);
            }
        });

        return availableMoveVersions;
    },


    /**
     * Gets count for unresolved issues in this version
     *
     * @param options
     * ... {function} success - Server success callback
     * ... {function} error - Server error callback
     * @return JIRA.VersionModel
     */
    getUnresolvedIssueCount: function (options) {

        var instance = this;

        options = options || {};

        JIRA.SmartAjax.makeRequest({
            url: this.url() + this.UNRESOLVED_ISSUES_PATH,
            complete: function (xhr, status, smartAjaxResponse) {
                if (smartAjaxResponse.successful) {
                    options.success.call(instance, smartAjaxResponse.data);
                } else {
                    instance._serverErrorHandler(smartAjaxResponse)
                }
            }
        });

        return this;
    }

});
