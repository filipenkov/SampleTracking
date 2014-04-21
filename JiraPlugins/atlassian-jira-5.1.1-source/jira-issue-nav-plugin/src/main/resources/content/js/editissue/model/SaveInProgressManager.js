JIRA.Issues.SaveInProgressManager = JIRA.Issues.BaseModel.extend({

    properties: [
        "savesInProgress"
    ],

    namedEvents:["savingStarted", "saveSuccess", "saveError"],

    initialize: function () {
        this.setSavesInProgress([]);

    },

    saveIssue:function(issueId, fieldsToSave, data, ajaxProperties) {

        var instance = this,
            saveInProgress,
            allParams;

        allParams = _.extend(data, {
            issueId: issueId,
            atl_token: atl_token(),
            singleFieldEdit: true,
            fieldsToForcePresent: fieldsToSave
        });

        var ajaxOpts = _.extend({
            type: "POST",
            url: contextPath + "/secure/IssueAction.jspa?decorator=none",
            error: function (xhr) {
                instance._handleSaveError(issueId, fieldsToSave, xhr);
            },
            success: function (resp, statusText, xhr, smartAjaxResult) {
                var responseData = smartAjaxResult.data;
                // Was the response HTML?
                if (typeof responseData == "string") {
                    instance._handleHtmlResponse(issueId, fieldsToSave, responseData);
                } else {
                    instance.triggerSaveSuccess(issueId, fieldsToSave, responseData);
                }
            },
            complete: function () {
                instance.removeSaveInProgress(saveInProgress);
                JIRA.trigger(JIRA.Events.INLINE_EDIT_SAVE_COMPLETE);
            },
            data: allParams
        }, ajaxProperties);


        saveInProgress = JIRA.SmartAjax.makeRequest(ajaxOpts);
        this.addSaveInProgress(saveInProgress);
        this.triggerSavingStarted(issueId, fieldsToSave, data);

    },

    hasSavesInProgress: function () {
        return this.getSavesInProgress().length > 0;
    },

    removeSaveInProgress: function (saveInProgress) {
        this.setSavesInProgress(_.without(this.getSavesInProgress(), saveInProgress));
    },

    addSaveInProgress: function (saveInProgress) {
        var savesInProgress = this.getSavesInProgress();
        savesInProgress.push(saveInProgress);
        this.setSavesInProgress(savesInProgress);
    },

    _handleHtmlResponse: function(issueId, fieldsToSave, responseData) {
        var instance = this;
        var responseBody = AJS.$(AJS.extractBodyFromResponse(responseData));
        var updatedXSRFToken = responseBody.find("#atl_token").val();

        // If we've received an XSRF token error, an updated token will be in the response.
        if (updatedXSRFToken) {
            AJS.$("#atlassian-token").attr("content", updatedXSRFToken);
        }

        var dialog = new JIRA.FormDialog({
            offsetTarget: "body",
            content: responseBody
        });

        this.triggerSaveError(issueId, fieldsToSave);

        // If clicking the XSRF dialog's "Retry" button worked, continue.
        dialog._handleServerSuccess = function (xsrfResponseData) {
            dialog.hide();
            var data = instance._parseResponse(xsrfResponseData);
            if (data) {
                instance.triggerSaveSuccess(issueId, fieldsToSave, data);
            }
        };

        dialog.show();
    },

    _handleSaveError: function (issueId, fieldsToSave, xhr) {
        var data = this._parseResponse(xhr.responseText);
        if (data) {
            this.triggerSaveError(issueId, fieldsToSave, data);
        }
    },

    /**
     * Attempts to parse raw response to JSON. If parsing fails, shows a global error message and returns null
     * @param responseText raw http response data
     */
    _parseResponse: function(responseText) {
        try {
            return JSON.parse(responseText);
        } catch (e) {
            // parse JSON failed
            this._showFatalErrorMessage();
            return null;
        }
    },

    _showFatalErrorMessage: function() {
        // TODO: would be nice to extract this error from smartAjax and make it uniform in JIRA
        var msg = '<p>' + AJS.I18n.getText("common.forms.ajax.error.dialog.heading") + '</p>' +
            '<p>' + AJS.I18n.getText("common.forms.ajax.error.dialog") + '</p>';
        JIRA.Messages.showErrorMsg(msg, {
            closeable: true
        });
    }
});

// Events
JIRA.Events.INLINE_EDIT_SAVE_COMPLETE = "inlineEditSaveComplete";