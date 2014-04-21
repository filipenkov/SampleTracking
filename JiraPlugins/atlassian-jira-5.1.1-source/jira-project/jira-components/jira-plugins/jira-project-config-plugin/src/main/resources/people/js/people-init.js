jQuery(function () {
    
    var roleTable = jQuery("#project-config-people-table");
    
    function getResourceURL () {
        return JIRA.REST_BASE_URL + "/project/" + JIRA.ProjectConfig.getKey() +"/role";
    }

    function getRole (callback) {

        JIRA.SmartAjax.makeRequest({
            url: getResourceURL(),
            complete: function (xhr, status, response) {
                if (response.successful) {
                    var roles = [];
                    for (var roleName in response.data) {
                        JIRA.SmartAjax.makeRequest({
                            async : false,
                            url : response.data[roleName],
                            complete: function (xhr, status, response) {
                                if (response.successful) {
                                    var categorisedActors = {};
                                    jQuery.each(response.data.actors, function(index, elt){
                                        if(!(elt.type in categorisedActors)){
                                            categorisedActors[elt.type] = [];
                                        }
                                        categorisedActors[elt.type].push(elt.name);
                                    });
                                    response.data.categorisedActors = categorisedActors;
                                    roles.push(response.data);
                                } else {
                                    roleTable.trigger("serverError",
                                            [JIRA.SmartAjax.buildSimpleErrorContent(response)]);
                                }
                            }
                        })
                    }
                    roles.sort(function(a, b) {
                        var aName = a.name.toLowerCase();
                        var bName = b.name.toLowerCase();
                        return ((aName < bName) ? -1 : ((aName > bName) ? 1 : 0));
                    });

                    callback(roles);

                } else {
                    roleTable.trigger("serverError",
                            [JIRA.SmartAjax.buildSimpleErrorContent(response)]);
                }
            }
        });
    }


    JIRA.Admin.RoleTable = new AJS.RestfulTable({
        editable: true,
        createDisabled: true,
        model: JIRA.PeopleModel,
        el: roleTable,
        resources: {
            all: getRole,
            self: getResourceURL()
        },
        columns: [
            {
                id: "name",
                styleClass: "project-config-role-name",
                header: AJS.I18n.getText("common.words.project.roles"),
                editable: false
            },
            {
                id: "users",
                styleClass: "project-config-role-users",
                header: AJS.I18n.getText("admin.common.words.users"),
                emptyText: AJS.I18n.getText("user.picker.add.users"),
                fieldName: "project-config-people-users-select-textarea"
            },
            {
                id: "groups",
                styleClass: "project-config-role-groups",
                header: AJS.I18n.getText("common.words.groups"),
                emptyText: AJS.I18n.getText('admin.usersandgroups.add.group'),
                fieldName: "project-config-people-groups-select-textarea"
            }
        ],
        noEntriesMsg: AJS.I18n.getText("admin.project.role.none"),
        views: {
            editRow: JIRA.Admin.People.EditPeopleRow,
            row: JIRA.Admin.People.PeopleRow
        }
    });

    roleTable.closest("form").addClass("ajs-dirty-warning-exempt"); // Stop dirty form warnings from firing on table

});
