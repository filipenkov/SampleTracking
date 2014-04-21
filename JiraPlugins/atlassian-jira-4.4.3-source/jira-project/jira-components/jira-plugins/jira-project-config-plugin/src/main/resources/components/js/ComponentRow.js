jQuery.namespace("JIRA.Admin.Component.ComponentRow");

/**
 * Handles rendering, interaction and updating (delegating to model) of a single version
 */
JIRA.Admin.Component.ComponentRow = JIRA.RestfulTable.Row.extend({

    initialize: function () {

        // call super
        JIRA.RestfulTable.Row.prototype.initialize.apply(this, arguments);

        // crap work around to handle backbone not extending events
        // (https://github.com/documentcloud/backbone/issues/244)
        this.events["click .project-config-component-delete"] = "_delete";
        this.delegateEvents();
    },


    _delete: function (e) {
        this.trigger("focus");
        this._openDialog(JIRA.Admin.Component.DeleteForm, "component-" + this.model.get("id") + "-delete-dialog");
        e.preventDefault();
    },

    /**
     * Resets and renders version row in table. This should be called whenever the model changes.
     */
    render: function () {

        var instance = this,
            id = this.model.get("id"),
            data = this.model.toJSON();

            data.assigneeInvalidMsg = instance.model.getAssigneeInvalidMsg();

            instance.$el.attr("className", "project-config-component")
                    .attr("id", "component-" +  id + "-row")
                    .attr("data-id", id)
                    .html(JIRA.Templates.Component.componentRow({
                        component: data
                    }));

            JIRA.userhover(this.el); // Add user hover for component lead

        return this;
    }
});

