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
                    for (roleName in response.data) {
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

    getRole(function (roles) {

        JIRA.Admin.RoleTable = new JIRA.RestfulTable({
            editable: true,
            createDisabled: true,
            el: roleTable,
            url: getResourceURL(),
            entries: roles,
            noEntriesMsg: AJS.I18n.getText("admin.project.role.none"), // AJS.params.noVersions,
            views: {
                editRow: JIRA.Admin.People.EditPeopleRow,
                row: JIRA.Admin.People.PeopleRow
            }
        });

        jQuery(".jira-restfultable-init").remove();
    })
    
});
