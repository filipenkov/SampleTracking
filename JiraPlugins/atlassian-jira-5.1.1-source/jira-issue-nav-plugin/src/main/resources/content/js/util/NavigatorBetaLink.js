AJS.$(function($) {
    $("#content").find("h1").first().append(
        '<div id="try-beta-navigator"><a href="' + contextPath + '/secure/TryKickAssAction!default.jspa?enable=true">' + AJS.I18n.getText("issue.nav.try.beta") + '</a></div>'
    );
});
