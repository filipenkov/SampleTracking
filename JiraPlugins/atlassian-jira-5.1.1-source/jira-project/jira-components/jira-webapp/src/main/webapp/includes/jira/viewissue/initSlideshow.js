(function ($) {

    function initGallery($el) {
        
        var closeFancyBox = function () {
            $el.fancybox.close();
        };

        var isFireFoxLinux = function () {
            return $.os.linux && $.browser.mozilla;
        };

        var useOverlay = true;
        // FF on Linux looks like a car accident when the overlay is applied.  Its all over the place
        // like a mad womans breakfast.  So lets opt out for that combination.  FF in Windows/M<ac is not affected
        if (isFireFoxLinux()) {
            useOverlay = false;
        }

        var fancyBoxOptions = {
            'imageScale':true,
            'centerOnScroll':false,
            'overlayShow':useOverlay,
            //looks like this isn't used a all??
            callbackOnStart:function () {
                $("#header").css("zIndex", "-1");
                if (useOverlay) {
                    $("body").addClass("fancybox-show");
                }
            },
            //looks like this isn't used a all??
            'callbackOnShow':function () {
                $(document).click(function () {
                    closeFancyBox();
                });
            },
            'onComplete':function () {
                // fix up title lozenge placement for narrow images (JRADEV-1797)
                var title = $('#fancybox-title');

                var mainWidth = $('#fancybox-title-main').outerWidth();
                var leftWidth = $("#fancybox-title-left").outerWidth();
                var rightWidth = $("#fancybox-title-right").outerWidth();

                title.width(mainWidth + leftWidth + rightWidth + 5);

                var imageDivWidth = $('#fancybox-inner').width();
                title.css("marginLeft", -(title.width() / 2) + (imageDivWidth / 2));
                title.css('bottom', title.outerHeight(true) * -1);
            },
            //looks like this isn't used a all??
            'callbackOnClose':function () {
                $("#header").css("zIndex", "");
                if (useOverlay) {
                    $("body").removeClass("fancybox-show");
                }
                $(document).unbind('click', closeFancyBox);
                if ($.browser.safari) {
                    var top = $(window).scrollTop();
                    $(window).scrollTop(10 + 5 * (top == 10)).scrollTop(top);
                }
            }
        };
        if ($.browser.msie) {
            fancyBoxOptions.transitionIn = 'none';
            fancyBoxOptions.transitionOut = 'none';
        }
        $el.fancybox(fancyBoxOptions);
    }


    function initWorkflow($ctx) {
        $("a.issueaction-viewworkflow", $ctx).each(function () {
            var title = $(this).attr("rel");
            if (title) {
                title = AJS.escapeHTML(title);
            }

            $(this).fancybox({
                type:"image",
                href:this.href,
                title:title,
                titlePosition:"outside",
                imageScale:true,
                centerOnScroll:true,
                overlayShadow:true
            });
        });
    }

    JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, $ctx, reason) {
        initGallery($("a.gallery", $ctx));
        initWorkflow($ctx);
    });

    jQuery(function () {
        if (JIRA.Events.PANEL_REFRESHED) {
            JIRA.bind(JIRA.Events.PANEL_REFRESHED, function (e, panel, $new, $existing) {
                if (panel === "attachmentmodule") {
                    initGallery($("a.gallery", $new));
                } else if (panel === "details-module") {
                    initWorkflow($new);
                }
            });
        }
    });


})(AJS.$);






