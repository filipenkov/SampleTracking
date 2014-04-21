/**
 * A special Model for quick edit. Has special handling for comment field. Comment field can be toggled on and off on
 * configurable form. On unconfigurable form it is always present.
 *
 * @class EditIssueModel
 */
JIRA.QuickForm.EditIssueModel = JIRA.QuickForm.Model.extend({

    /**
     * Gets tabs removing comment field if it is present.
     *
     * @return jQuery.promise
     */
    getUnconfigurableTabs: function () {

        var instance = this,
            deferred = jQuery.Deferred();

        instance.getTabs().done(function (tabs) {

            var newTabRef = tabs.slice(0); // Create a new array reference so we do not modify existing

            newTabRef[0].fields = newTabRef[0].fields.slice(0);

            jQuery.each(newTabRef[0].fields, function (i, field) {
                if (field.id === "comment") {
                    newTabRef[0].fields.splice(i, 1);
                    return false; // bail
                }
            });

            deferred.resolve(newTabRef);
        });

        return deferred.promise();
    },

     /**
     * Gets comment field
     *
     * @return jQuery.promise
     */
    getCommentField: function () {

        var deferred = jQuery.Deferred();

        this.getFields().done(function (fields) {
            jQuery.each(fields, function (i, field) {
                if (field.id === "comment") {
                    deferred.resolve(field);
                }
            })
        });

        return deferred.promise();
    }

});

/**
 * Renders an error message with edit issue furniture
 *
 * @class CreateIssueError
 */
JIRA.QuickForm.EditIssueError = JIRA.QuickForm.Error.extend({
    _render: function (error) {
        return jQuery(JIRA.Templates.Issue.editIssueError({
            errorHtml: error,
            useLegacyDecorator: JIRA.useLegacyDecorator()
        }));
    }
});


/**
 * A View that renders the full version of the edit issue form. The full version does NOT allow fields to be
 * configured.
 *
 * @class UnconfigurableEditIssueForm
 */
JIRA.QuickForm.UnconfigurableEditIssueForm = JIRA.QuickForm.AbstractUnconfigurableForm.extend({

    /**
     * @constructor
     * @param options
     * ... {JIRA.QuickForm.Model} model that gets fields and sets user preferences
     * ... {Array<String>} globalEventNamespaces - Events will be triggered on these namespaces
     * e.g EditForm.switchedToConfigurableForm. EditForm being the specified global namespace.
     * ... {String, Number} issue id
     */
    init: function (options) {
        this.globalEventNamespaces = options.globalEventNamespaces || [];
        this.model = options.model;
        this.issueId = options.issueId;
        this.issueEditedCallback = options.issueEdited;
        
        // Form values will be serialized and POSTed to this url
        this.action = contextPath + "/secure/QuickEditIssue.jspa?issueId=" + options.issueId + "&decorator=none";
        // The container element. All html will be injected into this guy.
        this.$element = jQuery("<div />");
    },

    /**
     * Gets HTML for fields. This includes tabs and tab panes if applicable.
     *
     * @return jQuery.Deferred
     */
    getFieldsHtml: function () {

        var instance = this,
            deferred = jQuery.Deferred(),
            data = {};

        this.model.getUnconfigurableTabs().done(function (tabs) {

            if (tabs.length === 1) {
                data.fields = tabs[0].fields;

            } else {
                data.tabs = tabs;
                data.hasTabs = true;
            }

            deferred.resolveWith(instance, [JIRA.Templates.Issue.issueFields(data)]);

        });

        return deferred.promise();
    },

     /**
     * Reloads window after form has been successfully submitted
     */
    handleSubmitSuccess: function (data) {

        this.triggerEvent("QuickForm.submitted");
        this.triggerEvent("QuickForm.issueEdited");

        if (this.issueEditedCallback) {
            this.issueEditedCallback(data);
        }
    },

    /**
     * Renders complete form
     *
     * @return jQuery.Deferred
     */
     _render: function () {

        var deferred = jQuery.Deferred(),
            instance = this;

        this.getFieldsHtml().done(function (fieldsHtml) {

            this.model.getCommentField().done(function (commentField) {
                var html = JIRA.Templates.Issue.editIssueForm({
                    fieldsHtml: fieldsHtml,
                    issueId: instance.issueId,
                    commentField: commentField,
                    atlToken: atl_token(),
                    useLegacyDecorator: JIRA.useLegacyDecorator()
                });

                instance.$element.html(html);
                deferred.resolveWith(instance, [instance.$element]);
            })

        });

        return deferred.promise();
    }
});

/**
 * A View that renders a configurable version of the edit issue form. This version allows you to reorder fields and
 * add and remove fields.
 *
 * @class ConfigurableEditIssueForm
 */
JIRA.QuickForm.ConfigurableEditIssueForm = JIRA.QuickForm.AbstractConfigurableForm.extend({

    /**
     * @constructor
     * @param options
     * ... {JIRA.QuickForm.Model} model that gets fields and sets user preferences
     * ... {String, Number} issue id
     * ... {Array<String>} globalEventNamespaces - Events will be triggered on these namespaces
     * e.g EditForm.switchedToConfigurableForm. EditForm being the specified global namespace.
     */
    init: function (options) {
        this.model = options.model;
        this.issueEditedCallback = options.issueEdited;
        this.globalEventNamespaces = options.globalEventNamespaces || [];
        this.issueId = options.issueId;
        this.fieldPicker = new JIRA.QuickForm.EditIssuePicker(this);
        // Form values will be serialized and POSTed to this url
        this.action = contextPath + "/secure/QuickEditIssue.jspa?issueId=" + options.issueId + "&decorator=none";
        // The container element. All html will be injected into this guy.
        this.$element = jQuery("<div />").addClass("qf-form");
    },

     /**
     * Reloads window after form has been successfully submitted
     */
    handleSubmitSuccess: function (data) {

        this.triggerEvent("QuickForm.submitted");
        this.triggerEvent("QuickForm.issueEdited");

        if (this.issueEditedCallback) {
            this.issueEditedCallback(data);
        }
    },

    /**
     * Determines if there are any visible fields. Comment field not included.
     *
     * @return Boolean
     */
    hasNoVisibleFields: function () {
        var deferred = jQuery.Deferred(),
            activeFieldIds = this.getActiveFieldIds();

        this.model.getUserFields().done(function (userFields) {
            if (userFields.length === 1 && userFields[0] === "comment") {
                deferred.resolve(false);
            } else if (activeFieldIds.length === 0 || (activeFieldIds.length === 1 && activeFieldIds[0] === "comment")) {
                deferred.resolve(true);
            } else {
                deferred.resolve(false);
            }
        });

        return deferred.promise();
    },

    _render: function () {
        var deferred = jQuery.Deferred(),
            instance = this,
            html = JIRA.Templates.Issue.editIssueForm({
                issueId: this.issueId,
                atlToken: atl_token(),
                isConfigurable: true,
                useLegacyDecorator: JIRA.useLegacyDecorator()
            });

        this.$element.html(html); // add form chrome to container element

         // render fields and picker
        instance.renderFormContents().done(function () {
            instance.getFieldById("comment").done(function (field) {
                if (field) {
                    field.$element.appendTo(instance.getFormContent()); // put comment field at end of form
                }
            });
            deferred.resolveWith(instance, [instance.$element]);
        });

        return deferred.promise();
    }
     
});

/**
 * Renders a list of fields that can be added and removed from an edit issue form
 *
 * @class EditIssuePicker
 */
JIRA.QuickForm.EditIssuePicker = JIRA.QuickForm.FieldPicker.extend({

    render: function () {
        var instance = this;
        this.$element = jQuery(JIRA.Templates.Issue.quickEditIssueFieldPicker());
        this.$content = this.$element.find(".qf-picker-content");
        this.$element.find(".qf-unconfigurable").click(function (e) {
            instance.inlineDialog.hide();
            e.preventDefault();
        });
        return this.renderContents();
    }
});

/**
 * Factory to create Edit Issue model
 *
 * @param {String, Number} issueId
 * @return JIRA.QuickForm.Model
 */
JIRA.QuickForm.Model.createQuickEditIssueModel = function (issueId) {
    return new JIRA.QuickForm.EditIssueModel({
        fieldsResource: contextPath + "/secure/QuickEditIssue!default.jspa?issueId=" + issueId + "&decorator=none",
        userFieldsResource: contextPath + "/rest/quickedit/1.0/userpreferences/edit"
    });
};

/**
 * Fatory to create Edit Issue Form
 *
 * @param {String, Number} issueId
 * @return JIRA.QuickForm.Container
 */
JIRA.QuickForm.createEditIssueForm = function (opts) {

    opts = opts || {};

    // Options is a function so that it can be evaluated whenever we render. We use this to make sure that we have the
    // correct issueId on the navigator where we move from issue to issue using the keyboard.
    var options = function () {

        opts.issueId = JIRA.makePropertyFunc(opts.issueId);

        // model that gets fields and sets user preferences
        var model = JIRA.QuickForm.Model.createQuickEditIssueModel(opts.issueId()),
            configurableForm = new JIRA.QuickForm.ConfigurableEditIssueForm({
                model: model,
                issueId: opts.issueId(),
                issueEdited: opts.issueEdited,
                globalEventNamespaces: ["QuickEdit"]
            }),
            unconfigurableForm = new JIRA.QuickForm.UnconfigurableEditIssueForm({
                model: model,
                issueId: opts.issueId(),
                issueEdited: opts.issueEdited,
                globalEventNamespaces: ["QuickEdit"]
            }),
            fieldPicker = new JIRA.QuickForm.FieldPicker(configurableForm);

        return {
            globalEventNamespaces: ["QuickEdit"],
            model: model,
            fieldPicker: fieldPicker,
            errorHandler: new JIRA.QuickForm.EditIssueError(),
            configurableForm: configurableForm,
            unconfigurableForm: unconfigurableForm
        };
    };

    return new JIRA.QuickForm.Container(options);
};

jQuery(function () {


    // Only init for 5.0+ instances
    if (!JIRA.Version.isGreaterThanOrEqualTo("5.0")) {
        return;
    }

    // Creates edit issue dialog
    var dialog = JIRA.QuickForm.createEditIssueForm({
        issueId: function () {
            return JIRA.Issue.getIssueId() || JIRA.IssueNavigator.getSelectedIssueId();
        },
        issueEdited:function () {
            return AJS.reloadViaWindowLocation();
        }
    }).asDialog({
        trigger: ".issueaction-edit-issue",
        id: "edit-issue-dialog"
    });

    jQuery(AJS).bind("QuickEdit.configurableFormSubmitted", JIRA.notifyNavigatorOfIssueModification)
            .bind("QuickEdit.unconfigurableFormSubmitted", JIRA.notifyNavigatorOfIssueModification);

    // Ensure Assign to me link works and tabs are initiated
    jQuery(document).bind("dialogContentReady", function (e, dialog) {
        JIRA.Issue.wireAssignToMeLink(dialog.get$popupContent());
        AJS.tabs.setup();
    });
});