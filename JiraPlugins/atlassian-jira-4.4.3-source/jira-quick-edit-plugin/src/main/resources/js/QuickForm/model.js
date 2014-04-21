/**
 * Retrieval mechanism for fields.
 *
 * Note: the fieldsResource should return an object of JSON entities that conform to:
 * fields: [{id: <string>, editHtml: <string>, label: <string>, required <boolean>}, ...]
 * userFields: [<string>, ...]
 *
 * @class JIRA.QuickForm.Model
 */
JIRA.QuickForm.Model = Class.extend({

    /**
     * @constructor
     * @param options
     * ... {String} userFieldsResource - url of server resource that will be POSTed to with the ids of the fields to be shown
     * ... {String} fieldsResource - url of server resource that will be user to request meta data about all the available fields
     */
    init: function (options) {
        this.userFieldsResource = options.userFieldsResource;
        this.fieldsResource = options.fieldsResource;
        this.retainedFields = [];
        this._hasRetainFeature = false;
        this._hasVisibilityFeature = true;
    },

    /**
     * Gets field resource
     */
    getFieldsResource: function () {
        return this.fieldsResource;
    },

    /**
     * Gets active fields, by default this is just the user defined fields
     */
    getActiveFieldIds: function () {
        return this.getUserFields();
    },

    /**
     * Specifies field should retain its value for next time
     *
     * @param {String} id - field id
     */
    addFieldToRetainValue: function (id) {
        if (this._hasRetainFeature) {
            this.removeFieldToRetainValue(id); // avoid duplicates
            this.retainedFields.push(id);
        } else {
            throw new Error("getFieldsWithRetainedValues: cannot be called. Must enable retain feature first by " +
                "specifiying [_hasRetainFeature=true]")
        }
    },

    clearRetainedFields: function () {
        this.retainedFields = [];
    },

    /**
     * Specifies field should NOT retain its value for next time
     *
     * @param {String} id - field id
     */
    removeFieldToRetainValue: function (id) {
        if (this._hasRetainFeature) {
            var inArray = jQuery.inArray(id, this.retainedFields);
            if (inArray != -1) {
                this.retainedFields.splice(inArray, 1);
            }
        } else {
            throw new Error("getFieldsWithRetainedValues: cannot be called. Must enable retain feature first by " +
                "specifiying [_hasRetainFeature=true]")
        }
    },

    /**
     * Gets array of field of which values should be retained for next time
     *
     * @return {Array}
     */
    getFieldsWithRetainedValues: function () {
        if (this._hasRetainFeature) {
            return this.retainedFields;
        } else {
            throw new Error("getFieldsWithRetainedValues: cannot be called. Must enable retain feature first by " +
                "specifiying [_hasRetainFeature=true]")
        }
    },

    /**
     * Should we retain the value for this field
     *
     * @param field - field descriptor
     */
    hasRetainedValue: function (field) {
        if (this._hasRetainFeature) {
            return jQuery.inArray(field.id, this.retainedFields) !== -1;
        } else {
            throw new Error("HasRetainedValue: cannot be called. Must enable retain feature first by specifiying [_hasRetainFeature=true]")
        }
    },

    /**
     * Go back to the server and retrieve all the available fields
     */
    refresh: function (values) {

        var instance = this,
            deferred = jQuery.Deferred(),
            data;

        if (values) {
            data = values + "&retainValues=true";
        }

        JIRA.SmartAjax.makeRequest({
            url: this.getFieldsResource(),
            type: "POST", // has to be a post, otherwise we go over the query param character limit
            data: data,
            success: function (data) {
                instance.fields = data.fields;
                instance.sortedTabs = data.sortedTabs;
                instance.userPreferences = data.userPreferences;
            },
            complete: function (xhr, textStatus, smartAjaxResult) {

                if (smartAjaxResult.successful) {
                    deferred.resolveWith(instance, arguments);
                } else {
                    instance.triggerEvent("serverError", [smartAjaxResult]);
                    deferred.rejectWith(instance, [smartAjaxResult]);
                }
            }
        });

        return deferred.promise();
    },

    /**
     * Manually set fields
     *
     * @param {Array} fields
     */
    setFields: function (fields) {
        this.fields = fields;
    },

    /**
     * Gets fields that can be configured - Added and removed from form
     *
     * @return jQuery.promise
     */
    getConfigurableFields: function () {
        return this.getFields();
    },

    /**
     * Gets all fields, see note on class description for structure.
     *
     * Note: this will throw an error if refesh hasn't been called first
     *
     * @return {Array} - Array of JSON describing field
     */
    getFields: function () {

        var instance = this,
            deferred = jQuery.Deferred();

        if (!this.fields) {
            this.refresh().done(function () {
                deferred.resolve(instance.fields);
            });
        } else {
            deferred.resolve(instance.fields);
        }

        return deferred.promise();
    },

    _mungeTabs: function (fields) {

        var tabs = [];

        jQuery.each(fields, function (i, field) {
            if (field.tab) {
                if (!tabs[field.tab.position]) {
                    tabs[field.tab.position] = field.tab;
                }

                if (field.tab.position === 0) {
                    tabs[field.tab.position].isFirst = true;
                }

                if (!tabs[field.tab.position].fields) {
                    tabs[field.tab.position].fields = []
                }

                tabs[field.tab.position].fields.push(field);

            }
        });

        return tabs;
    },

    /**
     * Gets tabs
     *
     * [{label: <string>, id: <string>, fields: [{id: <string>, editHtml: <string>, label: <string>, required <boolean>}]]
     *
     * @return jQuery.Promise
     */
    getTabs: function () {

        var instance = this,
            deferred = jQuery.Deferred();



        this.getFields().done(function (fields) {


            var tabs =instance._mungeTabs(fields);
            deferred.resolve(tabs);
        });

        return deferred.promise();
    },

    /**
     * Gets tabs sorted in alphabetical order
     *
     * @return jQuery.promise
     */
    getSortedTabs: function () {

        var deferred = jQuery.Deferred();

        if (!this.sortedTabs) {
            this.refresh().done(function () {
                deferred.resolve(this.sortedTabs);
            });
        } else {
            deferred.resolve(this.sortedTabs);
        }

        return deferred.promise();
    },

    /**
     * Gets all user field ids <array>
     *
     * Note: this will throw an error if refesh hasn't been called first
     *
     * @return jQuery.Promise
     */
    getUserFields: function () {

        var instance = this,
            deferred = jQuery.Deferred();

        if (!this.userPreferences) {
            this.refresh().done(function () {
                deferred.resolve(instance.userPreferences.fields);
            });
        } else {
            deferred.resolve(instance.userPreferences.fields);
        }

        return deferred.promise();
    },

    /**
     * Sends updated user fields back to server for persistance
     *
     * @param {Array<string>}userFields
     * @return jQuery.Promise
     */
    setUserFields: function (userFields) {
        var data = {};
        data.fields = userFields;
        return this.updateUserPrefs(data);
    },

    /**
     * Should the form allow the user to "retain" a fields value for next time
     *
     * @return Boolean
     */
    hasRetainFeature: function () {
        return !!this._hasRetainFeature;
    },

    /**
     * Should the form allow the user to add and remove fields
     *
     * @return Boolean
     */
    hasVisibilityFeature: function () {
        return !!this._hasVisibilityFeature;
    },

    /**
     * Gets a boolean value if the quick form or full form should be shown
     *
     * @return jQuery.Promise
     */
    getUseConfigurableForm: function () {

        var instance = this,
            deferred = jQuery.Deferred();

        if (!this.userPreferences) {
            this.refresh().done(function () {
                deferred.resolve(instance.userPreferences.useQuickForm);
            });
        } else {
            deferred.resolve(instance.userPreferences.useQuickForm)
        }

        return deferred.promise();
    },

    /**
     * Sets wheter to use Configurable or Unconfigurable forms
     *
     * @param {Boolean} use
     * @return jQuery.Promise
     */
    setUseConfigurableForm: function (use) {
        var data = {};
        data.useQuickForm = use;
        return this.updateUserPrefs(data);
    },

    /**
     * Updates user prefs in bulk
     *
     * @param data
     * ... {Array<String>} fields
     * ... {Boolean} useQuickForm
     */
    updateUserPrefs: function (data) {

        var instance = this;

        if (!data.fields) {
            data.fields = this.userPreferences.fields;
        }

        if (typeof data.showWelcomeScreen === "undefined") {
            data.showWelcomeScreen = this.userPreferences.showWelcomeScreen;
        }

        if (typeof data.useQuickForm === "undefined") {
            data.useQuickForm = this.userPreferences.useQuickForm;
        }

        return JIRA.SmartAjax.makeRequest({
            url: this.userFieldsResource,
            type: "POST",
            data: JSON.stringify(data),
            dataType: "json",
            contentType: "application/json",
            complete: function (xhr, textStatus, smartAjaxResult) {
                if (smartAjaxResult.successful) {
                    instance.userPreferences = data;
                } else {
                    instance.triggerEvent("serverError", [smartAjaxResult]);
                }
            }
        })
    }
});



