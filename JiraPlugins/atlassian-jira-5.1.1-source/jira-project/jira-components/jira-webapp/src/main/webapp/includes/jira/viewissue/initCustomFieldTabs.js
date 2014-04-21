(function ($) {
    var activateTab = function(tabNum) {
        // Set active tab
        $("#customfield-tabs li.active").removeClass("active");
        $("#tabCell" + tabNum).addClass("active");
        // Display active tab content
        $("#customfieldmodule ul.property-list:not(hidden)").addClass("hidden");
        var $tabPane = $("#tabCellPane" + tabNum);
        $tabPane.removeClass("hidden").trigger("tabSelect", {
            pane: $tabPane
        });
    };

    $(document).delegate("#customfield-tabs li a", "click", function(e) {
        e.preventDefault();
        var $this = AJS.$(this);
        activateTab($this.attr("rel"));
    });


    $(function () {
        var revealer = function(e) {
            var $containingTab = $(e.target).closest(".pl-tab");
            if ($containingTab.length > 0) {
                activateTab($containingTab.attr("id").substring("tabCellPane".length));
            }
        };

        // Remembering active tab after we refresh panel
        if (JIRA.Events.PANEL_REFRESHED) {
            // kickass
            JIRA.bind(JIRA.Events.PANEL_REFRESHED, function (e, panel, $new, $existing) {
                if (panel === "details-module") {
                    var $activeTab = $existing.find("#customfield-tabs li.active");
                    if ($activeTab.length === 1) {
                        $new.find("#" + $activeTab.attr("id") + " a").click();
                    }
                }
                $("#customfieldmodule").unbind("reveal");
                $("#customfieldmodule").bind("reveal", revealer);
            });
        }
        $("#customfieldmodule").bind("reveal", revealer);
    });

})(AJS.$);

