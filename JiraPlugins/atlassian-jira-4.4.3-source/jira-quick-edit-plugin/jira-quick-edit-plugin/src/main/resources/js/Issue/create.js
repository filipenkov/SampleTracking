/**
 * A special Model for quick create. Has special handling for setup fields, project an issue type
 *
 * @class CreateIssueModel
 */
JIRA.QuickForm.CreateIssueModel = JIRA.QuickForm.Model.extend({

    /**
     * @constructor
     */
    init: function () {
        this.userFieldsResource = contextPath + "/rest/quickedit/1.0/userpreferences/create"
        this._hasRetainFeature = true;
        this._hasVisibilityFeature = true;
        this.retainedFields = [];
    },

    /**
     * Sets issue type, then goes back to the server to get the correct fields.
     *
     * @param {String, Number} issueType
     * @param {String} values - serialized values to populate form with
     *
     * @return jQuery.Proimise
     */
    setIssueType: function (issueType, values) {
        this.issueType = issueType;
        return this.refresh(values);
    },

    /**
     * Sets setProjectId, then goes back to the server to get the correct fields.
     *
     * @param projectId
     * @param {String} values - serialized values to populate form with
     */
    setProjectId: function (projectId, values) {
        this.projectId = projectId;
        return this.refresh(values);
    },

    /**
     * Gets active fields. For Create Issue, this means all required fields also
     */
    getActiveFieldIds: function () {
        var instance = this,
            deferred = jQuery.Deferred(),
            activeFieldIds = [];

        this.getUserFields().done(function (userfields) {

            jQuery.each(userfields, function (i, fieldId) {
                activeFieldIds.push(fieldId);
            });

            instance.getRequiredFields().done(function (requiredFields) {
                jQuery.each(requiredFields, function (i, requiredField) {
                    if (jQuery.inArray(requiredField.id, activeFieldIds) === -1) {
                        activeFieldIds.push(requiredField.id);
                    }
                });
                deferred.resolve(activeFieldIds);
            });
        });

        return deferred.promise();
    },

    /**
     * Specifies on a per field basis if it's visibility can be configured. Can it be added and removed from the form.
     * In the case of quick create, required fields can not be removed.
     *
     * @param descriptor - field descriptor
     * @return {Boolean}
     */
    hasVisibilityFeature: function (descriptor) {
        return !descriptor.required;
    },

    /**
     * Gets fields resource url, adding issueType & projectId to the request if available.
     *
     * @return {String}
     */
    getFieldsResource: function () {
        var fieldsResource =  contextPath + "/secure/QuickCreateIssue!default.jspa?decorator=none";

        if (this.issueType) {
            fieldsResource = fieldsResource + "&issuetype=" + this.issueType;
        }

        if (this.projectId) {
            fieldsResource = fieldsResource + "&pid=" + this.projectId;
        }

        return fieldsResource;
    },

    addFieldToRetainValue: function (id) {
        if (id !== "summary") {
            this._super(id);
        }
    },

    /**
     * Gets required fields
     */
    getRequiredFields: function () {
        var deferred = jQuery.Deferred(),
            requiredFields = [];

        this.getFields().done(function (fields) {
            jQuery.each(fields, function (i, field) {
                if (field.required) {
                    requiredFields.push(field);
                }
            });
            deferred.resolve(requiredFields);
        });

        return deferred.promise();
    },


    /**
     * Gets fields that can be configured. Configured meaning, fields that can have features such as pinned values and
     * visibility toggling applied to them
     *
     * @return jQuery.Promise
     */
    getConfigurableFields: function () {

        var deferred = jQuery.Deferred(),
            issueFields = [];

        this.getFields().done(function (fields) {
            jQuery.each(fields, function (i, field) {
                if (field.id !== "project" && field.id !== "issuetype") {
                    issueFields.push(field);
                }
            });
            deferred.resolve(issueFields);
        });

        return deferred.promise();
    },

    /**
     * Gets project and issue type field. Used to get the correct fields for creating an issue.
     */
    getIssueSetupFields: function () {

        var deferred = jQuery.Deferred(),
            issueSetupFields = [];

        this.getFields().done(function (fields) {
            jQuery.each(fields, function (i, field) {
                if (field.id === "project" || field.id === "issuetype") {
                    issueSetupFields.push(field);
                }
            });
            deferred.resolve(issueSetupFields);
        });

        return deferred.promise();
    }

});

/**
 * Renders an error message with create issue furniture
 *
 * @class CreateIssueError
 */
JIRA.QuickForm.CreateIssueError = JIRA.QuickForm.Error.extend({
    _render: function (error) {
        return jQuery(JIRA.Templates.Issue.createIssueError({
            errorHtml: error,
            useLegacyDecorator: JIRA.useLegacyDecorator()
        }));
    }
});

/**
 * A View that renders the full version of the create issue form. The full version does NOT allow fields to be
 * configured.
 *
 * @class UnconfigurableCreateIssueForm
 */
JIRA.QuickForm.UnconfigurableCreateIssueForm = JIRA.QuickForm.AbstractUnconfigurableForm.extend({

    /**
     * @constructor
     * @param options
     * ... {JIRA.QuickForm.Model} model that gets fields and sets user preferences
     * ... {Array<String>} globalEventNamespaces - Events will be triggered on these namespaces
     * e.g CreateForm.switchedToConfigurableForm. CreateForm being the specified global namespace.
     * ... {String, Number} issue id
     */
    init: function (options) {
        this.globalEventNamespaces = options.globalEventNamespaces || [];
        this.model = options.model;
        this.issueCreatedCallback = options.issueCreated;

        // Form values will be serialized and POSTed to this url
        this.action = contextPath + "/secure/QuickCreateIssue.jspa?decorator=none";
        // The container element. All html will be injected into this guy.
        this.$element = jQuery("<div />");
    },

    /**
     * Directs the browser to the newly created issue
     *
     * @param data
     * ... {String} issueKey
     */
    handleSubmitSuccess: function (data) {
        this.triggerEvent("QuickForm.submitted");
        this.triggerEvent("QuickForm.issueCreated", [data, this]);
        if (this.issueCreatedCallback) {
            this.issueCreatedCallback(data);
        }
    },

    /**
     * Sets issue type and updates form (by rerendering it) with the correct fields
     *
     * @param {String} issueType
     */
    setIssueType: function (issueType) {
        var instance = this;
        this.invalidateFieldsCache(); // invalidate fields cache
        this.model.refresh(this.serialize()).done(function () {
            instance.render();
        });
    },

    /**
     * Sets projectId and updates form (by rerendering it) with the correct fields
     *
     * @param projectId
     */
    setProjectId: function (projectId) {

        var instance = this;
        this.invalidateFieldsCache(); // invalidate fields cache
        this.model.refresh(this.serialize()).done(function () {
            instance.render();
        });
    },

    /**
     * Gets project <select>
     *
     * @return {jQuery}
     */
    getProjectField: function () {
        return this.$element.find(".issue-setup-fields #project");
    },

    /**
     * Gets issueType <select>
     */
    getIssueTypeField: function () {
        return this.$element.find(".issue-setup-fields #issuetype");
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
            instance.model.getIssueSetupFields().done(function (issueSetupFields) {

                var html = JIRA.Templates.Issue.createIssueForm({
                    issueSetupFields: issueSetupFields,
                    fieldsHtml: fieldsHtml,
                    atlToken: atl_token(),
                    useLegacyDecorator: JIRA.useLegacyDecorator()
                });

                instance.$element.html(html);

                instance.getProjectField().change(function () {
                    instance.setProjectId(this.value);
                });

                instance.getIssueTypeField().change(function () {
                    instance.setIssueType(this.value);
                });

                deferred.resolveWith(instance, [instance.$element]);
            });

        });

        return deferred.promise();
    }
});

/**
 * A View that renders a configurable version of the create issue form. This version allows you to reorder fields and
 * add and remove fields.
 *
 * @class ConfigurableCreateIssueForm
 */
JIRA.QuickForm.ConfigurableCreateIssueForm = JIRA.QuickForm.AbstractConfigurableForm.extend({

    /**
     * @constructor
     * @param options
     * ... {JIRA.QuickForm.Model} model that gets fields and sets user preferences
     * ... {String, Number} issue id
     * ... {Array<String>} globalEventNamespaces - Events will be triggered on these namespaces
     * e.g CreateForm.switchedToConfigurableForm. CreateForm being the specified global namespace.
     */
    init: function (options) {
        this.model = options.model;
        this.globalEventNamespaces = options.globalEventNamespaces || [];
        this.issueId = options.issueId;
        this.issueCreatedCallback = options.issueCreated;

        // Form values will be serialized and POSTed to this url
        this.action = contextPath + "/secure/QuickCreateIssue.jspa?decorator=none";
        // The container element. All html will be injected into this guy.
        this.$element = jQuery("<div />").addClass("qf-form");
    },

    /**
     * If the create another checkbox is checked will notify the user of newly created issue and clear the form
     * (Except for pinned values). Otherwise, directs the browser to the newly created issue.
     *
     * @param data
     * ... {String} issueKey
     */
    handleSubmitSuccess: function (data) {

        var instance = this;

        this.triggerEvent("QuickForm.submitted");
        this.triggerEvent("QuickForm.issueCreated", [data, this]);
        
        if (this.isInMultipleMode()) {

            this.render().done(function () {

                var message = JIRA.Templates.Issue.issueCreatedMessage({
                        issueKey: data.issueKey,
                        url: contextPath + "/browse/" + data.issueKey
                    });


                JIRA.applySuccessMessageToForm(instance.getForm(), message);
                instance.getCreateAnotherCheckbox().attr("checked", "checked");
            });

        } else if (this.issueCreatedCallback) {
            this.issueCreatedCallback(data);
        }
    },

    /**
     * Gets checkbox that when checked allows multiple issue creating
     *
     * @return jQuery
     */
    getCreateAnotherCheckbox: function () {
        return this.$element.find("#qf-create-another");
    },

    /**
     * Determines if we are creating more entries.
     *
     * @return Boolean
     */
    isInMultipleMode: function () {
        return this.getCreateAnotherCheckbox().is(":checked");
    },

    /**
     * Gets project <select>
     *
     * @return {jQuery}
     */
    getProjectField: function () {
        return this.$element.find(".issue-setup-fields #project");
    },

    /**
     * Gets issueType <select>
     *
     * @return {jQuery}
     */
    getIssueTypeField: function () {
        return this.$element.find(".issue-setup-fields #issuetype");
    },

    /**
     * Sets issue type and updates form (by rerendering it) with the correct fields
     *
     * @param {String} issueType
     */
    setIssueType: function (issueType) {
        var instance = this;
        this.invalidateFieldsCache(); // invalidate fields cache
        this.model.refresh(this.serialize()).done(function () {
            instance.render();
        });
    },

    /**
     * Sets projectId and updates form (by rerendering it) with the correct fields
     *
     * @param projectId
     */
    setProjectId: function (projectId) {
        var instance = this;
        this.invalidateFieldsCache(); // invalidate fields cache
        this.model.refresh(this.serialize()).done(function () {
            instance.render();
        });
    },

    /**
     * Renders create issue specific chrome and furniture before delegating to super class for the rendering of fields
     *
     */
    _render: function () {

        var deferred = jQuery.Deferred(),
            instance = this;

        instance.model.getIssueSetupFields().done(function (issueSetupFields) {

            var html = jQuery(JIRA.Templates.Issue.createIssueForm({
                issueSetupFields: issueSetupFields,
                atlToken: atl_token(),
                isConfigurable: true,
                useLegacyDecorator: JIRA.useLegacyDecorator()
            }));

            instance.$element.html(html); // add form chrome to container element

            instance.getProjectField().change(function () {
                instance.setProjectId(this.value);
            });

            instance.getIssueTypeField().change(function () {
                instance.setIssueType(this.value);
            });


            // render fields
            instance.renderFormContents().done(function () {
                deferred.resolveWith(instance, [instance.$element]);
            });

        });

        return deferred.promise();
    }
});

/**
 * Renders a list of fields that can be added and removed from a create issue form
 *
 * @class EditIssuePicker
 */
JIRA.QuickForm.CreateIssuePicker = JIRA.QuickForm.FieldPicker.extend({

    /**
     * Renders create issue picker with a link at the buttom to switch to "full create".
     *
     * @return jQuery.Promise
     */
    render: function () {

        var instance = this;

        this.$element = jQuery(JIRA.Templates.Issue.quickCreateFieldPicker());
        this.$content = this.$element.find(".qf-picker-content");

        this.$element.find(".qf-unconfigurable").click(function (e) {
            instance.inlineDialog.hide();
            e.preventDefault();
        });

        return this.renderContents();
    }
});

/**
 * Factory to create Create Issue Form
 *
 * @return JIRA.QuickForm.Container
 */
JIRA.QuickForm.createCreateIssueForm = function (opts) {

    opts = opts || {};

    // model that gets fields and sets user preferences
    var model = new JIRA.QuickForm.CreateIssueModel(),
        configurableForm = new JIRA.QuickForm.ConfigurableCreateIssueForm({
            model: model,
            globalEventNamespaces: ["QuickCreate"],
            issueCreated: opts.issueCreated
        }),
        fieldPicker = new JIRA.QuickForm.FieldPicker(configurableForm),
        unconfigurableForm = new JIRA.QuickForm.UnconfigurableCreateIssueForm({
            model: model,
            globalEventNamespaces: ["QuickCreate"],
            issueCreated: opts.issueCreated
        }),
        options = {
            globalEventNamespaces: ["QuickCreate"],
            model: model,
            errorHandler: new JIRA.QuickForm.CreateIssueError(),
            configurableForm: configurableForm,
            unconfigurableForm: unconfigurableForm,
            fieldPicker: fieldPicker
        };

    return new JIRA.QuickForm.Container(options);
};

jQuery(function () {

    // Only init for 5.0+ instances
    if (!JIRA.Version.isGreaterThanOrEqualTo("5.0")) {
        return;
    }

    jQuery("#create_link").click(function (e) {
        e.stopPropagation();
    });

    // Creates create issue dialog
    JIRA.QuickForm.createCreateIssueForm({
        issueCreated: function (data) {
            AJS.reloadViaWindowLocation(contextPath + "/browse/" + data.issueKey);
        }
    }).asDialog({
        trigger: jQuery("#create_link"),
        id: "create-issue-dialog"
    });

    // Ensure Assign to me link works and tabs are initiated
    jQuery(document).bind("dialogContentReady", function (e, dialog) {
        JIRA.Issue.wireAssignToMeLink(dialog.get$popupContent());
        AJS.tabs.setup();
    });



});