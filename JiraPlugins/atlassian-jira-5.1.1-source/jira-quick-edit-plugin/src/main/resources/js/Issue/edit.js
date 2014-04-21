/**
 * A special Model for quick edit. Has special handling for comment field. Comment field can be toggled on and off on
 * configurable form. On unconfigurable form it is always present.
 *
 * @class EditIssueModel
 */
JIRA.Forms.EditIssueModel = JIRA.Forms.Model.extend({

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
            });

            deferred.reject();
        });

        return deferred.promise();
    }

});

/**
 * Renders an error message with edit issue furniture
 *
 * @class CreateIssueError
 */
JIRA.Forms.EditIssueError = JIRA.Forms.Error.extend({
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
JIRA.Forms.UnconfigurableEditIssueForm = JIRA.Forms.AbstractUnconfigurableForm.extend({

    /**
     * @constructor
     * @param options
     * ... {JIRA.Forms.Model} model that gets fields and sets user preferences
     * ... {Array<String>} globalEventNamespaces - Events will be triggered on these namespaces
     * e.g EditForm.switchedToConfigurableForm. EditForm being the specified global namespace.
     * ... {String, Number} issue id
     */
    init: function (options) {

        this.globalEventNamespaces = options.globalEventNamespaces || [];
        this.model = options.model;
        this.issueId = options.issueId;
        this.title = options.title;

        // Form values will be serialized and POSTed to this url
        this.action = contextPath + "/secure/QuickEditIssue.jspa?issueId=" + options.issueId + "&decorator=none";
        // The container element. All html will be injected into this guy.
        this.$element = jQuery("<div class='qf-unconfigurable-form' />");
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
        this.triggerEvent("submitted", [data]);
        this.triggerEvent("issueEdited", [data]);
        this.triggerEvent("sessionComplete");
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

            var renderData = {
                fieldsHtml: fieldsHtml,
                title: instance.title,
                issueId: instance.issueId,
                atlToken: instance.model.getAtlToken(),
                useLegacyDecorator: JIRA.useLegacyDecorator(),
                modifierKey: AJS.Navigator.modifierKey(),
                showFieldConfigurationToolBar: !JIRA.Users.LoggedInUser.isAnonymous()
            };

            this.model.getCommentField().then(function (commentField) {

                var html, result;

                renderData.commentField = commentField;

                html = JIRA.Templates.Issue.editIssueForm(renderData);

                // JRADEV-9069 Pull out custom field js to be executed post render
                // Look at JIRA.Forms.Container.render for actual execution
                result = JIRA.extractScripts(html);

                instance.$element.html(result.html);
                deferred.resolveWith(instance, [instance.$element, result.scripts]);
            }, function () {
                var html = JIRA.Templates.Issue.editIssueForm(renderData),
                    result = JIRA.extractScripts(html);
                instance.$element.html(result.html);
                deferred.resolveWith(instance, [instance.$element, result.scripts]);
            });
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
JIRA.Forms.ConfigurableEditIssueForm = JIRA.Forms.AbstractConfigurableForm.extend({

    /**
     * @constructor
     * @param options
     * ... {JIRA.Forms.Model} model that gets fields and sets user preferences
     * ... {String, Number} issue id
     * ... {Array<String>} globalEventNamespaces - Events will be triggered on these namespaces
     * e.g EditForm.switchedToConfigurableForm. EditForm being the specified global namespace.
     */
    init: function (options) {
        this.model = options.model;
        this.globalEventNamespaces = options.globalEventNamespaces || [];
        this.issueId = options.issueId;
        this.title = options.title;

        // Form values will be serialized and POSTed to this url
        this.action = contextPath + "/secure/QuickEditIssue.jspa?issueId=" + options.issueId + "&decorator=none";
        // The container element. All html will be injected into this guy.
        this.$element = jQuery("<div />").addClass("qf-form qf-configurable-form");
    },

     /**
     * Reloads window after form has been successfully submitted
     */
    handleSubmitSuccess: function (data) {
        this.triggerEvent("submitted", [data]);
        this.triggerEvent("issueEdited", [data]);
        this.triggerEvent("sessionComplete");
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
                title: this.title,
                atlToken: instance.model.getAtlToken(),
                isConfigurable: true,
                useLegacyDecorator: JIRA.useLegacyDecorator(),
                modifierKey: AJS.Navigator.modifierKey(),
                showFieldConfigurationToolBar: !JIRA.Users.LoggedInUser.isAnonymous()
            });

        this.$element.html(html); // add form chrome to container element

         // render fields
        instance.renderFormContents().done(function (el, scripts) {
            instance.getFieldById("comment").done(function (field) {
                instance.getFormContent().append(field.$element); // put comment field at end of form
            });
            deferred.resolveWith(instance, [instance.$element, scripts]);
        });

        return deferred.promise();
    }

});

/**
 * Factory to create Edit Issue model
 *
 * @param {String, Number} issueId
 * @return JIRA.Forms.Model
 */
JIRA.Forms.Model.createQuickEditIssueModel = function (issueId) {
    return new JIRA.Forms.EditIssueModel({
        fieldsResource: contextPath + "/secure/QuickEditIssue!default.jspa?issueId=" + issueId + "&decorator=none",
        userFieldsResource: contextPath + "/rest/quickedit/1.0/userpreferences/edit"
    });
};

/**
 * Returns true if a Dialog's title should contain the Issue Key.
 * @return Boolean
 */
JIRA.Dialog.shouldShowIssueKeyInTitle = function() {
    return JIRA.IssueNavigator.isNavigator() || JIRA.Dialog.getAttrFromActiveTrigger("data-issueKey");
};

/**
 * Fatory to create Edit Issue Form
 *
 * @param {String, Number} issueId
 * @return JIRA.Forms.Container
 */
JIRA.Forms.createEditIssueForm = function () {

    function getSelectedIssueId (options) {
        var triggerIssueId = JIRA.Dialog.getAttrFromActiveTrigger("data-issueId");
        if (jQuery.isFunction(options.issueId)) {
            return options.issueId(options);
        } else if (options.issueId) {
            return options.issueId;
        } else if (triggerIssueId) {
            return triggerIssueId;
        } else {
            return JIRA.Issue.getIssueId() || JIRA.IssueNavigator.getSelectedIssueId();
        }
    }

    // JRADEV-8599 Only Issue Navigator or sub-tasks should have Issue key shown in dialog title
    function getContainerTitle () {

        var editActionText = AJS.I18n.getText("admin.issue.operations.edit");

        if (JIRA.Dialog.shouldShowIssueKeyInTitle()) {
            return JIRA.Dialog.getIssueActionTitle(editActionText, true);
        } else {
            return editActionText;
        }
    }

    return function (options) {

        options = options || {};

        return new JIRA.Forms.Container(function () {
            var issueId = getSelectedIssueId(options),
                title = getContainerTitle(),
                model = JIRA.Forms.Model.createQuickEditIssueModel(issueId),
                configurableForm = new JIRA.Forms.ConfigurableEditIssueForm({
                    title: title,
                    model: model,
                    issueId: issueId,
                    globalEventNamespaces: ["QuickEdit"]
                }),
                unconfigurableForm = new JIRA.Forms.UnconfigurableEditIssueForm({
                    title: title,
                    model: model,
                    issueId: issueId,
                    globalEventNamespaces: ["QuickEdit"]
                });
            return {
                globalEventNamespaces: ["QuickEdit"],
                model: model,
                errorHandler: new JIRA.Forms.EditIssueError(),
                configurableForm: configurableForm,
                unconfigurableForm: unconfigurableForm
            };
        });
    };

}();


jQuery(function () {


    // Only init for 5.0+ instances
    if (!JIRA.Version.isGreaterThanOrEqualTo("5.0")) {
        return;
    }

    // Creates edit issue dialog
    JIRA.Forms.createEditIssueForm()
        .asDialog({
            windowTitle: function () {
                return JIRA.Dialog.getIssueActionTitle(AJS.I18n.getText("admin.issue.operations.edit"), false);
            },
            trigger: ".issueaction-edit-issue",
            id: "edit-issue-dialog"
        });

    // Ensure tabs are initiated
    jQuery(document).bind("dialogContentReady", function () {
        AJS.tabs.setup();
    });
});