AJS.$(document).bind("dialogContentReady", function(e, dialog){
    JIRA.UserAutoComplete.init(dialog.get$popupContent());
});

AJS.$(function(){
    JIRA.UserAutoComplete.init();
    JIRA.IssueAutoComplete.init();
});

AJS.$(function(){
    AJS.$("fieldset.user-searcher-params").each(function(){
        var params = JIRA.parseOptionsFromFieldset(AJS.$(this)),
            $container = AJS.$("#" + params.fieldId + "_container");

        if (params.userPickerEnabled === true){
            var autocompleter = JIRA.UserAutoComplete({
                fieldID: params.fieldId,
                delimChar: params.multiSelect === true ? "," : undefined,
                ajaxData: {
                    fieldName: params.fieldName
                }
            });
        }

        var setupFields = function(related){
            var field = AJS.$("#" + params.fieldId);
            var userImage = AJS.$("#" + params.fieldId + "Image");
            var groupImage = AJS.$("#" + params.fieldId + "GroupImage");
            var fieldDesc = AJS.$("#" + params.fieldId + "_desc");
            if (related === "select.list.none"){
                field.val("").attr("disabled", "true");
                userImage.hide();
                groupImage.hide();
                fieldDesc.hide();
            } else{
                field.removeAttr("disabled");
                if (related === "select.list.group"){
                    userImage.hide();
                    groupImage.show();
                    if (params.userPickerEnabled === true){
                        autocompleter.disable();
                        fieldDesc.hide();
                    }
                } else {
                    userImage.show();
                    groupImage.hide();
                    if (params.userPickerEnabled === true){
                        autocompleter.enable();
                        fieldDesc.show();
                    }
                }
            }
        };

        AJS.$("#" + params.userSelect).change(function(){
            var related = AJS.$(this).find("option:selected").attr("rel");
            setupFields(related);
        }).find("option:selected").each(function(){
            setupFields(AJS.$(this).attr("rel"));
        });

        $container.find("a.user-popup-trigger").click(function(e){
            var url = contextPath + '/secure/popups/UserPickerBrowser.jspa?';
            url += 'formName=' + params.formName + '&';
            url += 'multiSelect=' + params.multiSelect + '&';
            url += 'element=' + params.fieldId;

            var vWinUsers = window.open(url, 'UserPicker', 'status=yes,resizable=yes,top=100,left=200,width=580,height=750,scrollbars=yes');
            vWinUsers.opener = self;
            vWinUsers.focus();
            e.preventDefault();
        });

        $container.find("a.group-popup-trigger").click(function(e){
            var url = contextPath + '/secure/popups/GroupPickerBrowser.jspa?';
            url += 'formName=' + params.formName + '&';
            url += 'multiSelect=' + params.multiSelect + '&';
            url += 'element=' + params.fieldId;

            var vWinUsers = window.open(url, 'GroupPicker', 'status=yes,resizable=yes,top=100,left=200,width=580,height=750,scrollbars=yes');
            vWinUsers.opener = self;
            vWinUsers.focus();
            e.preventDefault();
        });
    });
});
