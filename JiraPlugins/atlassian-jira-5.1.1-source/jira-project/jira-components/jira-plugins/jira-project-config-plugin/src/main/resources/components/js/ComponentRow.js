jQuery.namespace("JIRA.Admin.Component.ComponentRow");

/**
 * Handles rendering, interaction and updating (delegating to model) of a single version
 */
JIRA.Admin.Component.ComponentRow = AJS.RestfulTable.Row.extend({

    initialize: function () {

        // call superx
        AJS.RestfulTable.Row.prototype.initialize.apply(this, arguments);

        // crap work around to handle backbone not extending events
        // (https://github.com/documentcloud/backbone/issues/244)
        this.events["click .project-config-component-delete"] = "destroy";
        this.delegateEvents();


        this.bind(this._events.RENDER, function () {

            var id = this.model.get("id");

            this.$el.addClass("project-config-component")
                    .attr("id", "component-" +  id + "-row")
                    .attr("data-id", id);

            JIRA.userhover(this.el); // Add user hover for component lead
        });
    },

    renderIcon: function () {
        return JIRA.Templates.Component.icon();
    },

    renderRealAssigneeType: function (self, all) {
        return JIRA.Templates.Component.defaultAssignee({
            component: all
        });
    },

    renderRealAssignee: function (self, all) {
        if (all.lead || all.leadUserName) {
            all.assigneeInvalidMsg = this.model.getAssigneeInvalidMsg();
            return JIRA.Templates.Component.componentLead({
                component: all
            });
        }
    },


    destroy: function (e) {
        this.trigger("focus");
        AJS.openDialogForRow(JIRA.Admin.Component.DeleteForm, this, "component-" + this.model.get("id") + "-delete-dialog");
        e.preventDefault();
    }
    

});

