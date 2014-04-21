AJS.$(function() {

    var mentionsCtr = (AJS.DarkFeatures.isEnabled("jira.legacy.mentions.disabled")) ? JIRA.Mention : JIRA.LegacyMention;
    var mentionsController;

    function initMentions() {
        if (!mentionsController) {
            mentionsController = new mentionsCtr();
        }
        mentionsController.textarea(this);
    }

    AJS.$(document).delegate(".mentionable", "focus", initMentions);
    AJS.$(".mentionable").each(initMentions);
});
