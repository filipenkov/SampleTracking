(function ($) {

    JIRA.UserPickerUtil = {

        formatSuggestions: function (data) {

            var ret = [],
                selectedVals = this.model.getSelectedValues();

            $(data).each(function(i, suggestions) {

                var selectedInList = 0;
                var groupDescriptor = new AJS.GroupDescriptor({
                    weight: i, // order or groups in suggestions dropdown
                    label: suggestions.footer
                });

                $(suggestions.users).each(function(){
                    if ($.inArray(this.name, selectedVals) === -1) {
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

                ret.push(groupDescriptor);

            });

            return ret;
        }

    };

})(AJS.$);

