jQuery.namespace("JIRA.Admin.Component.EditComponentRow");

/**
 * View for create/edit component
 */
JIRA.Admin.Component.EditComponentRow = JIRA.RestfulTable.EditRow.extend({

    /**
     * Handles all the rendering of the create version row. This includes handling validation errors if there is any
     *
     * @param {Object} renderData
     * ... {Object} vales - Values of fields
     *
     */
    render: function (data) {


        data.project = jQuery("meta[name=projectKey]").attr("content");
        data.isDefaultAssigneeProjectLead = AJS.params.isDefaultAssigneeProjectLead;
        data.projectLeadAssignee = AJS.params.projectLeadAssignee;
        data.isLeadPickerDisabled = AJS.params.leadPickerDisabled;

        this.el.className = "project-config-versions-add-fields";

        this.$el.html(JIRA.Templates.Component.editComponentRow(data));

        // autocomplete 'lead' field is added via the contentRefresh event. see initSingleUserPicers.js

        return this;
    },

    submit: function () {

        var $leadError = this.$(".project-config-component-lead .error");

        if ($leadError.length === 0) {
            JIRA.RestfulTable.EditRow.prototype.submit.apply(this, arguments);
        } else {
            this.focus();
        }
    }

});
