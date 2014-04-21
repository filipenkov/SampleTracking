JIRA.PeopleModel = AJS.RestfulTable.EntryModel.extend({

    formatGroupSuggestions: function (data) {
        var ret = [],
            selectedVals = this.model.getSelectedValues();

        AJS.$(data).each(function(i, suggestions) {

            var selectedInList = 0;
            var groupDescriptor = new AJS.GroupDescriptor({
                weight: i // order or groups in suggestions dropdown
            });
            AJS.$(suggestions.groups).each(function(){
                if (AJS.$.inArray(this.name, selectedVals) === -1) {
                    groupDescriptor.addItem(new AJS.ItemDescriptor({
                        value: this.name, // value of item added to select
                        label: this.name, // title of lozenge
                        html: this.html
                    }));
                } else {
                    ++selectedInList;
                }
            });

            if (suggestions.groups.length !== suggestions.total) {
                groupDescriptor.properties.label = AJS.I18n.getText('jira.ajax.autocomplete.group.more.results',
                        groupDescriptor.items().length, suggestions.total - selectedInList);
            }
            ret.push(groupDescriptor);
        });

        return ret;
    },

    formatUserSuggestions: function (data) {
        var ret = [],
            selectedVals = this.model.getSelectedValues();

        AJS.$(data).each(function(i, suggestions) {

            var selectedInList = 0;
            var groupDescriptor = new AJS.GroupDescriptor({
                weight: i // order or groups in suggestions dropdown
            });

            AJS.$(suggestions.users).each(function(){
                if (AJS.$.inArray(this.name, selectedVals) === -1) {
                    groupDescriptor.addItem(new AJS.ItemDescriptor({
                        value: this.name, // value of item added to select
                        label: this.displayName, // title of lozenge
                        html: this.html,
                        icon: this.avatarUrl,
                        allowDuplicate: false
                    }));
                } else {
                    ++selectedInList;
                }
            });

            if (suggestions.users.length !== suggestions.total) {
                groupDescriptor.properties.label = AJS.I18n.getText('jira.ajax.autocomplete.user.more.results',
                        groupDescriptor.items().length, suggestions.total - selectedInList);
            }
            ret.push(groupDescriptor);

        });

        return ret;
    },

    /**
     * Some special mapping of data sent to server. For the ProjectRoleResource we need to send all the changed categorisedActors.
     * Not just the ones that have changed
     *
     * @param {Object} params - serialized form data
     * @return data sent to server
     */
    mapSubmitParams: function (params) {

        var current = this.toJSON(),
            currentUserRole = current.categorisedActors["atlassian-user-role-actor"],
            currentGroupRole = current.categorisedActors["atlassian-group-role-actor"],
            data = {categorisedActors: {}};

        if (params.users) {
            if (currentUserRole && !_.isEqual(currentUserRole, params.users)) {
                data.categorisedActors["atlassian-user-role-actor"] = params.users;
            } else if (!currentUserRole && params.users.length > 0) {
                data.categorisedActors["atlassian-user-role-actor"] = params.users;
            }
        }

        if (params.groups) {
            if (currentGroupRole && !_.isEqual(currentGroupRole, params.groups)) {
                data.categorisedActors["atlassian-group-role-actor"] = params.groups;
            } else if (!currentGroupRole && params.groups.length > 0) {
                data.categorisedActors["atlassian-group-role-actor"] = params.groups;
            }
        }

        if (data.categorisedActors["atlassian-group-role-actor"]) {
            data.categorisedActors["atlassian-user-role-actor"] = params.users;
        }

        if (data.categorisedActors["atlassian-user-role-actor"]) {
            data.categorisedActors["atlassian-group-role-actor"] = params.groups;
        }


        if (!_.isEmpty(data.categorisedActors)) {
            return data;
        }
    }

});
