jQuery.namespace("JIRA.Admin.People.RoleRow");

JIRA.Admin.People.RoleRow = AJS.RestfulTable.Row.extend({


    render: function () {
        var data = this.model.toJSON();

        this.$el.html(JIRA.Templates.People.roleRow({
            role: data
        }));

        return this;
    }

});
