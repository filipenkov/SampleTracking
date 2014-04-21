jQuery.namespace("JIRA.Admin.People.PeopleRow");
 
JIRA.Admin.People.PeopleRow = JIRA.RestfulTable.Row.extend({

    render: function () {
        var data = this.model.toJSON(),
            users = [],
            groups = [],
            id = this.model.get("id"),
            $el = this.$el;

        // massage the actors
        $el.attr("id", "people-" + id + "-row").attr("data-id", id);

        jQuery.each(data.actors, function(index, elt){
            if(elt.type === "atlassian-user-role-actor"){
                users.push(elt);
            } else if(elt.type === "atlassian-group-role-actor"){
                groups.push(elt);
            }
        });

        if(users.length){
            data.users = users;
        }
        if(groups.length){
            data.groups = groups;
        }

        delete data.actors;

        $el.html(JIRA.Templates.People.peopleRow({
            role: data
        }));
        return this;
    }
 
});
