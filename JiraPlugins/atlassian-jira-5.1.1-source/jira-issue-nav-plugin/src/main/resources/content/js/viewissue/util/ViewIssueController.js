/**
 * Handles loading of issues. Including the glue between all the view issue models/controllers
 */
JIRA.Issues.ViewIssueController = JIRA.Issues.BaseModel.extend({

    namedEvents: [

    /**
     * Event triggered when we have no idea what to do with it. For instance if the error comes back after the issue
     * has been dismissed.
     */
        "unhandledSaveError",

    /**
     * Whenever we go to the server to update the issue data. We fire this event so others can use it also.
     * For example search result row.
     */
        "issueDataUpdated",

    /**
     * Triggered when loading of an issue completes (including when KickAss is applied to existing DOM content).
     */
        "issueLoaded",

    /**
     * Whenever we start loading an issue. For example, searchResults are interested in this as they need to select row.
     */
        "issueLoading",

    /**
     * Take me to search!
     */
        "returnToSearch"
    ],

    properties: [

    /**
     * Handles all saves across multiple issues
     * @type JIRA.Issues.SaveInProgressManager
     */
        "saveInProgressManager",


    /**
     * jQuery element where the issue view will be appended
     * @type jQuery
     */
        "viewIssueContext",


    /**
     * The loaded issue
     * @type JIRA.Issues.SearchPageModel
     */
        "selectedIssueModel",

    /**
     * @type JIRA.Issues.EditIssueController
     */
        "selectedEditIssueController"
    ],

    /**
     * @constructor
     */
    initialize: function () {
        var instance = this;

        _.bindAll(this); // Set context of all methods to be this

        var win = jQuery(window);

        // Why not just use jQuery I hear you say?? Well it doesn't work for IE!
        // JRADEV-11612
        var oldBeforeUnload = window.onbeforeunload;
        window.onbeforeunload = function () {
            return oldBeforeUnload.apply(this, arguments) ||
                    instance._handleUnload.apply(this, arguments);
        };
        this.on("destroy", function() {
            window.onbeforeunload = oldBeforeUnload;
        });

        JIRA.bind(JIRA.Events.REFRESH_ISSUE_PAGE, function (e, issueId, options) {
            if (parseInt(issueId, 10) === parseInt(instance.getSelectedIssueModel().id, 10)) {
                instance.getSelectedIssueModel().getIssueEventBus().triggerRefreshIssue(options);
            }
        });

        this.getSaveInProgressManager().bindSaveSuccess(function (issueId, savedFieldIds, responseData) {
            if (instance.getSelectedIssueModel() && issueId === instance.getSelectedIssueModel().id) {
                instance._handleSelectedIssueSaveSuccess(issueId, savedFieldIds, responseData);
            }
        });

        this.getSaveInProgressManager().bindSaveError(function (issueId, attemptedSavedIds, response) {
            if (response) {
                instance._parseLoadResponseData(response);
            }
            var hasModal = AJS.$(".aui-blanket:visible").length > 0;
            var isCurrentIssue = instance.getSelectedIssueModel() && issueId === instance.getSelectedIssueModel().id;
            if (isCurrentIssue) {
                if (hasModal) {
                    // Deferred error
                    new JIRA.Issues.UnhandledSaveErrorView().render({
                        issueEntity: instance.getSelectedIssueModel().getEntity(),
                        attemptedSavedIds: attemptedSavedIds,
                        response: response,
                        viewIssueLoader: instance,
                        isCurrentIssue: isCurrentIssue
                    });
                    instance.getSelectedEditIssueController().cancelEdit();
                } else {
                    instance._handleSelectedIssueSaveError(issueId, attemptedSavedIds, response);
                }
            } else {
                instance.triggerUnhandledSaveError(issueId, attemptedSavedIds, response);
            }
            instance._checkRefreshIssueRowData(issueId);
        });

        this.getSaveInProgressManager().bindSavingStarted(function (issueId, savingIds) {
            instance._recordTimeStampForAnalytic();
            if (instance.getSelectedIssueModel() && issueId === instance.getSelectedIssueModel().id) {
                instance.getSelectedIssueModel().getIssueEventBus().triggerSavingStarted(savingIds);
            }
        });
    },

    replaySaveError: function (issueEntity, attemptedSavedIds, response) {
        var issueId = issueEntity.id;
        if (this.getSelectedIssueModel() && issueId === this.getSelectedIssueModel().id) {
            // We have navigated away and back again, so we can just reapply the errors to the selected issue
            this._handleSelectedIssueSaveError(issueId, attemptedSavedIds, response);

            // If there's only one field in edit mode after handling the save error, then it's the culprit.
            // Automatically focus it for the user so they can just start typing after clicking "Fix Errors".
            var editingFieldModels = this.getSelectedEditIssueController().getFields().filter(function(fieldModel) {
                return fieldModel.getEditing();
            });

            if (editingFieldModels.length === 1) {
                editingFieldModels[0].triggerFocusRequested();
            }
        } else {
            this.load(issueEntity, response);
        }
    },

    /**
     * Destroys references and handlers attached to the currently view issue screen. We do this when navigating
     * away from an issue.
     */
    _destroySelectedViewIssue: function() {
        var selectedIssueModel = this.getSelectedIssueModel();
        if(this.getSelectedIssueModel()) {
            this.setSelectedIssueModel(undefined);
            selectedIssueModel.dismiss();
        }
    },

    /**
     * Gets the edit fields data from server, including edit html, if it is required etc.
     *
     * @param {Number} issueId
     */
    _getFieldsData: function (issueId) {

        var instance = this,
            deferred = jQuery.Deferred();

        jQuery.ajax({
            url: contextPath + "/secure/EditAction!default.jspa?decorator=none",
            data: {
                issueId: issueId
            },
            success: function (data) {
                var resp = JIRA.Issues.IssueFieldUtil.transformFieldHtml(data);
                deferred.resolveWith(instance, [resp.fields]);
            },
            error: function (xhr) {
                // 400 bad request when we don't have edit permissions. We still want to continue as normal in this
                // case so we resolve rather than reject.
                if (xhr.status === 400 || xhr.status === 401) {
                    deferred.resolveWith(instance, [null]);
                } else {
                    deferred.rejectWith(instance, arguments);
                }
            }
        });

        return deferred.promise();
    },

    /**
     * Gets the issue data, including issue operations & summary. Used to trigger updating for rows.
     *
     * @param {Number} issueId
     * @return {jQuery.Deferred}
     */
    _checkRefreshIssueRowData: function(issueId) {
        if (this._refreshRowDataAfterUpdate) {
            var instance = this;
            return jQuery.ajax({
                url: contextPath + "/rest/api/latest/issue/" + issueId + "?expand=operations",
                success: function (data) {
                    instance.triggerIssueDataUpdated(issueId, data);
                }
            });
        }
    },

    /**
     * Causes row data to be refetched after a successful update
     */
    refreshRowDataAfterUpdate: function() {
        this._refreshRowDataAfterUpdate = true;
        return this;
    },

    /**
     * Parses raw response into an object our PanelsModel can consume
     *
     * @param {Object} resp - raw response from server
     */
    _parsePanelData: function (resp) {
        return {
            leftPanels: resp.leftPanels,
            rightPanels: resp.rightPanels,
            infoPanels: resp.infoPanels
        };
    },

    /**
     * Shows loading indicator in center of screen if the loading of the issue has taken more than 200ms
     */
    showLoadingIndicator: function () {

        var instance = this,
                heightOfSprite = 440,
                currentOffsetOfSprite = 0;

        this.hideLoadingIndicator();

        this.loadingWait = window.setTimeout(function () {

            clearInterval(instance.loadingTimer);

            instance.$loadingIndicator = AJS.$("<div />").addClass("aui-loading")
                    .appendTo("body").show();

            instance.loadingTimer = window.setInterval(function () {
                if (currentOffsetOfSprite === heightOfSprite) {
                    currentOffsetOfSprite = 0;
                }
                currentOffsetOfSprite = currentOffsetOfSprite + 40;
                instance.$loadingIndicator.css("backgroundPosition", "0 -" + currentOffsetOfSprite + "px");
            }, 50);
        }, 200);
    },


    /**
     * Hides loading indicator if there is one.
     */
    hideLoadingIndicator: function () {
        clearInterval(this.loadingWait);
        clearInterval(this.loadingTimer);
        if (this.$loadingIndicator) {
            this.$loadingIndicator.remove();
            delete this.$loadingIndicator;
        }
    },

    /**
     * Refreshes Issue, by updating facets with new data.
     *
     * @param {Object} props
     * ... {Number} issueId
     * ... {Array<String>} fieldsSaved - The update may come as the result of a save. This array includes the ids of any fields ed before hand.
     * ... {Array<String>} fieldsInProgress - Array of fields that are still in edit mode or still saving.
     * ... {JIRA.Issues.IssueViewModel} issueViewModel
     * ... {JIRA.Issues.EditIssueController} editIssueController
     * ... {Boolean}initialize - parameter indicating if it is the first time the update has been called.
     */
    _update: function (props) {

        var instance = this;

        this.startIssueLoad = new Date();

        jQuery.ajax({
            url: contextPath + "/secure/IssueAction!default.jspa?decorator=none&issueId=" + props.issueId,
            success: function(data) {
                instance._handleLoadSuccess(props, data);
            },
            error: function (xhr) {
                var errorCollection;
                try {
                    errorCollection = JSON.parse(xhr.responseText) || {};
                } catch (e) {
                    errorCollection = {};
                }

                // If any of our requests for data fail we fall into here
                var html = JIRA.Templates.ViewIssue.Body.errorsLoading(errorCollection);
                JIRA.Messages.showErrorMsg(html, {closeable: true});

                instance._handleLoadError(props);
            }
        });
    },

    _handleLoadSuccess: function(props, data) {

        var clientTime = new Date().getTime();

        this._parseLoadResponseData(data);
        this._checkRefreshIssueRowData(props.issueId);

        if (data) {
            var editable = props.editable = data.fields && data.fields.length;

            if (editable) {
                // we only care about these if we are still editable
                props.fieldsInProgress = props.editIssueController.getEditsInProgress();
            }

            if (props.lastEditData && props.lastEditData.errorCollection) {
                props.editIssueController.applyErrors(props.lastEditData);
            }

            if (editable) {
                // this has to come after props.editIssueController.reset()
                props.issueViewModel.update(data, props);
                props.editIssueController.update(data.fields, props.fieldsSaved);
            } else {
                props.editIssueController.reset();
                props.issueViewModel.update(data, props);
            }
        }

        this._handleLoadComplete(props);
        this.triggerIssueLoaded(props.issueViewModel);
        if (props.success) {
            props.success();
        }
        if (props.complete) {
            props.complete();
        }

        console.log("Client updated issue in " + (new Date().getTime() - clientTime) + "ms");
    },

    _handleLoadError: function(props, data) {
        // if no longer editable, reload page
        if (data && (!data.fields || !data.fields.length)) {
            props.editIssueController.reset();
            props.issueViewModel.update(data, props);
        }
        this._handleLoadComplete(props);
        if (props.error) {
            props.error();
        }
        if (props.complete) {
            props.complete();
        }
    },

    _handleLoadComplete: function(props) {
        if (this.startIssueLoad) {
            JIRA.Issues.Analytics.trigger("kickass.issueLoadDuration", {duration:new Date() - this.startIssueLoad});
        }

        this.hideLoadingIndicator();

        props.issueViewModel.getIssueEventBus().triggerIssueRefreshed(props.issueId);
        JIRA.trace("jira.issue.refreshed", {
            id: props.issueId
        });

        JIRA.trigger(JIRA.Events.ISSUE_REFRESHED, [props.issueId]);

        this._logAnalyticAfterUpdate();
    },

    _parseLoadResponseData: function(data) {
        data = JIRA.Issues.IssueFieldUtil.transformFieldHtml(data);
        if (data.panels) {
            data.panels = this._parsePanelData(data.panels);
        }
    },

    dismiss: function () {
        this.deactivateViewIssueScrolling();
        this._destroySelectedViewIssue();
    },

    deactivateViewIssueScrolling: function() {
        if(this.getSelectedIssueModel()) {
            this.trigger("issueDismissed", this.getSelectedIssueModel());
        }
    },

    /**
     * Records the time a save was started
     * @private
     */
    _recordTimeStampForAnalytic: function() {
        // TODO: flaky. Assumes that requests will always come back in the order in which they were started.
        // This is ok as are refactoring save & reload to be in the same request, which will make this much easier.
        if (!this._saveStarted) {
            this._saveStarted = [];
        }
        this._saveStarted.push((new Date).getTime()); // Date.now() doesn't work IE8
    },

    /**
     * Logs the total time between save start and save end
     * @private
     */
    _logAnalyticAfterUpdate: function() {
        if (this._saveStarted && this._saveStarted.length) {
            var started  = this._saveStarted.shift();
            JIRA.Issues.Analytics.trigger("kickass.issueTotalSaveDuration", { duration: (new Date).getTime() - started }); // Date.now() doesn't work IE8
        }
    },

    /**
     * Handles save error by notifying the JIRA.Issues.IssueEventBus
     *
     * @param issueId
     * @param attemptedSaveIds
     * @param response
     */
    _handleSelectedIssueSaveError: function (issueId, attemptedSaveIds, response) {

        var editIssueController = this.getSelectedEditIssueController(),
                eventBus = this.getSelectedIssueModel().getIssueEventBus(),
                issueViewModel = this.getSelectedIssueModel(),
                savesInError, successfulSaves;

        if (response) {
            savesInError = _.keys(response.errorCollection.errors);
            successfulSaves = _.without(attemptedSaveIds, savesInError);
        }

        eventBus.triggerSaveError(issueId, attemptedSaveIds, response);

        this._handleLoadError({
            editIssueController: editIssueController,
            issueViewModel: issueViewModel,
            issueId: issueId,
            fieldsSaved: successfulSaves,
            initialize: false
        }, response);
    },


    /**
     * Handles save success by
     * - refreshing the issue
     * - notifying the JIRA.Issues.IssueEventBus
     * - reloading data from response
     *
     * @param issueId
     * @param savedFieldIds
     * @param data
     */
    _handleSelectedIssueSaveSuccess: function (issueId, savedFieldIds, data) {
        var editIssueController = this.getSelectedEditIssueController(),
                eventBus = this.getSelectedIssueModel().getIssueEventBus(),
                issueViewModel = this.getSelectedIssueModel();


        eventBus.triggerSaveSuccess.apply(eventBus, arguments);

        this._handleLoadSuccess({
            editIssueController: editIssueController,
            issueViewModel: issueViewModel,
            issueId: issueId,
            fieldsSaved: savedFieldIds,
            fieldsInProgress: editIssueController.getEditsInProgress(),
            initialize: false
        }, data);
    },

    /**
     * Handling for when we navigate away from the page. For example closing browser window.
     */
    _handleUnload: function() {
        var editIssueController = this.getSelectedEditIssueController();
        if (editIssueController) {
            if (editIssueController.getDirtyEditsInProgress().length > 0) {
                return AJS.I18n.getText("viewissue.editing.leave");
            }
        }
    },

    /**
     * Creates all the objects and wires them up
     *
     * @param issueEntity
     */
    _initIssueObjects: function (issueEntity) {
        var instance = this;

        var issueEventBus = new JIRA.Issues.IssueEventBus({
            issueId: issueEntity.id
        });

        issueEventBus.bindOpenFocusShifter(this._openFocusShifter);
        issueEventBus.bindRefreshIssue(function (options) {
            options = options || {};
            instance._update({
                editIssueController: editIssueController,
                issueViewModel: issueViewModel,
                issueId: issueEntity.id,
                success: options.success,
                error: options.error,
                complete: options.complete
            });
        });

        var issueViewModel = new JIRA.Issues.IssueViewModel({
            id: issueEntity.id,
            issueEventBus: issueEventBus,
            statusColorSupport: issueEntity.statusColorSupport
        });

        issueViewModel.bindReturnToSearch(function () {
            instance.triggerReturnToSearch();
        });

        this.setSelectedIssueModel(issueViewModel);

        // Edit Issue Controller

        var editIssueController = new JIRA.Issues.EditIssueController({
            issueId: issueEntity.id,
            issueKey: issueEntity.key,
            issueViewContext: jQuery(this.getViewIssueContext()),
            issueEventBus: issueEventBus
        });

        editIssueController.bindSave(function (issueId, toSaveIds, params, ajaxProperties) {
            instance.getSaveInProgressManager().saveIssue(issueId, toSaveIds, params, ajaxProperties)
        });

        this.setSelectedEditIssueController(editIssueController);

        new JIRA.Issues.IssueView({
            model: issueViewModel,
            el: this.getViewIssueContext()
        });

        return {
            issueEventBus: issueEventBus,
            issueViewModel: issueViewModel,
            editIssueController: editIssueController
        }
    },

    /**
     * Used to apply behaviour to server rendered html
     *
     * @param {Object} issueEntity
     */
    applyToDom: function (issueEntity) {
        var issue;
        issueEntity.id = +issueEntity.id; // Ensure value grabbed from DOM is converted into a number
        issue = this._initIssueObjects(issueEntity);
        this._getFieldsData(issueEntity.id).done(function (fields) {
            var editable = fields && fields.length;
            if(editable) {
                issue.editIssueController.update(fields);
                this._showFocusShifterTip();
            }
        });
        issue.issueViewModel.setEntity(issueEntity);
        issue.issueEventBus.triggerUpdateFromDom(this.getViewIssueContext());
        this.triggerIssueLoaded(issue.issueViewModel);
    },

    /**
     * Requests issue entity. Will not make request if there is one pending for the same issue.
     *
     * @param {Object} issueEntity
     */
    load: function (issueEntity, lastEditData) {

        this.triggerIssueLoading(issueEntity.id, lastEditData);

        this.trigger("beforeIssueRequest");
        this._destroySelectedViewIssue();

        var issue = this._initIssueObjects(issueEntity);

        this._update({
            editIssueController: issue.editIssueController,
            issueViewModel: issue.issueViewModel,
            issueId: issueEntity.id,
            lastEditData: lastEditData,
            initialize: true
        });
        this.showLoadingIndicator();
    },

    _openFocusShifter: function () {
        new JIRA.Issues.FocusShifter({
            viewIssueController: this,
            hideTriggers: {
                "Dialog.show": JIRA,
                "editingStarted focusRequested": this.getSelectedEditIssueController().getFields()
            }
        });
    },

    /**
     * Show the focus shifter tip if it's loaded and the focus shifter will show.
     */
    _showFocusShifterTip: function() {
        var fieldModels = this.getSelectedEditIssueController().getFields().models;
        var tipShouldShow = JIRA.Issues.FocusShifter._shouldShow(fieldModels);
        if (tipShouldShow && JIRA.Issues.FocusShifterTip) {
            new JIRA.Issues.FocusShifterTip();
        }
    }
});

JIRA.Events.ISSUE_REFRESHED = "issueRefreshed";
