/**
 * A special Model for quick create. Has special handling for setup fields, project an issue type
 *
 * @class CreateIssueModel
 */
JIRA.Forms.CreateIssueModel = JIRA.Forms.Model.extend({

    /**
     * @constructor
     * @param {object} options
     * ... {String} issueType - The initially selected issue type
     * ... {String} projectId - The initially selected project id
     */
    init: function (options) {

        options = options || {};

        this.userFieldsResource = contextPath + "/rest/quickedit/1.0/userpreferences/create"
        this._hasRetainFeature = true;
        this._hasVisibilityFeature = true;
        this.retainedFields = [];
        // initIssueType and initProjectId allow you to override the default project and issue type.
        this.initIssueType = options.issueType;
        this.initProjectId = options.projectId;
        this.parentIssueId = options.parentIssueId;
    },

    /**
     * Gets parent issue id, if there is one
     * @return {String}
     */
    getParentIssueId: function () {
        return this.parentIssueId;
    },

    /**
     * Checks to see if we are in multi create mode
     *
     * @return {Boolean}
     */
    isInMultipleMode: function () {
        return this.multipleMode;
    },

    /**
     * Sets if we are creating more than one issue at a time
     *
     * @param {Boolean} state
     */
    setIsMultipleMode: function (state) {
        this.multipleMode = state;
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
     * Gets fields resource url, adding initIssueType & initProjectId to the request if available.
     *
     * initIssueType and initProjectId allow you to override the default project and issue type.
     *
     * @return {String}
     */
    getFieldsResource: function () {
        var fieldsResource =  contextPath + "/secure/QuickCreateIssue!default.jspa?decorator=none";

        if (this.parentIssueId) {
            return fieldsResource + "&parentIssueId=" + this.parentIssueId;
        }

        if (this.initIssueType) {
            fieldsResource = fieldsResource + "&issuetype=" + this.initIssueType;
            delete this.initIssueType;
        }

        if (this.initProjectId) {
            fieldsResource = fieldsResource + "&pid=" + this.initProjectId;
            delete this.initProjectId;
        }

        return fieldsResource;
    },

    /**
     * Sets field values to retain
     * 
     * @param id
     * @param force - always retain
     */
    addFieldToRetainValue: function (id, force) {
        if (force || id !== "summary") {
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
 * A helper class to share between Configurable and Unconfigurable forms
 *
 * @class CreateIssueHelper
 */
JIRA.Forms.CreateIssueHelper = Class.extend({

    init: function (form) {
        this.form = form
    },

    /**
     * If the create another checkbox is checked will notify the user of newly created issue and clear the form
     * (Except for pinned values). Otherwise, directs the browser to the newly created issue.
     *
     * @param data
     * ... {String} issueKey
     */
    handleSubmitSuccess: function (data) {

        function issueCreatedMessage(issueKey, summary, url) {
            var link = '<a href="' + url + '">' + issueKey + ' - ' + AJS.escapeHTML(summary) + '</a>';
            return AJS.I18n.getText('issue.create.created', link);
        }

        var instance = this,
            summary = this.form.$element.find("#summary").val();

        data.summary = summary;

        this.form.triggerEvent("submitted", [data, this]);
        this.form.triggerEvent("issueCreated", [data, this], true);

        if (instance.form.model.isInMultipleMode()) {

            this.form.render().done(function () {
                var message = issueCreatedMessage(data.issueKey, summary, contextPath + "/browse/" + data.issueKey);
                JIRA.applySuccessMessageToForm(instance.form.getForm(), message);
            });

        } else {
            this.form.triggerEvent("sessionComplete");
        }
    },

    /**
     * Gets checkbox that when checked allows multiple issue creating
     *
     * @return jQuery
     */
    getCreateAnotherCheckbox: function () {
        return this.form.$element.find("#qf-create-another");
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
        return this.form.$element.find(".issue-setup-fields #project");
    },

    /**
     * Gets issueType <select>
     *
     * @return {jQuery}
     */
    getIssueTypeField: function () {
        return this.form.$element.find(".issue-setup-fields #issuetype");
    },

    /**
     * Serializes form and specifies which form values to retain. This is different to a normal serialization as
     * it includes any other fields set during the session. JRADEV-9466 e.g you can set a priority then specify a project that hasn't
     * got a priority field then come back to the original project and the priority will still be remembered.
     *
     * @return String
     */
    serializeForToggle: function () {

        if (!this.form.model.hasRetainFeature()) {
            return this.form.getForm().serialize();
        }

        // All fields (and their values) that were ever displayed to the user, before switching to this project/issuetype screen
        var prevActiveFields = this.form.model.prevActiveFields || [];

        // All fields (and their values) on the current project/issuetype screen. Including those not visible.
        var currentSerialization = this.form.getForm().serializeArray();

        // The ids of the fields that are currently visible.
        var currentActiveIds = this.form.getActiveFieldIds();

        // An array of all the active fields that need to be passed onto the next form
        var currentActive = jQuery.grep(currentSerialization, function (param) {
            return jQuery.inArray(param.name, currentActiveIds) !== -1;
        });

        // A filtered list of the previously active fields that are NOT visible on the current screen
        var filteredPreviousActive = jQuery.grep(prevActiveFields, function (param) {
            return jQuery.inArray(param.name, currentActiveIds) === -1;
        });

        // A filtered list of the previously active field ids that are NOT visible on the current screen
        var filteredPrevActiveIds = jQuery.map(filteredPreviousActive, function (param) {
            return param.name;
        });

        // A combined list of the previously active and currently active. Representing what should be retained when we
        // switch issue types
        var combinedActive = jQuery.merge(currentActive, filteredPreviousActive);

        // A combined list of the previously active and currently active. Representing what should be retained when we
        // switch issue types
        var combinedActiveIds = jQuery.merge(currentActiveIds, filteredPrevActiveIds);

        // Update the history of active fields
        this.form.model.prevActiveFields = combinedActive;

        // Prepare the list of paramaters we will post to the server
        var postParams = jQuery.merge([{name: "retainValues", value: true}], combinedActive);

        // Don't forget project and issue type. We always need this
        jQuery.each(currentSerialization, function (i, param) {
            if (param.name === "pid" || param.name === "issuetype") {
                postParams.push(param)
            }
        });

        // Let the server know that we want to retain all the values we send it. So the next screen will have the
        // pre populated values
        jQuery.each(combinedActiveIds, function (i, id) {
            postParams.push({name: "fieldsToRetain", value: id});
        });

        // Finally format the data into a post body.
        return jQuery.param(postParams)

    },

    /**
     * Sets issue type and updates form (by rerendering it) with the correct fields
     *
     * @param {String} issueType
     */
    setIssueType: function () {
        var instance = this,
            serialisedForm = this.serializeForToggle();
        this.form.invalidateFieldsCache(); // invalidate fields cache
        this.form.disable();
        this.form.model.refresh(serialisedForm).done(function () {
            instance.form.render();
            instance.form.enable();
        });
    },


    /**
     * Sets projectId and updates form (by rerendering it) with the correct fields
     *
     * @param projectId
     */
    setProjectId: function () {
        var instance = this,
            serialisedForm = this.serializeForToggle();
        this.form.invalidateFieldsCache(); // invalidate fields cache
        this.form.disable();
        this.form.model.refresh(serialisedForm).done(function () {
            instance.form.render();
            instance.form.enable();
        });
    },

    /**
     * Decorates form with events
     */
    decorate: function () {

        var instance = this;

        this.getProjectField().change(function () {
            instance.setProjectId(this.value);
        });

        this.getIssueTypeField().change(function () {
            instance.setIssueType(this.value);
        });

        this.getCreateAnotherCheckbox().change(function () {
            instance.form.model.setIsMultipleMode(this.checked);
        });
    }

});


/**
 * Renders an error message with create issue furniture
 *
 * @class CreateIssueError
 */
JIRA.Forms.CreateIssueError = JIRA.Forms.Error.extend({
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
JIRA.Forms.UnconfigurableCreateIssueForm = JIRA.Forms.AbstractUnconfigurableForm.extend({

    /**
     * @constructor
     * @param options
     * ... {JIRA.Forms.Model} model that gets fields and sets user preferences
     * ... {Array<String>} globalEventNamespaces - Events will be triggered on these namespaces
     * e.g CreateForm.switchedToConfigurableForm. CreateForm being the specified global namespace.
     * ... {String, Number} issue id
     */
    init: function (options) {
        this._serialization = {};
        this.globalEventNamespaces = options.globalEventNamespaces || [];
        this.model = options.model;
        this.helper = new JIRA.Forms.CreateIssueHelper(this);
        this.title = options.title;
        // Form values will be serialized and POSTed to this url
        this.action = contextPath + "/secure/QuickCreateIssue.jspa?decorator=none";
        // The container element. All html will be injected into this guy.
        this.$element = jQuery("<div class='qf-unconfigurable-form' />");
    },

    /**
     * Removes success message if there is one
     *
     * @param {Object} smartAjax
     * @param {Object} errors
     */
    handleSubmitError: function (smartAjax, errors) {
        this.helper.handleSubmitError(smartAjax, errors);
    },

    /**
     * Directs the browser to the newly created issue
     *
     * @param data
     * ... {String} issueKey
     */
    handleSubmitSuccess: function (data) {
        this.helper.handleSubmitSuccess(data);
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
                        parentIssueId: instance.model.getParentIssueId(),
                        atlToken: instance.model.getAtlToken(),
                        title: instance.title,
                        multipleMode: instance.model.isInMultipleMode(),
                        useLegacyDecorator: JIRA.useLegacyDecorator(),
                        showFieldConfigurationToolBar: !JIRA.Users.LoggedInUser.isAnonymous(),
                        modifierKey: AJS.Navigator.modifierKey()
                    }),
                    result = JIRA.extractScripts(html); // JRADEV-9069 Pull out custom field js to be executed post render
                // Look at JIRA.Forms.Container.render for actual execution
                instance.$element.empty().append(result.html);

                instance.helper.decorate();

                deferred.resolveWith(instance, [instance.$element, result.scripts]);
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
JIRA.Forms.ConfigurableCreateIssueForm = JIRA.Forms.AbstractConfigurableForm.extend({

    /**
     * @constructor
     * @param options
     * ... {JIRA.Forms.Model} model that gets fields and sets user preferences
     * ... {String, Number} issue id
     * ... {Array<String>} globalEventNamespaces - Events will be triggered on these namespaces
     * e.g CreateForm.switchedToConfigurableForm. CreateForm being the specified global namespace.
     */
    init: function (options) {
        this.model = options.model;
        this.helper = new JIRA.Forms.CreateIssueHelper(this);
        this.globalEventNamespaces = options.globalEventNamespaces || [];
        this.issueId = options.issueId;
        this.title = options.title;
        this._serialization = {};

        // Form values will be serialized and POSTed to this url
        this.action = contextPath + "/secure/QuickCreateIssue.jspa?decorator=none";
        // The container element. All html will be injected into this guy.
        this.$element = jQuery("<div />").addClass("qf-form qf-configurable-form");
    },

    /**
     * Delegates submit success to Create Issue helper
     * @param {issueKey: <String>} data
     */
    handleSubmitSuccess: function (data) {
        return this.helper.handleSubmitSuccess(data);
    },

    /**
     * Removes success message if there is one
     *
     * @param {Object} smartAjax
     * @param {Object} errors
     */
    handleSubmitError: function (smartAjax, errors) {
        this.helper.handleSubmitError(smartAjax, errors);
    },

    /**
     * Renders create issue specific chrome and furniture before delegating to super class for the rendering of fields
     *
     */
    _render: function () {

        var deferred = jQuery.Deferred(),
            instance = this;

        instance.model.getIssueSetupFields().done(function (issueSetupFields) {

            var html = JIRA.Templates.Issue.createIssueForm({
                    issueSetupFields: issueSetupFields,
                    atlToken: instance.model.getAtlToken(),
                    isConfigurable: true,
                    title: instance.title,
                    parentIssueId: instance.model.getParentIssueId(),
                    useLegacyDecorator: JIRA.useLegacyDecorator(),
                    multipleMode: instance.model.isInMultipleMode(),
                    showFieldConfigurationToolBar: !JIRA.Users.LoggedInUser.isAnonymous(),
                    modifierKey: AJS.Navigator.modifierKey()
                });

            // add form chrome to container element
            instance.$element.html(html);

            instance.helper.decorate();

            // render fields
            instance.renderFormContents().done(function (el, scripts) {
                deferred.resolveWith(instance, [instance.$element, scripts]);
            });

        });

        return deferred.promise();
    }
});

/**
 * Factory to create Create Issue Form
 *
 * @return JIRA.Forms.Container
 */
JIRA.Forms.createCreateIssueForm = function (options) {

    options = options || {};

    return new JIRA.Forms.Container(function () {

        options.globalEventNamespaces = options.globalEventNamespaces || ["QuickCreateIssue"];

        // model that gets fields and sets user preferences
        var title = AJS.I18n.getText('admin.issue.operations.create'),
            model = new JIRA.Forms.CreateIssueModel({
                pid: options.pid,
                issueType: options.issueType
            }),
            configurableForm = new JIRA.Forms.ConfigurableCreateIssueForm({
                model: model,
                title: title,
                globalEventNamespaces: options.globalEventNamespaces
            }),
            unconfigurableForm = new JIRA.Forms.UnconfigurableCreateIssueForm({
                model: model,
                title: title,
                globalEventNamespaces: options.globalEventNamespaces
            });

        return {
            globalEventNamespaces: options.globalEventNamespaces,
            model: model,
            errorHandler: new JIRA.Forms.CreateIssueError(),
            configurableForm: configurableForm,
            unconfigurableForm: unconfigurableForm
        };
    });
};


/**
 * Factory to create subtask form
 *
 * @return JIRA.Forms.Container
 */
JIRA.Forms.createSubtaskForm = function () {

    function getParentIssueId (options) {
        return options.parentIssueId || JIRA.Issue.getIssueId() || JIRA.IssueNavigator.getSelectedIssueId()
    }

    return function (options) {

        options = options || {};

        return new JIRA.Forms.Container(function () {

            options.globalEventNamespaces = options.globalEventNamespaces || ["QuickCreateSubtask"];

            // model that gets fields and sets user preferences
            var parentIssueId = getParentIssueId(options),
                title = JIRA.Dialog.getIssueActionTitle(AJS.I18n.getText("issue.create.subtask")),
                model = new JIRA.Forms.CreateIssueModel({
                    parentIssueId: parentIssueId,
                    pid: options.pid,
                    issueType: options.issueType
                }),
                configurableForm = new JIRA.Forms.ConfigurableCreateIssueForm({
                    model: model,
                    title: title,
                    globalEventNamespaces: options.globalEventNamespaces
                }),
                unconfigurableForm = new JIRA.Forms.UnconfigurableCreateIssueForm({
                    model: model,
                    title: title,
                    globalEventNamespaces: options.globalEventNamespaces
                });

            return {
                globalEventNamespaces: options.globalEventNamespaces,
                model: model,
                errorHandler: new JIRA.Forms.CreateIssueError(),
                configurableForm: configurableForm,
                unconfigurableForm: unconfigurableForm
            };
        });
    };
}();


jQuery(function () {

    var subtaskTrigger;

    // Only init for 5.0+ instances
    if (!JIRA.Version.isGreaterThanOrEqualTo("5.0")) {
        return;
    }

    JIRA.Forms.createCreateIssueForm().asDialog({
        trigger: ".create-issue",
        id: "create-issue-dialog",
        windowTitle: AJS.I18n.getText('admin.issue.operations.create')
    });

    // Browse project issue types
    jQuery("#create-issue .create-issue-type").each(function () {
        var $trigger = jQuery(this);
        JIRA.Forms.createCreateIssueForm({
            pid: $trigger.attr("data-pid"),
            issueType: $trigger.attr("data-issue-type")
        }).asDialog({
            windowTitle: AJS.I18n.getText('admin.issue.operations.create'),
            trigger: this,
            id: "create-issue-dialog"
        });
    });

    subtaskTrigger = document.getElementById("stqc_show");

    // remove old subtask form
    if (subtaskTrigger) {
        subtaskTrigger.onclick = null;
    }

    JIRA.Forms.createSubtaskForm().asDialog({
        windowTitle: function () {
            return JIRA.Dialog.getIssueActionTitle(AJS.I18n.getText("issue.create.subtask"));
        },
        trigger: ".issueaction-create-subtask",
        id: "create-subtask-dialog"
    });

});