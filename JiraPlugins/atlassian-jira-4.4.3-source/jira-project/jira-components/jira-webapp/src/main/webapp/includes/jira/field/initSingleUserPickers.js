AJS.$(function() {

    function initSingleUserPicker (el) {

        AJS.$(el || document.body).find(".single-user-picker").each(function () {
            var $this = AJS.$(this);

            if (!$this.data("aui-ss")) {

                new AJS.SingleSelect({
                    element: $this,
                    showDropdownButton: false,
                    removeOnUnSelect: true,
                    submitInputVal: true,
                    overlabel: AJS.I18n.getText("user.picker.ajax.short.desc"),
                    errorMessage: AJS.I18n.getText("admin.errors.invalid.user"),
                    ajaxOptions: {
                        url: contextPath + "/rest/api/1.0/users/picker",
                        query: true, // keep going back to the sever for each keystroke
                        minQueryLength: 1,
                        data: {showAvatar: true},
                        formatResponse: function (response) {

                            var ret = [];

                            AJS.$(response).each(function(i, suggestions) {

                                var groupDescriptor = new AJS.GroupDescriptor({
                                    weight: i, // order or groups in suggestions dropdown
                                    id: "user-suggestions",
                                    replace: true,
                                    label: suggestions.footer // Heading of group
                                });


                                AJS.$(suggestions.users).each(function(){
                                    groupDescriptor.addItem(new AJS.ItemDescriptor({
                                        value: this.name, // value of item added to select
                                        label: this.displayName, // title of lozenge
                                        html: this.html,
                                        allowDuplicate: false,
                                        icon: this.avatarUrl
                                    }));
                                });
    
                                ret.push(groupDescriptor);
                            });

                            return ret;
                        }
                    }
                });
            }
        });
    }

    AJS.$(document).bind("contentRefresh", function (e, context) {
        initSingleUserPicker(e.target);
    });

    // Bind the init function so it runs within the dialogs
    AJS.$(document).bind("dialogContentReady", function (e, dialog) {
        initSingleUserPicker(dialog.get$popupContent());
    });

    //Init the ones on the pages directly.
    initSingleUserPicker();
});