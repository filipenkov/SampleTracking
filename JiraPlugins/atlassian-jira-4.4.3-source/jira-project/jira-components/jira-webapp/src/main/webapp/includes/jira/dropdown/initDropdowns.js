// Create all dropdowns
AJS.$(function () {

    // New Style dropdowns (Use this type from now on)
    AJS.Dropdown.create({
        trigger: ".issue-actions-trigger",
        ajaxOptions: {
            dataType: "json",
            cache: false,
            formatSuccess: JIRA.FRAGMENTS.issueActionsFragment
        }
    });

    var $navigatorOptions = AJS.$("#navigator-options");

    AJS.Dropdown.create({
        trigger: $navigatorOptions.find(".aui-dd-link"),
        content: $navigatorOptions.find(".aui-list"),
        alignment: AJS.RIGHT
    });


    var $commandBar = AJS.$("div.command-bar");

    AJS.Dropdown.create({
        trigger: $commandBar.find("a.drop"),
        content: $commandBar.find(".aui-list"),
        // We must not scroll to dropdown items for dropdowns in the stalker bar.
        // $.fn.stalker() and $.fn.scrollIntoView() conceptually DON'T MIX! Let's
        // hope your stalking dropdowns fit within your window height!
        autoScroll: false
    });

    AJS.$("#dashboard").find(".aui-dd-parent").dropDown("Standard", {
        trigger: "a.aui-dd-link"
    });

    AJS.$("#main-nav.admin-menu-bar").find("li.admin-menu-link").linkedMenu({
        onFocusRemoveClass: "#main-nav .selected"
    });

    AJS.$("#main-nav.standard-menu-bar").find("a.aui-dd-link").linkedMenu({
        reflectFocus: "#main-nav .lnk",
        onFocusRemoveClass: "#main-nav .selected"
    });

    AJS.$("#navigator-options").find("a.aui-dd-link").linkedMenu();

});

