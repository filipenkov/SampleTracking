JIRA.containDropdown = function (dropdown, containerSelector, dynamic) {

    function getDropdownOffset() {
        return dropdown.$.offset().top - jQuery(containerSelector).offset().top;
    }

    var container,
        ddOffset,
        availableArea,
        shadowOffset = 25;

    if (dropdown.$.parents(containerSelector).length !== -1) {

        container = jQuery(containerSelector),
        ddOffset = getDropdownOffset(),
        shadowOffset = 30,
        availableArea = container.outerHeight() - ddOffset - shadowOffset;

        if (availableArea <= parseInt(dropdown.$.attr("scrollHeight"), 10)) {
            JIRA.containDropdown.containHeight(dropdown, availableArea);
        } else if (dynamic) {
            JIRA.containDropdown.releaseContainment(dropdown);
        }   
        dropdown.reset();
    }
};

JIRA.containDropdown.containHeight = function (dropdown, availableArea) {
    dropdown.$.css({
        height: availableArea
    });
    if (dropdown.$.css("overflowY") !== "scroll") {
        dropdown.$.css({
            width: 15 + dropdown.$.attr("scrollWidth"),
            overflowY: "scroll",
            overflowX: "hidden"
        });
    }
};

JIRA.containDropdown.releaseContainment = function(dropdown) {
    dropdown.$.css({
        height: "",
        width: "",
        overflowY: "",
        overflowX: ""
    });
};


/** Preserve legacy namespace
    @deprecated AJS.containDropdown */
AJS.namespace("AJS.containDropdown", null, JIRA.containDropdown);

