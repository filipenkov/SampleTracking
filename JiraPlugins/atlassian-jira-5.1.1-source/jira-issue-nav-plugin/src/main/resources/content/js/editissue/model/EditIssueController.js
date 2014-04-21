AJS.namespace("JIRA.Issues.EditIssueController");

/**
 * Controls edits for the currently viewed issue.
 * Note: the actual saving of edits  is done by the JIRA.Issues.SaveInProgressManager
 */
JIRA.Issues.EditIssueController = JIRA.Issues.BaseModel.extend({

    properties: [
        /**
         * @type number
         */
        "issueId",
        /**
         * Issue Key
         * @type string
         */
        "issueKey",
        /**
         * jQuery element that contains the view issue html
         * @type jQuery
         */
        "issueViewContext",

        /**
         * Collection of JIRA.Issues.IssueFieldModel
         * @type JIRA.Issues.IssueFieldCollection
         */
        "fields",
        /**
         * @type JIRA.Issues.IssueEventBus
         */
        "issueEventBus"
    ],

    namedEvents: [
        /**
         * Lets whoever is listening (SaveInProgressManager) know we want to save the current edits
         */
        "save"
    ],

    /**
     * @constructor
     */
    initialize: function () {

        _.bindAll(this);

        this.set({
            fields: new JIRA.Issues.IssueFieldCollection()
        }, {silent:true});

        this.getFields()
                .bind("add", this.createFieldView)
                .bind("updated", this.handleFieldUpdate)
                .bind("save", this.save);

        this.getIssueEventBus().bindSavingStarted(this._handleSavingStarted);
        this.getIssueEventBus().bindSaveSuccess(this._handleSaveSuccess);
        this.getIssueEventBus().bindSaveError(this._handleSaveError);
        this.getIssueEventBus().bindDismiss(this.save);
        this.getIssueEventBus().bindSave(this.save);
        this.getIssueEventBus().bindSave(this.cancelUneditedFields);
    },

    _saveById: function(id) {
        var model = this.getFields().get(id);
        if (model) {
            model.blurEdit();
        }
    },

    /**
     * Handles case where the JIRA.Issues.SaveInProgressManager returns server/validation errors for issue.
     *
     * @param {Number} issueId
     * @param {Array} attemptedSaveIds
     * @param {Object} response
     * ... {Array} errorMessages
     * ... {Object} errors - Validation errors
     */
    _handleSaveError:function(issueId, attemptedSaves, response) {
        var instance = this;
        if (response) {
            this.applyErrors(response);
        } else {
            _.each(attemptedSaves, function (id) {
                var model = instance.getFields().get(id);
                if (model) {
                    model.handleSaveError();
                }
            });
        }
    },

    /**
     * Lets all the models know that saving has started
     *
     * @param savingIds
     * @private
     */
    _handleSavingStarted: function (savingIds) {
        this.getFields().each(function (model) {
             if (_.include(savingIds, model.id)) {
                 model.handleSaveStarted();
             }
        });
    },

    /*
     * Handles the situation where a field becomes visible but doesn't have a
     * view associated with it, meaning it's not possible to inline-edit it.
     *
     * @param {Object} fieldModel The field model that was updated.
     */
    handleFieldUpdate: function (fieldModel) {
        // If a view has been created for the field, its trigger element (or
        // one of its descendants) will have the "editable-field" class.
        var trigger = jQuery(JIRA.Issues.IssueFieldUtil.getFieldSelector(fieldModel.id));
        if (!trigger.hasClass("editable-field")) {
            this.createFieldView(fieldModel);
        }
    },

    /**
     * Applies an error collection to the current issue page. Useful when restoring an issues state after navigating away.
     *
     * @param errorCollection
     */
    applyErrors: function (lastEditData) {
        var errorCollection = lastEditData.errorCollection;
        if (errorCollection && errorCollection.errors) {
            this.getFields().each(function (model) {
                if (errorCollection.errors[model.id]) {
                    var updatedField = _.find(lastEditData.fields, function(field) {
                        return field.id == model.id;
                    });
                    if(updatedField) {
                        model.setValidationError(updatedField.editHtml, errorCollection.errors[model.id]);
                    }
                }
            });
        }

        // In the case of error messages we pin up a global error message
        if (errorCollection.errorMessages && errorCollection.errorMessages.length) {
            var html = JIRA.Templates.ViewIssue.Fields.saveErrorMessage({
                errors: errorCollection.errorMessages,
                issueKey: this.getIssueKey()
            });
            JIRA.Messages.showErrorMsg(html, {
                closeable: true
            });
        }
    },


    /**
     * Removes all field models and edital views
     */
    reset: function () {
        this.getFields().reset()
    },

    /**
     * Cancels any edit is progress
     */
    cancelEdit: function () {
        this.getFields().each(function (model) {
            model.cancelEdit();
        });
    },


    /**
     * Handles case where the JIRA.Issues.SaveInProgressManager saves successfully for issue
     *
     * @param {Number} issueId
     * @param {Array} savedFieldIds - Ids for successfully saved fields
     */
    _handleSaveSuccess:function(issueId, savedFieldIds) {
        var savedFieldModels = this.getFields().filter(function(fieldModel) {
            return _.indexOf(savedFieldIds, fieldModel.id) >=0;
        });
        _.each(savedFieldModels, function(model) {
            model.handleSaveSuccess();
        });
    },

    /**
     * Gets the ids of fields in edit mode that need to be saved
     *
     * @return Array<String>
     */
    getDirtyEditsInProgress: function() {
        return _.pluck(this.getFields().filter(function (model) {
            return model.getEditing() && model.isDirty();
        }), "id");
    },

    /**
     * Gets the ids of fields in edit mode
     *
     * @return Array<String>
     */
    getEditsInProgress: function () {
        return _.pluck(this.getFields().filter(function (model) {
            return model.getEditing();
        }), "id");
    },

    /**
     * Saves all the fields that are currently in edit mode with dirty (changed) values.
     * Note: The actual save is delegated to the JIRA.Issues.SaveInProgressManager
     *
     * @param model
     * @param ajaxProperties
     */
    save: function(model, ajaxProperties) {

        var params = {}, toSaveIds = [];

        var toSave = [model];
        if (!model) {
            toSave = this.getFields().filter(function (model) {
                return !model.getSaving() && model.getEditing() && model.isDirty();
            });
        } else if (!model.getEditing() || model.getSaving()) {
            return;
        }

        _.each(toSave, function(model) {
            toSaveIds.push(model.getId());
            _.extend(params, model.getCurrentParams());
        });

        if(toSaveIds.length > 0) {
            this.triggerSave(this.getIssueId(), toSaveIds, params, ajaxProperties);
        }
    },

    /**
     * Cancels any fields which are not dirty (have not been edited) and have no validation errors.
     */
    cancelUneditedFields: function() {
        this.getFields().each(function(model) {
            if (model.getEditing() && !model.isDirty() && !model.hasValidationError()) {
                model.cancelEdit();
            }
        });
    },

    /**
     * Updates/creates fields from data
     *
     * @param {Object} data - Fields data from server @see JIRA.Issues.ViewIssueController.getFieldsData
     */
    update: function(data) {
        this.getFields().update(data);
    },

    /**
     * Creates fields view
     *
     * @param {JIRA.Issues.IssueFieldModel} fieldModel
     */
    createFieldView: function(fieldModel) {
        var editableFieldTrigger = jQuery(JIRA.Issues.IssueFieldUtil.getFieldSelector(fieldModel.id));
        if (editableFieldTrigger.length === 1) {
            new JIRA.Issues.IssueFieldView({
                model: fieldModel,
                el: editableFieldTrigger,
                issueEventBus: this.getIssueEventBus()
            });
        }
    }
});
