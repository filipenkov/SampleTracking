(function ($)
{
    AJS.namespace('JIRA.app.admin.email');
    var email = JIRA.app.admin.email;

    email.dialogInitializer = function() {
        AJS.$("#verifyServer").hide();
     };

    email.verifyServerConnection = function(e, url) {
        AJS.$("#verifyServer").show();
        AJS.$("#verifyMessages").hide();
        document.forms.jiraform.action = url;
        AJS.$(document.forms.jiraform).submit();

    };


    $(email.dialogInitializer);
})(AJS.$);
