JIRA.Dropdowns = {};

JIRA.Dropdowns.bindGenericDropdowns = function (ctx) {

    AJS.$(".js-default-dropdown", ctx).each(function () {

        var $trigger = AJS.$(this),
            $content = $trigger.next(".aui-list"),
            alignment = $trigger.attr("data-alignment") || AJS.RIGHT;

        if ($content.length == 0) {
            console.warn("Dropdown init failied. Could not find content. Printing culprit...");
            console.log($trigger);
        }

        new AJS.Dropdown({
            trigger: $trigger,
            content: $content,
            alignment: alignment
        });
    });
};

/**
 * Binds issue action (cog) dropdowns
 * @param ctx
 */
JIRA.Dropdowns.bindIssueActionsDds = function (ctx) {
    return AJS.Dropdown.create({
        trigger: AJS.$(".issue-actions-trigger", ctx),
        ajaxOptions: {
            dataType: "json",
            cache: false,
            formatSuccess: JIRA.FRAGMENTS.issueActionsFragment
        }
    });
};

/**
 * Binds dropdowns that control the views & columns in issue navigator
 */
JIRA.Dropdowns.bindNavigatorOptionsDds = function () {
    var $navigatorOptions = AJS.$("#navigator-options");

    AJS.Dropdown.create({
        trigger: $navigatorOptions.find(".aui-dd-link"),
        content: $navigatorOptions.find(".aui-list"),
        alignment: AJS.RIGHT
    });
    $navigatorOptions.find("a.aui-dd-link").linkedMenu();
};



/**
 * Binds all the dropdowns that support the dashboard chrome
 */
JIRA.Dropdowns.bindConfigDashboardDds = function () {
    AJS.$("#dashboard").find(".aui-dd-parent").dropDown("Standard", {
        trigger: "a.aui-dd-link"
    });
};

/**
 * Binds all the header Dropdowns. Including global nav and user profile.
 */
JIRA.Dropdowns.bindHeaderDds = function () {

    AJS.$("#main-nav li").each(function () {

        var $this = AJS.$(this),
            trigger = $this.hasClass("admin-menu-link") ? $this : AJS.$(".drop", this);

        if ($this.hasClass("lazy")) {
            AJS.Dropdown.create({
                alignment: AJS.LEFT,
                offsetTarget: $this,
                trigger: trigger,
                styleClass: "main-nav-dropdown",
                ajaxOptions: {
                    url: contextPath + "/rest/api/1.0/menus/" + trigger.attr("rel"),
                    dataType: "json",
                    cache: false,
                    formatSuccess: JIRA.FRAGMENTS.menuFragment
                }
            });
        } else if ($this.hasClass("nonlazy")) {
            AJS.Dropdown.create({
                alignment: AJS.LEFT,
                offsetTarget: $this,
                styleClass: "main-nav-dropdown",
                trigger: trigger,
                content: $this.find(".aui-list")
            });
        }
    });

    AJS.Dropdown.create({
        alignment: AJS.RIGHT,
        trigger: AJS.$("#header-details-user .drop"),
        content: AJS.$("#user-options-list")
    });

    AJS.$("#main-nav.admin-menu-bar").find("li.admin-menu-link").linkedMenu({
        onFocusRemoveClass: "#main-nav .selected"
    });

    AJS.$("#main-nav.standard-menu-bar").find("a.aui-dd-link").linkedMenu({
        reflectFocus: "#main-nav .lnk",
        onFocusRemoveClass: "#main-nav .selected"
    });
};

AJS.$(function () {
    JIRA.Dropdowns.bindNavigatorOptionsDds();
    JIRA.Dropdowns.bindConfigDashboardDds();
    JIRA.Dropdowns.bindHeaderDds();
});

JIRA.bind("Issue.subtasksRefreshed", function (e, ctx) {
    JIRA.Dropdowns.bindIssueActionsDds(ctx);
});

JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, ctx) {
    JIRA.Dropdowns.bindIssueActionsDds(ctx);
    JIRA.Dropdowns.bindGenericDropdowns(ctx);
});



