AJS.$(function () {
    JIRA.Issue.wireAssignToMeLink(document);

    AJS.$(".assignee-edit-group").each(function(){
        var $this = AJS.$(this);
        var assigneeFieldId = $this.attr("rel");

        AJS.$("#assignee_userpicker_dummy_" + assigneeFieldId +"_container").click(function (){
            AJS.$("#assignee_radio_picker_" + assigneeFieldId).attr("checked", "true");
        });

        $this.parents("form[name=jiraform]").submit(function(){
            $this.find("input[name=assignee_radio]:checked").each(function(){
                if(AJS.$(this).attr("id") == "assignee_radio_picker_" + assigneeFieldId){
                    AJS.$("#" + assigneeFieldId).val(AJS.$("#assignee_userpicker_dummy_" + assigneeFieldId).val());
                } else {
                    AJS.$("#" + assigneeFieldId).val(AJS.$(this).val());
                }
            });
        });
    });
});
