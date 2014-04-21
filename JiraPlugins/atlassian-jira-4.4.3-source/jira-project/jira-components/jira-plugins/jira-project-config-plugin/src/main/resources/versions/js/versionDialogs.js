jQuery.namespace("JIRA.Admin.Version");

/**
 * Renders release form used in dialog
 */
JIRA.Admin.Version.ReleaseForm = Backbone.View.extend({
    /**
     * Releases a version on the server.
     *
     * @param {Object} values
     * @param complete
     * @return {JIRA.Admin.Version.ReleaseForm}
     */
    submit: function (values, row, complete) {

        this.$(".throbber").addClass("loading");

        var instance = this;

        var updatedValues = {userReleaseDate:values["userReleaseDate"], released:true, expand:"operations"};

        if(values["unresolved"] === "move"){
            updatedValues["moveUnfixedIssuesTo"] = values["moveUnfixedIssuesTo"];
        }

        this.model.save(updatedValues, {
            success:function () {
                row.render();
                row._showUpdated();
                complete();
            },
            error: function(model, xhr, smartAjaxResponse) {
                if (smartAjaxResponse.validationError) {

                    var options = {
                        errors: smartAjaxResponse.data.errors,
                        values: values
                    };

                    instance.render(instance.ready, options);
                } else {
                    complete();
                }
                instance.$(".throbber").removeClass("loading");
            }

        });
        return this;
    },

    /**
     *
     * Renders the release form. This differs from standard render methods, as it requires async request/s to the server.
     * As a result when this method is called the first argument is a function that is called when the content has been
     * rendered.
     *
     * @param {function} ready - callback to declare content is ready
     * @return {JIRA.Admin.Version.DeleteForm}
     */
    render: function (ready, options) {

        this.ready = ready;

        var instance = this;

        options = options || {};

        this.model.getUnresolvedIssueCount({

            success: function (unresolvedIssueCount) {

                if (unresolvedIssueCount.issuesUnresolvedCount === 0 && unresolvedIssueCount.issuesUnresolvedCount === 0) {
                    unresolvedIssueCount = false;
                }

                instance.el.innerHTML = JIRA.Templates.Versions.releaseForm({
                    version: instance.model.toJSON(),
                    unresolvedIssueCount: unresolvedIssueCount,
                    versions: instance.model.getMoveVersionsJSON(),
                    projectId: jQuery("meta[name=projectId]").attr("content"),
                    errors: options.errors || {},
                    errorMessages: options.errorMessages || []
                });

                ready.call(instance, instance.el);

                Calendar.setup({
                    singleClick: true,
                    align: "Bl",
                    firstDay: AJS.params.firstDay,
                    button: instance.$("#project-config-versions-release-form-release-date-trigger")[0],
                    inputField: instance.$("#project-config-version-release-form-release-date-field")[0],
                    currentMillis: AJS.params.currentMillis,
                    useISO8061: AJS.params.useISO8061,
                    ifFormat: AJS.params.dateFormat
                });

                // if we focus select, check the radio associated with it also
                jQuery("#moveUnfixedIssuesTo").focus(function () {
                    jQuery("#unresolved-move").attr("checked", "checked");
                });
            }
        });

        return this;
    }

});

/**
 * Renders and handles merge form used in dialog
 */
JIRA.Admin.Version.MergeForm = Backbone.View.extend({

    /**
     * Merges a version on the server.
     *
     * @param {Object} values
     * @param complete
     * @return {JIRA.Admin.Version.MergeForm}
     */
    submit: function (values, complete) {

        if (this.$("#idsToMerge + .error").length > 0) {
            // Don't submit the form while there's an unresolved error.
            this.$("#idsToMerge-textarea").focus();
            this.$(".button:disabled").attr("disabled", false);
            return this;
        }

        this.$(".throbber").addClass("loading");

        var instance = this,
            idsToMerge = values["idsToMerge"],
            mergeToVersion = values["idMergeTo"];

        if (idsToMerge.length > 1) {
            idsToMerge = _.without(idsToMerge, mergeToVersion);
        }

        if (!idsToMerge || idsToMerge.length === 0) {
            complete();
        }

        var errorMessages = [];

        var versionCount = idsToMerge.length,
            noValidationErrors = true;

        var mergedCount = 0;

        jQuery.each(idsToMerge, function (index, id) {

            var versionToMerge = instance.collection.get(id);

            versionToMerge.destroy({
                data: {
                    moveAffectedIssuesTo: mergeToVersion,
                    moveFixIssuesTo: mergeToVersion
                },
                complete: function(status, responseData) {

                    if (status === 400 && responseData && responseData.errorMessages) {
                        noValidationErrors = false;
                    }

                    if (responseData && responseData.errorMessages && responseData.errorMessages.length > 0) {
                        errorMessages = errorMessages.concat(responseData.errorMessages);
                    }

                    mergedCount++;

                    if (mergedCount === versionCount) {

                        if (noValidationErrors) {
                            complete();
                        } else {
                            instance.$(".throbber").removeClass("loading");
                            instance.render(instance.ready, {
                                errorMessages: errorMessages
                            });
                        }

                    }
                }
            });

        });
        return this;
    },

    /**
     *
     * Renders the release form. This differs from standard render methods, as it requires async request/s to the server.
     * As a result when this method is called the first argument is a function that is called when the content has been
     * rendered.
     *
     * @param {function} ready - callback to declare content is ready
     * @return {JIRA.Admin.Version.DeleteForm}
     */
    render: function (ready, options) {

        this.ready = ready;

        var instance = this;

        options = options || {};

        instance.el.innerHTML = JIRA.Templates.Versions.mergeForm({
            versions: instance.collection.toJSON(),
            errorMessages: options.errorMessages || []
        });

        ready.call(instance, instance.el);

        return this;
    }

});



/**
 * Renders and handles submission of delete form used in dialog
 */
JIRA.Admin.Version.DeleteForm = Backbone.View.extend({

    /**
     * Destorys model on server
     *
     * @param {Object} values
     * @param complete
     * @return {JIRA.Admin.Version.DeleteForm}
     */
    submit: function (values, row, complete) {

        this.$(".throbber").addClass("loading");


        if (values.fix !== "swap") {
            delete values.moveFixIssuesTo;
        }

        if (values.affects !== "swap") {
            delete values.moveAffectedIssuesTo;
        }

        this.model.destroy({
            data: values,
            success: function () {
                complete();
            },
            error: function () {
                complete();
            }
        });

        return this;
    },

    /**
     *
     * Renders delete form. This differs from standard render methods, as it requires async request/s to the server.
     * As a result when this method is calle the first argument is a function that is called when the content has been
     * rendered.
     *
     * @param {function} ready - callback to declare content is ready
     * @return {JIRA.Admin.Version.DeleteForm}
     */
    render: function (ready) {

        var instance = this;

        this.model.getRelatedIssueCount({

            success: function (relatedIssueCount) {

                if (relatedIssueCount.issuesFixedCount === 0 && relatedIssueCount.issuesAffectedCount === 0) {
                    relatedIssueCount = false;
                }

                instance.el.innerHTML = JIRA.Templates.Versions.deleteForm({
                    version: instance.model.toJSON(),
                    relatedIssueCount: relatedIssueCount,
                    versions: instance.model.getSwapVersionsJSON(),
                    projectId: jQuery("meta[name=projectId]").attr("content")
                });

                ready.call(instance, instance.el);
                
                // if we focus select, check the radio associated with it also
                jQuery("#moveAffectedIssuesTo").focus(function () {
                    jQuery("#affects-swap").attr("checked", "checked");
                });

                jQuery("#moveFixIssuesTo").focus(function () {
                    jQuery("#fix-swap").attr("checked", "checked");
                });

            }
        });

        return this;
    }
});
