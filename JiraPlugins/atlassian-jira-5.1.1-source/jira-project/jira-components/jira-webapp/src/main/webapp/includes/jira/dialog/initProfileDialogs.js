AJS.$(function(){
    var quickLinks = function () {
        var $quicklinks = AJS.$("#quicklinks");
        AJS.Dropdown.create({
            trigger: $quicklinks.find(".aui-dd-link"),
            content: $quicklinks.find(".aui-list"),
            alignment: AJS.RIGHT
        });
        return arguments.callee;
    }();

    var applyExpandos = function(){
        if (JIRA.ExpandableBlock){
            JIRA.ExpandableBlock.init();
        }
    };

    var initProjPickerForRoadMap = function(){
        AJS.$("#project-selector").change(function(e){
            jQuery(jQuery.ajax({
                url: contextPath + "/secure/ViewProfile.jspa?selectedTab=jira.user.profile.panels:up-roadmap-panel&decorator=none&contentOnly=true&pid=" + AJS.$(this).attr("value"),
                dataType: "html",
                success: function (response) {
                    AJS.$(".content-container > .content-body").html(response);
                    applyExpandos();
                    initProjPickerForRoadMap();
                }
            })).throbber({target: jQuery("#up_up-roadmap-panel_li")});

        });
    };

    var opts = {

        customInit : function(){
            JIRA.TabManager.navigationTabs.addLoadEvent("up_user-profile-summary-panel_a", function(){
                quickLinks();
            });

            JIRA.TabManager.navigationTabs.addLoadEvent("up_up-roadmap-panel_a", function(){
                applyExpandos();
                initProjPickerForRoadMap();
            });

        },
        getTabRegEx: /selectedTab=.*/,
        checkQualifiedUrlRegEx: /[\?&](?=selectedTab=)/,
        idGeneratorRegEx: /.*(?=selectedTab=)/
    };

    JIRA.TabManager.navigationTabs.init(opts);
    applyExpandos();
    initProjPickerForRoadMap();

    new JIRA.EditProfileDialog({
        trigger: "#edit_profile_lnk",
        autoClose: true
    });

    new JIRA.UserProfileDialog({
        trigger: "#view_change_password",
        autoClose: true
    });

    new JIRA.UserProfileDialog({
        trigger: "#view_clear_rememberme",
        autoClose: true
    });

    new JIRA.EditPreferencesDialog({
        trigger: "#edit_prefs_lnk",
        autoClose: true
    });
});
