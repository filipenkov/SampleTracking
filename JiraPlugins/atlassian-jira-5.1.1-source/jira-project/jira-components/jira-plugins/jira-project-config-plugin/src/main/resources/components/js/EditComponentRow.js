jQuery.namespace("JIRA.Admin.Component.EditComponentRow");

/**
 * View for create/edit component
 */
JIRA.Admin.Component.EditComponentRow = AJS.RestfulTable.EditRow.extend({

    init: function () {

        // call super
        AJS.RestfulTable.EditRow.prototype.initialize.apply(this, arguments);

        this.bind(this._events.RENDER,  function () {
            this.$el.addClass("project-config-versions-add-fields");
        });
    },

    renderIcon: function() {
        return JIRA.Templates.Component.icon();
    },

    renderName: function (self, all) {
        all.project = this.model.getProject();
        return JIRA.Templates.Component.editComponentName(all);
    },

    renderRealAssignee: function (self, all) {
        all.isLeadPickerDisabled = this.model.isLeadPickerDisabled();
        return JIRA.Templates.Component.editComponentLead(all);
    },

    renderRealAssigneeType: function (self, all) {
        all.isDefaultAssigneeProjectLead = this.model.isDefaultAssigneeProjectLead();
        all.projectLeadAssignee = this.model.getProjectLeadAssignee();
        return JIRA.Templates.Component.editDefaultAssignee(all);
    },

    submit: function () {

        var $leadError = this.$(".project-config-component-lead .error");

        if ($leadError.length === 0) {
            AJS.RestfulTable.EditRow.prototype.submit.apply(this, arguments);
        } else {
            this.focus();
        }
    }
});
