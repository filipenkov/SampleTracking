AJS.$(function () {

    var $activeWrap,
        $activeArea;

    function setActiveAreaWidth() {

        var activeAreaWidth,
            activeWrapWidth;

        if (!$activeWrap) {
            $activeWrap = AJS.$("div.admin-active-wrap");
            $activeArea = AJS.$("div.admin-active-area");
        }

        activeAreaWidth = $activeArea.attr("scrollWidth");
        activeWrapWidth = $activeWrap.width();

        if (activeAreaWidth >  activeWrapWidth) {
            $activeWrap.width(activeAreaWidth);
        } else {
            $activeWrap.css({
                width: ""
            });
        }
    }


    /**
     * In IE 7 we need to explicitly set the width of the parent container so that child content does not overflow its
     * bounds.
     */
    if (AJS.$.browser.msie && AJS.$.browser.version == 7) {
        setActiveAreaWidth()
        AJS.$(window).resize(function () {
           setActiveAreaWidth();
        });
    }

    // Edit project dialog on Projects page
    AJS.$("#project-list .edit-project").each(function () {
        JIRA.createEditProjectDialog(this);
    });

});