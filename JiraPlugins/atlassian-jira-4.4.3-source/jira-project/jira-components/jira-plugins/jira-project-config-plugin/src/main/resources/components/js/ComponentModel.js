jQuery.namespace("JIRA.Admin.Component.ComponentModel");

/**
 * Model for a Project component
 *
 * @class ComponentModel
 * @namespace JIRA.Admin.Component
 */
JIRA.Admin.Component.ComponentModel = JIRA.RestfulTable.EntryModel.extend({

    // Rest resources
    RELATED_ISSUES_PATH: "/relatedIssueCounts",

    /**
     * Some additional mapping of leadUserName
     *
     * @param {Object} attributes
     * @return {Object}
     */
    changedAttributes: function (attributes) {

        var currentLead = this.get("lead"),
            leadUserName = attributes.leadUserName,
            changed = JIRA.RestfulTable.EntryModel.prototype.changedAttributes.call(this, attributes);

        if (currentLead && leadUserName === "") {
            changed = changed || {};
            changed.leadUserName = leadUserName;
        }

        return changed;
    },

    /**
     * If the assignee type is invalid returns a message indicating why.
     *
     * @return {string|undefined}
     */
    getAssigneeInvalidMsg: function () {

        if (this.get("isAssigneeTypeValid")) {
            // No error message is returned if assignee is valid.
            return undefined;
        }

        var msg;
        var assignee = this.get("assignee") || {};

        switch (this.get("assigneeType")) {

            case "UNASSIGNED":
                msg = AJS.I18n.getText("admin.project.assigneeType.unassigned.invalid");
                break;

            case "PROJECT_DEFAULT":
                msg = AJS.I18n.getText("admin.project.assigneeType.project.default.not.assignable");
                break;

            case "PROJECT_LEAD":
                msg = (assignee.active)
                    ? AJS.I18n.getText("admin.project.assigneeType.project.lead.not.assignable")
                    : AJS.I18n.getText("admin.project.assigneeType.project.lead.not.active");
                break;

            case "COMPONENT_LEAD":
                msg = (assignee.active)
                    ? AJS.I18n.getText("admin.project.assigneeType.component.lead.not.assignable")
                    : AJS.I18n.getText("admin.project.assigneeType.component.lead.not.active");
                break;
        }

        if (msg) {
            return AJS.format(msg, assignee.displayName || assignee.name || "");
        }
    },


    /**
     * Gets count for issues with either affects version or fix version containing this version
     *
     * @param options
     * ... {function} success - Server success callback
     * ... {function} error - Server error callback
     * @return JIRA.Admin.Component.ComponentModel
     */
    getRelatedIssueCount: function (options) {

        var instance = this;

        options = options || {};

        JIRA.SmartAjax.makeRequest({
            url: this.url() + this.RELATED_ISSUES_PATH,
            complete: function (xhr, status, smartAjaxResponse) {
                if (smartAjaxResponse.successful) {
                    options.success.call(instance, smartAjaxResponse.data.issueCount);
                } else {
                    instance._serverErrorHandler(smartAjaxResponse)
                }
            }
        });

        return this;
    },

/**
     * Gets JSON representation of available components to migrate issues of this component into.
     *
     * @return {Array}
     */
    getSwapComponentsJSON: function () {

        var instance = this,
            swapComponents = [];

        this.collection.each(function (model) {
            if (model !== instance) {
                swapComponents.push(model.toJSON());
            }
        });

        return swapComponents;
    }
});