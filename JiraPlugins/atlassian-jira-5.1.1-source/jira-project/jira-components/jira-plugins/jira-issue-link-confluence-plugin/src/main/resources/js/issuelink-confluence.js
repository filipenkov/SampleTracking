/**
 * Initialises OAuth authentication for Confluence Application Links. Requires the following elements:
 * <div class="issue-link-applinks-authentication-message"></div>
 */
(function ($) {

    var settings = {
        getCurrentAppId: function (context) {
            return $("#issue-link-confluence-app-id", context).val();
        },
        shouldExecute: function (context) {
            return $("#confluence-page-link", context).length !== 0;
        }
    };

    JIRA.bind(JIRA.Events.NEW_PAGE_ADDED, function (e, context) {
        IssueLinkAppLinks.init(settings, context);
    });

})(AJS.$);