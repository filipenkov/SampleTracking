
AJS.namespace("JIRA.FRAGMENTS");

JIRA.FRAGMENTS.issueActionsFragment = function () {

    function addIssueIdToReturnUrl(issueId) {
        var matchSelectedIssueId = /selectedIssueId=[0-9]*/g;

        if (self != top) {
            return encodeURIComponent(window.top.location.href);
        }

        var url = window.location.href,
           newUrl = url;

        if (/selectedIssueId=[0-9]*/.test(url)) {
            newUrl = url.replace(matchSelectedIssueId, "selectedIssueId=" + issueId);
        } else {
            if (url.lastIndexOf("?") >= 0) {
                newUrl = url + "&";
            } else {
                newUrl = url + "?";
            }
            newUrl = newUrl + "selectedIssueId=" + issueId;
        }
        return encodeURIComponent(newUrl);
    }

    return function(json) {

        var returnURL = addIssueIdToReturnUrl(json.id);
        var htmlParts = [
            '<div class="aui-list"><ul class="aui-list-section"><li class="aui-list-item"><a href="',
            contextPath,
            "/browse/",
            json.key,
            '" class="aui-list-item-link">',
            htmlEscape(json.viewIssue),
            '</a></li></ul>'
        ];

        var hasActions = json.actions && json.actions.length > 0;
        var hasOperations = json.operations && json.operations.length > 0;

        if (hasActions) {
            htmlParts.push(hasOperations ? '<ul class="aui-list-section">' : '<ul class="aui-list-section aui-last">');
            var URL_A = contextPath + "/secure/WorkflowUIDispatcher.jspa?id=" + json.id + "&amp;action=";
            var URL_B = "&amp;atl_token=" + json.atlToken + "&amp;returnUrl=" + returnURL;
            AJS.$.each(json.actions, function() {
                htmlParts.push(
                    '<li class="aui-list-item"><a href="',
                    URL_A,
                    this.action,
                    URL_B,
                    '" rel="',
                    this.action,
                    '" class="aui-list-item-link issueaction-workflow-transition" data-issueId="' + json.id + '">',
                    htmlEscape(this.name),
                    '</a></li>'
                );
            });
            htmlParts.push('</ul>');
        }

        if (hasOperations) {
            htmlParts.push('<ul class="aui-list-section aui-last">');
            URL_A = "&amp;returnUrl=" + returnURL;
            URL_B = "&amp;atl_token=" + json.atlToken;
            AJS.$.each(json.operations, function() {
                htmlParts.push(
                    '<li class="aui-list-item"><a href="',
                    this.url,
                    URL_A,
                    URL_B,
                    '" class="aui-list-item-link ',
                    this.styleClass,
                    '" data-issueId="' + json.id + '" data-issueKey="' + json.key + '">',
                    htmlEscape(this.name),
                    '</a></li>'
                );
            });
            htmlParts.push('</ul>');
        }

        htmlParts.push('</div>');

        return AJS.$(htmlParts.join(""));
    }

}();

/** Preserve legacy namespace
    @deprecated jira.issuepicker */
//AJS.namespace("jira.app.fragments", null, JIRA.FRAGMENTS);
