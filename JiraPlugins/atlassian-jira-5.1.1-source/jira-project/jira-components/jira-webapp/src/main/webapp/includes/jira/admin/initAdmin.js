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

        activeAreaWidth = $activeArea.prop("scrollWidth");
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
        setActiveAreaWidth();
        AJS.$(window).resize(function () {
           setActiveAreaWidth();
        });
    }

    // Edit project dialog on Projects page
    AJS.$("#project-list .edit-project").each(function () {
        JIRA.createEditProjectDialog(this);
    });
});

JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context, reason) {
    if (reason !== JIRA.CONTENT_ADDED_REASON.panelRefreshed) {
        context.find('.jira-iconpicker-trigger').click(function(e) {
            e.preventDefault();
            var $this = AJS.$(this);
            var url = $this.attr('href');
            var $iconPickerTarget = $this.prev('input.text');
            if ($iconPickerTarget.length) {
                var popup = window.open(url, 'IconPicker',
                        'status=no,resizable=yes,top=100,left=200,width=580,height=650,scrollbars=yes');
                popup.focus();
            }
        });
    }
});
