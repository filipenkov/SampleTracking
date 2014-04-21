jQuery.namespace("JIRA.Admin.People.PeopleRow");

JIRA.Admin.People.PeopleRow = AJS.RestfulTable.Row.extend({

    initialize: function () {

        AJS.RestfulTable.Row.prototype.initialize.apply(this, arguments);

        this.bind(this._events.RENDER, function () {
            var id = this.model.get("id");
            this.$el.attr("id", "people-" + id + "-row").attr("data-id", id);
        });
    },

    formatModelJSON: function (data) {
        var formattedData = {groups: [], users: []};
        jQuery.each(data.actors, function(index, elt){
            if(elt.type === "atlassian-user-role-actor"){
                formattedData.users.push(elt);
            } else if(elt.type === "atlassian-group-role-actor"){
                formattedData.groups.push(elt);
            }
        });
        return formattedData;
    },

    renderUsers: function (self, all) {
        var data = this.formatModelJSON(all);
        return JIRA.Templates.People.users({
            role: data
        });
    },

    renderOperations: function () {
        return "";
    },

    renderGroups: function (self, all) {
        var data = this.formatModelJSON(all);
        return JIRA.Templates.People.groups({
            role: data
        });
    }
});
